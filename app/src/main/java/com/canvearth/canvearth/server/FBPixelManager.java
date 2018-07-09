package com.canvearth.canvearth.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.client.PixelEvents;
import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.utils.BitmapUtils;
import com.canvearth.canvearth.utils.ColorUtils;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.MathUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.concurrency.Function;
import com.canvearth.canvearth.utils.concurrency.CountUpDownLatch;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class FBPixelManager {
    private static final FBPixelManager ourInstance = new FBPixelManager();
    private static final String TAG = "FBPixelManager";

    public static FBPixelManager getInstance() {
        return ourInstance;
    }

    private Map<String, WatchingPixel> watchingPixels = new HashMap<>();

    private FBPixelManager() {
    }

    // Client have to call watchPixel to keep track pixel data.
    public void watchPixel(final PixelData pixelData) {
        final String firebaseId = pixelData.firebaseId;
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FBPixel FBPixel;
                if (pixelData.zoom < Constants.LEAF_PIXEL_ZOOM_LEVEL) {
                    FBPixel = dataSnapshot.getValue(FBPixel.class);
                } else {
                    FBPixel = dataSnapshot.getValue(LeafFBPixel.class);
                }
                watchingPixels.get(firebaseId).setFBPixel(FBPixel);
                PixelEvents.watchingPixelChanged(FBPixel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "load pixel data:onCancelled", databaseError.toException());
            }
        };
        watchingPixels.put(firebaseId, new WatchingPixel(null, valueEventListener));
        DatabaseUtils.getPixelReference(firebaseId).addValueEventListener(valueEventListener);
    }

    public void watchPixels(List<PixelData> pixelDataList) {
        for (PixelData pixelData: pixelDataList) {
            watchPixel(pixelData);
        }
    }

    // Client have to call unwatchPixel when you don't need to track pixel data anymore.
    public void unwatchPixel(PixelData pixelData) {
        String firebaseId = pixelData.firebaseId;

        ValueEventListener registeredListener = watchingPixels.get(firebaseId).getValueEventListener();
        DatabaseUtils.getPixelReference(firebaseId).removeEventListener(registeredListener);
        watchingPixels.remove(firebaseId);
    }

    public FBPixel readPixel(PixelData pixelData) {
        String firebaseId = pixelData.firebaseId;
        return watchingPixels.get(firebaseId).getFBPixel();
    }

    public void unwatchPixels(List<PixelData> pixelDataList) {
        for (PixelData pixelData: pixelDataList) {
            unwatchPixel(pixelData);
        }
    }

    // You can read unwatching pixel by this method
    private FBPixel readPixelInstantly(PixelData pixelData) throws InterruptedException {
        String firebaseId = pixelData.firebaseId;
        final FBPixel fbPixel = FBPixel.emptyPixel();
        final CountDownLatch latchForFinish = new CountDownLatch(1);
        DatabaseUtils.getPixelReference(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FBPixel serverFBPixel = dataSnapshot.getValue(FBPixel.class);
                if (serverFBPixel == null) {
                    latchForFinish.countDown();
                } else {
                    serverFBPixel.copyTo(fbPixel);
                    latchForFinish.countDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "load pixel data:onCancelled", databaseError.toException());
                latchForFinish.countDown();
            }
        });
        latchForFinish.await();
        return fbPixel;
    }

    // prefer use this rather than getBitmapAsync
    public void getCachedBitmapAsync(PixelData pixelData, Function<Bitmap> callback) {
        String firebaseId = pixelData.firebaseId;
        int bitmapSide = MathUtils.intPow(2, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
        DatabaseUtils.getBitmapReference(firebaseId)
                .getBytes(Constants.BITMAP_PNG_MAX_BYTES)
                .addOnSuccessListener((bytes -> {
                    Log.v(TAG, "Got bitmap");
                    Bitmap receivedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap mutableBitmap = BitmapUtils.getMutableBitmap(receivedBitmap);
                    callback.run(mutableBitmap);
                }))
                .addOnFailureListener((exception) -> {
                    //TODO when another error?
                    Log.v(TAG, "Could not get bitmap");
                    callback.run(BitmapUtils.emptyBitmap(bitmapSide, bitmapSide));
                });
    }

    // You don't have to watch this pixel (for now).. I'm nervous about performance issue of this method.
    // returns Bitmap which has resolution of 2^resolutionFactor * 2^resolutionFactor
    // Do we need Async version of this?
    public Bitmap getBitmapSync(PixelData pixelData, int resolutionFactor) {
        int resolution = MathUtils.intPow(2, resolutionFactor);
        final Bitmap bitmap = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
        try {
            int hierarchy = Math.min(Constants.LEAF_PIXEL_ZOOM_LEVEL - pixelData.zoom, resolutionFactor);
            int chargeForOnePixel = MathUtils.intPow(2, resolutionFactor - hierarchy);
            ArrayList<PixelData> childrenPixelData = PixelUtils.getChildrenPixelData(pixelData, hierarchy);
            int childrenStartX = childrenPixelData.get(0).x;
            int childrenStartY = childrenPixelData.get(0).y;
            CountDownLatch latchForFinish = new CountDownLatch(childrenPixelData.size());
            for (PixelData childPixelData: childrenPixelData) {
                String firebaseId = childPixelData.firebaseId;
                DatabaseUtils.getPixelReference(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        FBPixel ServerFBPixel;
                        if (childPixelData.isLeaf()) {
                            ServerFBPixel = dataSnapshot.getValue(LeafFBPixel.class);
                        } else {
                            ServerFBPixel = dataSnapshot.getValue(FBPixel.class);
                        }
                        if (ServerFBPixel == null) {
                            ServerFBPixel = FBPixel.emptyPixel();
                        }
                        int color = BitmapUtils.intColor(ServerFBPixel.color);
                        int relativeCoordX = childPixelData.x - childrenStartX;
                        int relativeCoordY = childPixelData.y - childrenStartY;
                        for (int y = 0; y < chargeForOnePixel; y++) {
                            for (int x = 0; x < chargeForOnePixel; x++) {
                                bitmap.setPixel(relativeCoordX * chargeForOnePixel + x,
                                        relativeCoordY * chargeForOnePixel + y, color);
                            }
                        }
                        latchForFinish.countDown();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "load pixel data:onCancelled", databaseError.toException());
                        latchForFinish.countDown();
                    }
                });
            }
            latchForFinish.await();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bitmap;
    }

    public void writePixelAsync(PixelData pixelData, Color color, @Nullable Function<PixelData> callback) {
        new Thread(()-> {
            try {
                if (!pixelData.isLeaf()) {
                    throw new Exception("Pixel is not leaf");
                }
                String firebaseId = pixelData.firebaseId;
                // You have to watch pixel before you write it.
                if (!watchingPixels.containsKey(firebaseId)) {
                    throw new Exception("Try to write pixel which is not watched");
                }
                FBPixel originalPixel = watchingPixels.get(firebaseId).getFBPixel();
                UserInformation userInformation = UserInformation.getInstance();
                String userToken = userInformation.getToken();
                LeafFBPixel newPixel = new LeafFBPixel(color, userToken, new Date()); // TODO consider when timezone differs, or abusing current datetime
                final CountUpDownLatch latchForAllFinish = new CountUpDownLatch(2);
                updateAncestorBitmapCacheAsync(newPixel, pixelData, (StorageMetadata metadata) -> {
                    latchForAllFinish.countDown();
                });
                DatabaseUtils.getPixelReference(firebaseId).setValue(newPixel, (@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) -> {
                    Log.v(TAG, "setValue finished");
                    latchForAllFinish.countDown();
                }); // TODO transaction based on time / push uid
                final PixelData lastUpdatedPixelData = updateParent(originalPixel, newPixel, pixelData, latchForAllFinish);
                new Thread(() -> {
                    try {
                        latchForAllFinish.await();
                        if (callback != null) {
                            callback.run(lastUpdatedPixelData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "in callback - " + e.getMessage());
                    }
                }).start();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }).start();
    }

    // Please prefer writePixelAsync, for performance.
    // returns last update pixel data.
    public PixelData writePixelSync(PixelData pixelData, Color color) {
        final PixelData returnPixelData = new PixelData(0, 0, 0);
        try {
            CountDownLatch latchForFinish = new CountDownLatch(1);
            writePixelAsync(pixelData, color, (PixelData lastUpdatedPixelData) -> {
                returnPixelData.copyFrom(lastUpdatedPixelData);
                latchForFinish.countDown();
            });
            latchForFinish.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        return returnPixelData;
    }

    // returns last updated pixel
    private PixelData updateParent(FBPixel childOriginPixel, FBPixel childNewPixel,
                              PixelData childPixelData, final CountUpDownLatch latchForAllFinish) {
        if (childPixelData.isRoot()) {
            return childPixelData;
        }
        try {
            PixelData parentPixelData = PixelUtils.getParentPixelData(childPixelData);
            FBPixel parentFBPixel = readPixelInstantly(parentPixelData);
            FBPixel newParentFBPixel = parentFBPixel.clone();
            newParentFBPixel.futureColor.replaceColorPortion(childOriginPixel.color, childNewPixel.color, 0.25);
            if (ColorUtils.areDifferent(newParentFBPixel.color, newParentFBPixel.futureColor)) {
                newParentFBPixel.color = newParentFBPixel.futureColor.clone();
                latchForAllFinish.countUp();
                String parentId = parentPixelData.firebaseId;
                updateAncestorBitmapCacheAsync(newParentFBPixel, parentPixelData, (StorageMetadata metadata) -> {
                    latchForAllFinish.countDown();
                });
                latchForAllFinish.countUp();
                DatabaseUtils.getPixelReference(parentId).setValue(newParentFBPixel,
                        (@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) -> {
                            Log.v(TAG, "setValue finished");
                            latchForAllFinish.countDown();
                        });
                return updateParent(parentFBPixel, newParentFBPixel, parentPixelData, latchForAllFinish);
            } else {
                Log.v(TAG, "Update canceled - original color is " + newParentFBPixel.color.toString()
                        + " , future color is " + newParentFBPixel.futureColor.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return childPixelData;
    }

    // TODO Update has to be transaction
    private void updateAncestorBitmapCacheAsync(FBPixel childNewPixel, PixelData childPixelData, Function<StorageMetadata> callback) {
        try {
            if (childPixelData.zoom - Constants.BITMAP_CACHE_RESOLUTION_FACTOR < 0) {
                return;
            }
            PixelData ancestorPixelData = PixelUtils.getAncestorPixelData(childPixelData, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
            String ancestorFirebaseId = ancestorPixelData.firebaseId;
            getCachedBitmapAsync(ancestorPixelData, (Bitmap bitmap) -> {
                int relativeX = childPixelData.x - ancestorPixelData.x * MathUtils.intPow(2, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
                int relativeY = childPixelData.y - ancestorPixelData.y * MathUtils.intPow(2, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
                Log.v(TAG, "updating " + relativeX + "," + relativeY);
                bitmap.setPixel(relativeX, relativeY, BitmapUtils.intColor(childNewPixel.color));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] uploadData = baos.toByteArray();
                UploadTask uploadTask = DatabaseUtils.getBitmapReference(ancestorFirebaseId).putBytes(uploadData);
                uploadTask.addOnFailureListener((@NonNull Exception exception) -> {
                        Log.e(TAG, "Firebase storage upload failed");
                    }).addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
                        Log.v(TAG, "Firebase storage upload succeed");
                        callback.run(taskSnapshot.getMetadata());
                    });
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
