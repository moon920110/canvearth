package com.canvearth.canvearth.pixel;

import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.google.android.gms.maps.model.LatLng;
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

    public PixelCoord readPixel(PixelCoord pixelCoord, int zoomLevel) {
        //TODO
        return new PixelCoord();
    }

    public boolean writePixel(PixelCoord pixelCoord, Color color) {
        assert(pixelCoord.isLeaf());
        UserInformation userInformation = UserInformation.getInstance();
        try {
            String userToken = userInformation.getToken();
            LeafPixel4Firebase newPixel = new LeafPixel4Firebase(userToken, new Date(), color); // TODO consider when timezone differs, or abusing current datetime
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child(newPixel.getFirebaseId()).setValue(newPixel); // TODO transaction based on time / push uid
            //TODO update parent pixels
        } catch (TimeoutException e) {
            Log.e(TAG, e.getMessage());
        }
        return true;
    }
}
