package com.canvearth.canvearth.pixel;

public class Pixel {
    public int x;
    public int y;
    public int zoom;

    //TODO image member variable needed

    public Pixel() {
        // Default constructor required for Firebase db
    }

    public Pixel(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public String getPixelId() {
        return Integer.toString(x) + "," + Integer.toString(y) + "," + Integer.toString(zoom);
    }
}
