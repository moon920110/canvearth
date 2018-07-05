package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.Color;

import java.util.Date;

// TODO Change this data structure
public class LeafPixel4Firebase extends Pixel4Firebase {
    public String modifiedUserToken;
    public String modifiedTime;
    public Color color;

    public LeafPixel4Firebase() {
        // Default constructor required for Firebase db
    }

    public LeafPixel4Firebase(String userToken, Date now, Color color) {
        super();
        this.modifiedUserToken = userToken;
        this.modifiedTime = now.toString();
        this.color = color;
    }
}
