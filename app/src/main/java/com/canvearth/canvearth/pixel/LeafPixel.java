package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;


public class LeafPixel extends Pixel {
    public String modifiedUserToken;
    public String modifiedTime;
    public Color color;

    public LeafPixel() {
        // Default constructor required for Firebase db
    }

    public LeafPixel(int x, int y, String userToken, Date now, Color color) {
        super(x, y, Constants.LEAF_PIXEL_LEVEL);

        this.modifiedUserToken = userToken;
        this.modifiedTime = now.toString();
        this.color = color;
    }
}
