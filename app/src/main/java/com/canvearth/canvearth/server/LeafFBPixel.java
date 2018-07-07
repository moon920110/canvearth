package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.Color;

import java.util.Date;

// TODO Change this data structure
public class LeafFBPixel extends FBPixel {
    public String modifiedUserToken;
    public String modifiedTime;

    public LeafFBPixel() {
        // Default constructor required for Firebase db
    }

    public LeafFBPixel(Color color, String userToken, Date now) {
        super(color);
        this.modifiedUserToken = userToken;
        this.modifiedTime = now.toString();
    }
}
