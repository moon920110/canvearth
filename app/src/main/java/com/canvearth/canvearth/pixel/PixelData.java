package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.utils.Constants;

public class PixelData {
    public int x;
    public int y;
    public int zoom;
    public String firebaseId;

    public PixelData(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.firebaseId = Integer.toString(zoom) + "," + Integer.toString(x) + "," + Integer.toString(y);
    }

    public void copyFrom(PixelData pixelData) {
        this.x = pixelData.x;
        this.y = pixelData.y;
        this.zoom = pixelData.zoom;
        this.firebaseId = pixelData.firebaseId;
    }

    public boolean isLeaf() {
        return zoom == Constants.LEAF_PIXEL_ZOOM_LEVEL;
    }

    public boolean isRoot() {
        return zoom == 0;
    }
}
