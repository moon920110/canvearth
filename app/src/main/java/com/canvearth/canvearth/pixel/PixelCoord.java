package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.utils.Constants;

public class PixelCoord {
    public int x;
    public int y;
    public int zoom;

    //TODO image member variable needed

    public PixelCoord() {
        // Default constructor required for Firebase db
    }

    public PixelCoord(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public boolean isLeaf() {
        return zoom == Constants.LEAF_PIXEL_LEVEL;
    }

    public String getFirebaseId() { // This method have to be synchronized with Pixel4Firebase.getFirebaseId().
        return Integer.toString(zoom) + "," + Integer.toString(x) + "," + Integer.toString(y);
    }
}
