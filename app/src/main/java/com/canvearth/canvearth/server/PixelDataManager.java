package com.canvearth.canvearth.server;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.client.PixelEvents;
import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.utils.BitmapUtils;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.MathUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.concurrency.CountUpDownLatch;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PixelDataManager {
    private static final PixelDataManager ourInstance = new PixelDataManager();
    private static final String TAG = "PixelDataManager";

    public static PixelDataManager getInstance() {
        return ourInstance;
    }

    private Map<String, WatchingPixel> watchingPixels = new HashMap<>();

    private PixelDataManager() {
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

    public void watchPixels(List<PixelData> pixelCoords) {
        for (PixelData pixelCoord: pixelCoords) {
            watchPixel(pixelCoord);
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

    // You don't have to watch this pixel (for now).. I'm nervous about performance issue of this method.
    // returns Bitmap which has resolution of 2^resolutionFactor * 2^resolutionFactor
    // TODO cache this when there is performance issue
    // Do we need Async version of this?
    public Bitmap getBitmapSync(PixelData pixelCoord, int resolutionFactor) {
        int resolution = MathUtils.intPow(2, resolutionFactor);
        final Bitmap bitmap = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
        try {
            int hierarchy = Math.min(Constants.LEAF_PIXEL_ZOOM_LEVEL - pixelCoord.zoom, resolutionFactor);
            int chargeForOnePixel = MathUtils.intPow(2, resolutionFactor - hierarchy);
            ArrayList<PixelData> childrenPixelCoord = PixelUtils.getChildrenPixelCoord(pixelCoord, hierarchy);
            int childrenStartX = childrenPixelCoord.get(0).x;
            int childrenStartY = childrenPixelCoord.get(0).y;
            CountDownLatch latchForFinish = new CountDownLatch(childrenPixelCoord.size());
            for (PixelData childPixelCoord: childrenPixelCoord) {
                String firebaseId = childPixelCoord.firebaseId;
                DatabaseUtils.getPixelReference(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.v(TAG, "Got one pixel");
                        FBPixel ServerPixel4Firebase = dataSnapshot.getValue(FBPixel.class);
                        if (ServerPixel4Firebase == null) {
                            ServerPixel4Firebase = FBPixel.emptyPixel();
                        }
                        int color = BitmapUtils.intColor(ServerPixel4Firebase.color);
                        int relativeCoordX = childPixelCoord.x - childrenStartX;
                        int relativeCoordY = childPixelCoord.y - childrenStartY;
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


    public void writePixelAsync(PixelData pixelData, Color color, @Nullable Runnable callback) {
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
            final CountUpDownLatch latchForAllFinish = new CountUpDownLatch(1);
            DatabaseUtils.getPixelReference(firebaseId).setValue(newPixel, (@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) -> {
                Log.v(TAG, "setValue finished");
                latchForAllFinish.countDown();
            }); // TODO transaction based on time / push uid
            updateParent(originalPixel, newPixel, pixelData, latchForAllFinish);
            new Thread(() -> {
                try {
                    latchForAllFinish.await();
                    if (callback != null) {
                        callback.run();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "in callback - " +  e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // Please prefer writePixelAsync, for performance.
    public void writePixelSync(PixelData pixelCoord, Color color) {
        try {
            CountDownLatch latchForFinish = new CountDownLatch(1);
            writePixelAsync(pixelCoord, color, latchForFinish::countDown);
            latchForFinish.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private void updateParent(FBPixel childOriginPixel, FBPixel childNewPixel,
                              PixelData childPixelData, final CountUpDownLatch latchForAllFinish) {
        if (childPixelData.isRoot()) {
            return;
        }
        try {
            PixelData parentPixelData = PixelUtils.getParentPixelData(childPixelData);
            FBPixel parentFBPixel = readPixelInstantly(parentPixelData);
            FBPixel newParentFBPixel = parentFBPixel.clone();
            newParentFBPixel.futureColor.replaceColorPortion(childOriginPixel.color, childNewPixel.color, 0.25);
            if (Color.areDifferent(newParentFBPixel.color, newParentFBPixel.futureColor)) {
                newParentFBPixel.color = newParentFBPixel.futureColor.clone();
                latchForAllFinish.countUp();
                String parentId = parentPixelData.firebaseId;
                DatabaseUtils.getPixelReference(parentId).setValue(newParentFBPixel,
                        (@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) -> {
                            Log.v(TAG, "setValue finished");
                            latchForAllFinish.countDown();
                        });
                updateParent(parentFBPixel, newParentFBPixel, parentPixelData, latchForAllFinish);
            } else {
                Log.v(TAG, "Update canceled - original color is " + newParentFBPixel.color.toString()
                        + " , future color is " + newParentFBPixel.futureColor.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
