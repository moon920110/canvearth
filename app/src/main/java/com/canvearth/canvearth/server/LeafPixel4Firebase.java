package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.Color;

import java.util.Date;

// TODO Change this data structure
public class LeafPixel4Firebase extends Pixel4Firebase {
    public String modifiedUserToken;
    public String modifiedTime;

    public LeafPixel4Firebase() {
        // Default constructor required for Firebase db
    }

    public LeafPixel4Firebase(Color color, String userToken, Date now) {
        super(color);
        this.modifiedUserToken = userToken;
        this.modifiedTime = now.toString();
    }
}
