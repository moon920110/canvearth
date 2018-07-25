package com.canvearth.canvearth.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.client.PixelEvents;
import com.canvearth.canvearth.pixel.PixelColor;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.utils.BitmapUtils;
import com.canvearth.canvearth.utils.ColorUtils;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.MathUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.concurrency.Function;
import com.canvearth.canvearth.utils.concurrency.CountUpDownLatch;
import com.canvearth.canvearth.utils.concurrency.Succeed;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URL;
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

    public Map<String, WatchingPixel> watchingPixels = new HashMap<>();

    private FBPixelManager() {
    }

    // Client have to call watchPixel to keep track pixel data.
    public void watchPixel(final PixelData pixelData) {
        final String firebaseId = pixelData.getFirebaseId();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FBPixel FBPixel;
                if (pixelData.zoom < Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL) {
                    FBPixel = dataSnapshot.getValue(FBPixel.class);
                } else {
                    FBPixel = dataSnapshot.getValue(LeafFBPixel.class);
                }
                watchingPixels.get(firebaseId).setFBPixel(FBPixel);
                PixelEvents.watchingPixelChanged(pixelData, FBPixel);
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
        for (PixelData pixelData : pixelDataList) {
            watchPixel(pixelData);
        }
    }

    // Client have to call unwatchPixel when you don't need to track pixel data anymore.
    public void unwatchPixel(PixelData pixelData) {
        String firebaseId = pixelData.getFirebaseId();

        WatchingPixel watchingPixel = watchingPixels.get(firebaseId);
        if (watchingPixel == null) {
            return;
        }
        ValueEventListener registeredListener = watchingPixel.getValueEventListener();
        DatabaseUtils.getPixelReference(firebaseId).removeEventListener(registeredListener);
        watchingPixels.remove(firebaseId);
    }

    public FBPixel readPixel(PixelData pixelData) {
        String firebaseId = pixelData.getFirebaseId();
        return watchingPixels.get(firebaseId).getFBPixel();
    }

    public void unwatchPixels(List<PixelData> pixelDataList) {
        for (PixelData pixelData : pixelDataList) {
            unwatchPixel(pixelData);
        }
    }

    // You can read unwatching pixel by this method
    private FBPixel readPixelInstantly(PixelData pixelData) throws InterruptedException {
        String firebaseId = pixelData.getFirebaseId();
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
        String firebaseId = pixelData.getFirebaseId();
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

    class UrlWapper {
        URL url;
    }

    public URL getCachedBitmapDownloadUrlSync(PixelData pixelData) throws InterruptedException {
        CountDownLatch latchForFinish = new CountDownLatch(1);
        UrlWapper urlWapper = new UrlWapper();
        getCachedBitmapDownloadUrl(pixelData, (Uri uri) -> {
            try {
                urlWapper.url = new URL(uri.toString());
                latchForFinish.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        latchForFinish.await();
        return urlWapper.url;
    }

    public void getCachedBitmapDownloadUrl(PixelData pixelData, Function<Uri> callback) {
        String firebaseId = pixelData.getFirebaseId();
        DatabaseUtils.getBitmapReference(firebaseId).getDownloadUrl()
                .addOnSuccessListener(callback::run);
    }

    private static class GetBitmapAsyncTask extends AsyncTask<Pair<PixelData, Integer>, Void, Bitmap> {
        private Function<Bitmap> callback;

        public GetBitmapAsyncTask(Function<Bitmap> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Pair<PixelData, Integer>[] params) {
            return FBPixelManager.getInstance().getBitmapSync(params[0].first, params[0].second);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            callback.run(bitmap);
        }
    }

    public void getBitmapAsync(PixelData pixelData, int resolutionFactor, Function<Bitmap> callback) {
        new GetBitmapAsyncTask(callback).execute(new Pair<>(pixelData, resolutionFactor));
    }

    // You don't have to watch this pixel (for now).. I'm nervous about performance issue of this method.
    // returns Bitmap which has resolution of 2^resolutionFactor * 2^resolutionFactor
    // TODO this seems better to executed in server side.
    public Bitmap getBitmapSync(PixelData pixelData, int resolutionFactor) {
        int resolution = MathUtils.intPow(2, resolutionFactor);
        final Bitmap bitmap = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
        try {
            int hierarchy = Math.min(Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL - pixelData.zoom, resolutionFactor);
            int chargeForOnePixel = MathUtils.intPow(2, resolutionFactor - hierarchy);
            ArrayList<PixelData> childrenPixelData = PixelUtils.getChildrenPixelData(pixelData, hierarchy);
            int childrenStartX = childrenPixelData.get(0).x;
            int childrenStartY = childrenPixelData.get(0).y;
            CountDownLatch latchForFinish = new CountDownLatch(childrenPixelData.size());
            for (PixelData childPixelData : childrenPixelData) {
                String firebaseId = childPixelData.getFirebaseId();
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
                        int color = BitmapUtils.intColor(ServerFBPixel.pixelColor);
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

    public Thread writePixelAsync(PixelData pixelData, PixelColor pixelColor) {
        Thread thread = new Thread(() -> {
            try {
                if (!pixelData.isLeaf()) {
                    throw new Exception("Pixel is not leaf");
                }
                String firebaseId = pixelData.getFirebaseId();
                // You have to watch pixel before you write it.
                if (!watchingPixels.containsKey(firebaseId)) {
                    throw new Exception("Try to write pixel which is not watched");
                }

                final CountUpDownLatch latchForAllFinish = new CountUpDownLatch(1);
                final CountDownLatch latchForCurrentUpdateFinish = new CountDownLatch(1);
                final FBPixel newPixel = new FBPixel();
                // TODO need to check again for transaction logic
                DatabaseUtils.getPixelReference(firebaseId).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        try {
                            FBPixel serverOriginPixel = mutableData.getValue(LeafFBPixel.class);
                            if (serverOriginPixel == null) {
                                serverOriginPixel = FBPixel.emptyPixel();
                            }
                            UserInformation userInformation = UserInformation.getInstance();
                            String userToken = userInformation.getToken();
                            LeafFBPixel newPixel = new LeafFBPixel(pixelColor, userToken, new Date()); // TODO consider when timezone differs, or abusing current datetime
                            mutableData.setValue(newPixel);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        if (!b) {
                            Log.e(TAG, "transaction failed");
                            return;
                        }
                        dataSnapshot.getValue(LeafFBPixel.class).copyTo(newPixel);
                        updateAncestorBitmapCacheAsync(newPixel, pixelData, (StorageMetadata metadata) -> {
                            latchForAllFinish.countDown();
                        });
                        latchForCurrentUpdateFinish.countDown();
                    }
                });
                latchForCurrentUpdateFinish.await();
                final PixelData lastUpdatedPixelData = updateParent(newPixel, pixelData, latchForAllFinish);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        thread.start();
        return thread;
    }

    // returns last updated pixel
    private PixelData updateParent(FBPixel childNewPixel,
                                   PixelData childPixelData, final CountUpDownLatch latchForAllFinish) {
        if (childPixelData.isRoot()) {
            return childPixelData;
        }
        try {
            PixelData parentPixelData = PixelUtils.getParentPixelData(childPixelData);
            String parentId = parentPixelData.getFirebaseId();
            final FBPixel parentFBPixel = new FBPixel();
            final FBPixel newParentFBPixel = new FBPixel();
            final Succeed needUpdate = new Succeed();
            final CountDownLatch latchForCurrentUpdateFinish = new CountDownLatch(1);
            latchForAllFinish.countUp();
            DatabaseUtils.getPixelReference(parentId).runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    try {
                        FBPixel serverParentFBPixel = mutableData.getValue(FBPixel.class);
                        if (serverParentFBPixel == null) {
                            serverParentFBPixel = FBPixel.emptyPixel();
                        }
                        serverParentFBPixel.copyTo(parentFBPixel);
                        parentFBPixel.copyTo(newParentFBPixel);
                        if (Math.random() < 0.25) {
                            newParentFBPixel.pixelColor = childNewPixel.pixelColor.clone();
                            mutableData.setValue(newParentFBPixel);
                            needUpdate.setSucceed(true);
                        } else {
                            needUpdate.setSucceed(false);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    if (!b) {
                        Log.e(TAG, "transaction failed");
                        return;
                    }
                    updateAncestorBitmapCacheAsync(newParentFBPixel, parentPixelData, (StorageMetadata metadata) -> {
                        latchForAllFinish.countDown();
                    });
                    latchForCurrentUpdateFinish.countDown();
                }
            });
            latchForCurrentUpdateFinish.await();
            if (needUpdate.getSucceed()) {
                return updateParent(newParentFBPixel, parentPixelData, latchForAllFinish);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return childPixelData;
    }

    private void updateAncestorBitmapCacheAsync(FBPixel childNewPixel, PixelData
            childPixelData, Function<StorageMetadata> callback) {
        try {
            if (childPixelData.zoom - Constants.BITMAP_CACHE_RESOLUTION_FACTOR < 0) {
                return;
            }
            PixelData ancestorPixelData = PixelUtils.getAncestorPixelData(childPixelData, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
            String ancestorFirebaseId = ancestorPixelData.getFirebaseId();
            new Thread(() -> {
                Bitmap bitmap = getBitmapSync(ancestorPixelData, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
                int relativeX = childPixelData.x % MathUtils.intPow(2, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
                int relativeY = childPixelData.y % MathUtils.intPow(2, Constants.BITMAP_CACHE_RESOLUTION_FACTOR);
                Log.v(TAG, "updating " + relativeX + "," + relativeY);
                bitmap.setPixel(relativeX, relativeY, BitmapUtils.intColor(childNewPixel.pixelColor));
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
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
