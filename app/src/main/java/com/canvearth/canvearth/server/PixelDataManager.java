package com.canvearth.canvearth.server;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.client.PixelEvents;
import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelCoord;
import com.canvearth.canvearth.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class PixelDataManager {
    private static final PixelDataManager ourInstance = new PixelDataManager();
    private static final String TAG = "PixelDataManager";
    public static PixelDataManager getInstance() {
        return ourInstance;
    }

    private ArrayList<Pair<String, ValueEventListener>> watchingListeners = new ArrayList<>();
    private PixelDataManager() {
    }

    // Client have to call watchPixel to keep track pixel data.
    public void watchPixel(final PixelCoord pixelCoord) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String firebaseid = pixelCoord.getFirebaseId();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Pixel4Firebase pixel4Firebase;
                if (pixelCoord.zoom < Constants.LEAF_PIXEL_LEVEL) {
                    pixel4Firebase = dataSnapshot.getValue(Pixel4Firebase.class);
                } else {
                    pixel4Firebase = dataSnapshot.getValue(LeafPixel4Firebase.class);
                }
                PixelEvents.watchingPixelChanged(pixel4Firebase);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "load pixel data:onCancelled", databaseError.toException());
            }
        };
        watchingListeners.add(new Pair<>(firebaseid, valueEventListener));
        database.child(firebaseid).addValueEventListener(valueEventListener);
    }

    // Client have to call unwatchPixel when you don't need to track pixel data anymore.
    public void unwatchPixel(PixelCoord pixelCoord) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String firebaseid = pixelCoord.getFirebaseId();
        for (Pair<String, ValueEventListener> listenerPair: watchingListeners) {
            if (listenerPair.first.equals(firebaseid)) {
                database.removeEventListener(listenerPair.second);
                return;
            }
        }
        Log.e(TAG, "unwatchPixel called for not watching pixel");
    }

    public boolean writePixel(PixelCoord pixelCoord, Color color) {
        assert(pixelCoord.isLeaf());
        UserInformation userInformation = UserInformation.getInstance();
        try {
            String userToken = userInformation.getToken();
            LeafPixel4Firebase newPixel = new LeafPixel4Firebase(color, userToken, new Date()); // TODO consider when timezone differs, or abusing current datetime
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child(newPixel.getFirebaseId()).setValue(newPixel); // TODO transaction based on time / push uid
            updateParent(newPixel);
        } catch (TimeoutException e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    private void updateParent(Pixel4Firebase childPixel) {

    }
}
