package com.canvearth.canvearth.server;

import android.util.Log;

import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;

public class WatchingPixel {
    private static final String TAG = "WatchingPixel";
    private FBPixel mFBPixel;
    private ValueEventListener mValueEventListener;
    private CountDownLatch checkFetchedDone;

    public WatchingPixel(FBPixel FBPixel, ValueEventListener valueEventListener) {
        this.mFBPixel = FBPixel;
        this.mValueEventListener = valueEventListener;
        this.checkFetchedDone = new CountDownLatch(1);
    }

    public FBPixel getFBPixel() {
        try {
            this.checkFetchedDone.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        if (mFBPixel == null) {
            return FBPixel.emptyPixel();
        }
        return mFBPixel;
    }

    public void setFBPixel(FBPixel FBPixel) {
        mFBPixel = FBPixel;
        this.checkFetchedDone.countDown();
    }

    public ValueEventListener getValueEventListener() {
        return mValueEventListener;
    }
}
