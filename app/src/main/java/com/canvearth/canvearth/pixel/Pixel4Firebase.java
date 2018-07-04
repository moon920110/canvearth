package com.canvearth.canvearth.pixel;

// This class is for Firebase.
public class Pixel4Firebase {
    Long zoom;
    Long x;
    Long y;

    public Pixel4Firebase() {
        // Default constructor required for Firebase
    }

    public String getFirebaseId() {
        return zoom.toString() + "," + x.toString() + "," + y.toString();
    }
}
