package com.canvearth.canvearth.server;

import com.google.firebase.database.ValueEventListener;

public class WatchingPixel {
    public Pixel4Firebase mPixel4Firebase;
    public ValueEventListener mValueEventListener;

    public WatchingPixel(Pixel4Firebase pixel4Firebase, ValueEventListener valueEventListener) {
        this.mPixel4Firebase = pixel4Firebase;
        this.mValueEventListener = valueEventListener;
    }

    public Pixel4Firebase getPixel4Firebase() {
        return mPixel4Firebase;
    }

    public void setPixel4Firebase(Pixel4Firebase pixel4Firebase) {
        mPixel4Firebase = pixel4Firebase;
    }

    public ValueEventListener getValueEventListener() {
        return mValueEventListener;
    }
}
