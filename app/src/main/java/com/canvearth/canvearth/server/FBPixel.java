package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.Color;

// This class is for Firebase.
public class FBPixel implements Cloneable {
    public Color color;
    public Color futureColor;

    public FBPixel() {
        // Default constructor required for Firebase
    }

    public FBPixel(Color color) {
        this.color = color;
        this.futureColor = color;
    }

    public void copyTo(FBPixel FBPixel) {
        FBPixel.color = this.color;
        FBPixel.futureColor = this.futureColor;
    }

    public FBPixel clone() throws CloneNotSupportedException {
        FBPixel FBPixel = (FBPixel) super.clone();
        FBPixel.color = this.color.clone();
        FBPixel.futureColor = this.futureColor.clone();
        return FBPixel;
    }

    public static FBPixel emptyPixel() {
        return new FBPixel(new Color(0L,0L,0L,0L));
    }
}
