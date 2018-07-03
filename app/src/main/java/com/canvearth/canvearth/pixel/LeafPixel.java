package com.canvearth.canvearth.pixel;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;


public class LeafPixel extends Pixel {
    public String modifiedUserToken;
    public String modifiedTime;
    public Color color;

    public LeafPixel() {
        // Default constructor required for Firebase db
    }

    public LeafPixel(LatLng latLng, String userKey, Date now, Color color) {
        super(latLng, 0);

        this.modifiedUserToken = userKey;
        this.modifiedTime = now.toString();
        this.color = color;
    }
}
