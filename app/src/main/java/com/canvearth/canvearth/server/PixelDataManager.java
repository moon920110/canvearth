package com.canvearth.canvearth.server;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.client.PixelEvents;
import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelCoord;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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

    public boolean writePixel(PixelCoord pixelCoord, Color color) {
        assert(pixelCoord.isLeaf());
        String firebaseId = pixelCoord.getFirebaseId();
        // You have to watch pixel before you write it.
        assert(watchingPixels.containsKey(firebaseId));
        Pixel4Firebase originalPixel = watchingPixels.get(firebaseId).getPixel4Firebase();
        UserInformation userInformation = UserInformation.getInstance();
        try {
            String userToken = userInformation.getToken();
            LeafPixel4Firebase newPixel = new LeafPixel4Firebase(color, userToken, new Date()); // TODO consider when timezone differs, or abusing current datetime
            DatabaseUtils.getPixelReference(firebaseId).setValue(newPixel); // TODO transaction based on time / push uid
            updateParent(originalPixel, newPixel, pixelCoord);
        } catch (TimeoutException e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    class PixelValueEventListener implements ValueEventListener {
        public Pixel4Firebase pixel4Firebase = null;
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            pixel4Firebase = dataSnapshot.getValue(Pixel4Firebase.class);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.w(TAG, "load pixel data:onCancelled", databaseError.toException());
        }
    }

    private void updateParent(Pixel4Firebase childOriginPixel, Pixel4Firebase childNewPixel, PixelCoord childPixelCoord) {
        if (childPixelCoord.isRoot()) {
            return;
        }
        try {
            PixelCoord parentPixelCoord = PixelUtils.getParentPixelCoord(childPixelCoord);
            String parentId = parentPixelCoord.getFirebaseId();
            PixelValueEventListener instanceListener = new PixelValueEventListener();
            DatabaseUtils.getPixelReference(parentId).addListenerForSingleValueEvent(instanceListener);
            while (instanceListener.pixel4Firebase == null) {
                Thread.sleep(10);
            }
            Pixel4Firebase parentPixel = instanceListener.pixel4Firebase;
            Pixel4Firebase newParentPixel = parentPixel.clone();
            newParentPixel.futureColor.replaceColorPortion(childOriginPixel.color, childNewPixel.color, 0.25);
            if (Color.areDifferent(newParentPixel.color, newParentPixel.futureColor)) {
                newParentPixel.color = newParentPixel.futureColor.clone();
                DatabaseUtils.getPixelReference(parentId).setValue(newParentPixel);
                updateParent(newParentPixel, parentPixel, parentPixelCoord);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
