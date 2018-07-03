package com.canvearth.canvearth.pixel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class PixelDataManager {
    private static final PixelDataManager ourInstance = new PixelDataManager();
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert(user != null);
        LeafPixel newPixel = new LeafPixel(latLng, "TODO_USERKEY", new Date(), color); // TODO consider when timezone differs, or abusing current datetime

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child(newPixel.getPixelId()).setValue(newPixel);
        //TODO update parent pixels
        return true;
    }
}
