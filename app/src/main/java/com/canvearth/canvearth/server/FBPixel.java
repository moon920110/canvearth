package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.PixelColor;

// This class is for Firebase.
public class FBPixel implements Cloneable {
    public PixelColor pixelColor;
    public PixelColor futurePixelColor;

    public FBPixel() {
        // Default constructor required for Firebase
    }

    public FBPixel(PixelColor pixelColor) {
        this.pixelColor = pixelColor;
        this.futurePixelColor = pixelColor;
    }

    public void copyTo(FBPixel FBPixel) {
        FBPixel.pixelColor = this.pixelColor;
        FBPixel.futurePixelColor = this.futurePixelColor;
    }

    public FBPixel clone() throws CloneNotSupportedException {
        FBPixel FBPixel = (FBPixel) super.clone();
        FBPixel.pixelColor = this.pixelColor.clone();
        FBPixel.futurePixelColor = this.futurePixelColor.clone();
        return FBPixel;
    }

    public static FBPixel emptyPixel() {
        return new FBPixel(new PixelColor(0L,0L,0L,0L));
    }
}
