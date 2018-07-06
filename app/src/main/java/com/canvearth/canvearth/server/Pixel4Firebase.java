package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.Color;

// This class is for Firebase.
public class Pixel4Firebase implements Cloneable {
    public Color color;
    public Color futureColor;

    public Pixel4Firebase() {
        // Default constructor required for Firebase
    }

    public Pixel4Firebase(Color color) {
        this.color = color;
        this.futureColor = color;
    }

    public void copyTo(Pixel4Firebase pixel4Firebase) {
        pixel4Firebase.color = this.color;
        pixel4Firebase.futureColor = this.futureColor;
    }

    public Pixel4Firebase clone() throws CloneNotSupportedException {
        Pixel4Firebase pixel4Firebase = (Pixel4Firebase) super.clone();
        pixel4Firebase.color = this.color.clone();
        pixel4Firebase.futureColor = this.futureColor.clone();
        return pixel4Firebase;
    }

    public static Pixel4Firebase emptyPixel() {
        return new Pixel4Firebase(new Color(0L,0L,0L,0L));
    }
}
