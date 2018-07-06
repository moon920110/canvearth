package com.canvearth.canvearth.server;

import android.util.Log;

import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;

public class WatchingPixel {
    private static final String TAG = "WatchingPixel";
    private Pixel4Firebase mPixel4Firebase;
    private ValueEventListener mValueEventListener;
    private CountDownLatch checkFetchedDone;

    public WatchingPixel(Pixel4Firebase pixel4Firebase, ValueEventListener valueEventListener) {
        this.mPixel4Firebase = pixel4Firebase;
        this.mValueEventListener = valueEventListener;
        this.checkFetchedDone = new CountDownLatch(1);
    }

    public Pixel4Firebase getPixel4Firebase() {
        try {
            this.checkFetchedDone.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        if (mPixel4Firebase == null) {
            return Pixel4Firebase.emptyPixel();
        }
        return mPixel4Firebase;
    }

    public void setPixel4Firebase(Pixel4Firebase pixel4Firebase) {
        mPixel4Firebase = pixel4Firebase;
        this.checkFetchedDone.countDown();
    }

    public ValueEventListener getValueEventListener() {
        return mValueEventListener;
    }
}
