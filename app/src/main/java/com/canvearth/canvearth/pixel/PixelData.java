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

    public boolean isLeaf() {
        return zoom == Constants.LEAF_PIXEL_ZOOM_LEVEL;
    }

    public boolean isRoot() {
        return zoom == 0;
    }
}
