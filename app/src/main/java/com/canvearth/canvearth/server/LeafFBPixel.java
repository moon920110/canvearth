package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.PixelColor;

import java.util.Date;

// TODO Change this data structure
public class LeafFBPixel extends FBPixel {
    public String modifiedUserToken;
    public String modifiedTime;

    public LeafFBPixel() {
        // Default constructor required for Firebase db
    }

    public LeafFBPixel(PixelColor pixelColor, String userToken, Date now) {
        super(pixelColor);
        this.modifiedUserToken = userToken;
        this.modifiedTime = now.toString();
    }
}
