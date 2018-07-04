package com.canvearth.canvearth.pixel;

import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.concurrent.TimeoutException;

public class PixelDataManager {
    private static final PixelDataManager ourInstance = new PixelDataManager();
    private static final String TAG = "PixelDataManager";
    public static PixelDataManager getInstance() {
        return ourInstance;
    }

    private PixelDataManager() {
    }

    public Pixel readPixel(LatLng latLng, int zoomLevel) {
        //TODO
        return new Pixel();
    }

    public boolean writePixel(LatLng latLng, Color color) {
        UserInformation userInformation = UserInformation.getInstance();
        try {
            String userToken = userInformation.getToken();
            LeafPixel newPixel = new LeafPixel(latLng, userToken, new Date(), color); // TODO consider when timezone differs, or abusing current datetime
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child(newPixel.getPixelId()).setValue(newPixel);
        } catch (TimeoutException e) {
            Log.e(TAG, e.getMessage());
        }
        //TODO update parent pixels
        return true;
    }
}
