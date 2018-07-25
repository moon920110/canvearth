package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.PixelColor;

// This class is for Firebase.
public class FBPixel implements Cloneable {
    public PixelColor pixelColor;

    public FBPixel() {
        // Default constructor required for Firebase
    }

    public FBPixel(PixelColor pixelColor) {
        this.pixelColor = pixelColor;
    }

    public void copyTo(FBPixel FBPixel) {
        FBPixel.pixelColor = this.pixelColor;
    }

    public FBPixel clone() throws CloneNotSupportedException {
        FBPixel FBPixel = (FBPixel) super.clone();
        FBPixel.pixelColor = this.pixelColor.clone();
        return FBPixel;
    }

    public static FBPixel emptyPixel() {
        return new FBPixel(new PixelColor(0L,0L,0L,0L));
    }
}
