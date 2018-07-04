package com.canvearth.canvearth.server;

import com.canvearth.canvearth.pixel.Color;

// This class is for Firebase.
public class Pixel4Firebase {
    Long zoom;
    Long x;
    Long y;

    public Color color;

    public Pixel4Firebase() {
        // Default constructor required for Firebase
    }

    public Pixel4Firebase(Color color) {
        this.color = color;
    }

    public String getFirebaseId() {
        return zoom.toString() + "," + x.toString() + "," + y.toString();
    }
}
