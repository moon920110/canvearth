package com.canvearth.canvearth.server;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.client.PixelEvents;
import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelCoord;
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
    public void watchPixel(final PixelCoord pixelCoord) {
        final String firebaseId = pixelCoord.getFirebaseId();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Pixel4Firebase pixel4Firebase;
                if (pixelCoord.zoom < Constants.LEAF_PIXEL_LEVEL) {
                    pixel4Firebase = dataSnapshot.getValue(Pixel4Firebase.class);
                } else {
                    pixel4Firebase = dataSnapshot.getValue(LeafPixel4Firebase.class);
                }
                watchingPixels.get(firebaseId).setPixel4Firebase(pixel4Firebase);
                PixelEvents.watchingPixelChanged(pixel4Firebase);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "load pixel data:onCancelled", databaseError.toException());
            }
        };
        watchingPixels.put(firebaseId, new WatchingPixel(null, valueEventListener));
        DatabaseUtils.getPixelReference(firebaseId).addValueEventListener(valueEventListener); // TODO check when null value comes
    }

    // Client have to call unwatchPixel when you don't need to track pixel data anymore.
    public void unwatchPixel(PixelCoord pixelCoord) {
        String firebaseId = pixelCoord.getFirebaseId();

        ValueEventListener registeredListener = watchingPixels.get(firebaseId).getValueEventListener();
        DatabaseUtils.getPixelReference(firebaseId).removeEventListener(registeredListener);
        watchingPixels.remove(firebaseId);
    }

    public Pixel4Firebase readPixel(PixelCoord pixelCoord) {
        String firebaseId = pixelCoord.getFirebaseId();
        return watchingPixels.get(firebaseId).getPixel4Firebase();
    }

    // You can read unwatching pixel by this method
    public Pixel4Firebase readPixelInstantly(PixelCoord pixelCoord) throws InterruptedException {
        String firebaseId = pixelCoord.getFirebaseId();
        final Pixel4Firebase pixel4Firebase = Pixel4Firebase.emptyPixel();
        final CountDownLatch latchForFinish = new CountDownLatch(1);
        DatabaseUtils.getPixelReference(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Pixel4Firebase ServerPixel4Firebase = dataSnapshot.getValue(Pixel4Firebase.class);
                if (ServerPixel4Firebase == null) {
                    latchForFinish.countDown();
                } else {
                    ServerPixel4Firebase.copyTo(pixel4Firebase);
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
        return pixel4Firebase;
    }

    // You don't have to watch this pixel (for now).. I'm nervous about performance issue of this method.
    // returns Bitmap which has resolution of 2^resolutionFactor * 2^resolutionFactor
    // TODO cache this when there is performance issue
    // Do you need Async version of this?
    public Bitmap getBitmapSync(PixelCoord pixelCoord, int resolutionFactor) {
        try {
            int resolution = MathUtils.intPow(2, resolutionFactor);
            Bitmap bitmap = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
            int hierarchy = Math.min(Constants.LEAF_PIXEL_LEVEL - pixelCoord.zoom, resolutionFactor);
            int chargeForOnePixel = MathUtils.intPow(2, resolutionFactor - hierarchy);
            ArrayList<PixelCoord> childrenPixelCoord = PixelUtils.getChildrenPixelCoord(pixelCoord, hierarchy);
            for (PixelCoord childPixelCoord: childrenPixelCoord) {
                
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public boolean writePixel(PixelCoord pixelCoord, Color color, @Nullable Runnable callback) {
        try {
            if (!pixelCoord.isLeaf()) {
                throw new Exception("Pixel is not leaf");
            }
            String firebaseId = pixelCoord.getFirebaseId();
            // You have to watch pixel before you write it.
            if (!watchingPixels.containsKey(firebaseId)) {
                throw new Exception("Try to write pixel which is not watched");
            }
            Pixel4Firebase originalPixel = watchingPixels.get(firebaseId).getPixel4Firebase();
            UserInformation userInformation = UserInformation.getInstance();
            String userToken = userInformation.getToken();
            LeafPixel4Firebase newPixel = new LeafPixel4Firebase(color, userToken, new Date()); // TODO consider when timezone differs, or abusing current datetime
            final CountUpDownLatch latchForAllFinish = new CountUpDownLatch(1);
            DatabaseUtils.getPixelReference(firebaseId).setValue(newPixel, (@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference)->{
                Log.v(TAG, "setValue finished");
                latchForAllFinish.countDown();
            }); // TODO transaction based on time / push uid
            updateParent(originalPixel, newPixel, pixelCoord, latchForAllFinish);
            new Thread(()->{
                try {
                    latchForAllFinish.await();
                    if (callback != null) {
                        callback.run();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    private void updateParent(Pixel4Firebase childOriginPixel, Pixel4Firebase childNewPixel,
                              PixelCoord childPixelCoord, final CountUpDownLatch latchForAllFinish) {
        if (childPixelCoord.isRoot()) {
            return;
        }
        try {
            PixelCoord parentPixelCoord = PixelUtils.getParentPixelCoord(childPixelCoord);
            Pixel4Firebase parentPixel = readPixelInstantly(parentPixelCoord);
            Pixel4Firebase newParentPixel = parentPixel.clone();
            newParentPixel.futureColor.replaceColorPortion(childOriginPixel.color, childNewPixel.color, 0.25);
            if (Color.areDifferent(newParentPixel.color, newParentPixel.futureColor)) {
                newParentPixel.color = newParentPixel.futureColor.clone();
                latchForAllFinish.countUp();
                String parentId = parentPixelCoord.getFirebaseId();
                DatabaseUtils.getPixelReference(parentId).setValue(newParentPixel,
                        (@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) -> {
                            Log.v(TAG, "setValue finished");
                            latchForAllFinish.countDown();
                        });
                updateParent(parentPixel, newParentPixel, parentPixelCoord, latchForAllFinish);
            } else {
                Log.v(TAG, "Update canceled - original color is " + newParentPixel.color.toString()
                        + " , future color is " + newParentPixel.futureColor.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
