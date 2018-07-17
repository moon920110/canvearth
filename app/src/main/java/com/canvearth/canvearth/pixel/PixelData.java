package com.canvearth.canvearth.pixel;

import android.util.Log;

import com.canvearth.canvearth.utils.Constants;

public class PixelData {
    private static String TAG = "PixelData";
    public int x;
    public int y;
    public int zoom;

    public PixelData(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public void copyFrom(PixelData pixelData) {
        this.x = pixelData.x;
        this.y = pixelData.y;
        this.zoom = pixelData.zoom;
    }

    public String getFirebaseId() {
        return Integer.toString(zoom) + "," + Integer.toString(x) + "," + Integer.toString(y);
    }

    public boolean isLeaf() {
        return zoom == Constants.LEAF_PIXEL_ZOOM_LEVEL;
    }

    public boolean isRoot() {
        return zoom == 0;
    }

    public boolean equals(PixelData pixelData) {
        return x == pixelData.x && y == pixelData.y && zoom == pixelData.zoom;
    }

    public PixelData clone() {
        try {
            PixelData pixelData = (PixelData)super.clone();
            pixelData.copyFrom(this);
            return pixelData;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
