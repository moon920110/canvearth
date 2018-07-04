package com.canvearth.canvearth.pixel;

public class Pixel {
    public Long x;
    public Long y;
    public Long level;

    //TODO image member variable needed

    public Pixel() {
        // Default constructor required for Firebase db
    }

    public Pixel(int x, int y, int level) {
        this.x = (long)x;
        this.y = (long)y;
        this.level = (long)level;
    }

    public String getPixelId() {
        //TODO
        return "pixel-id";
    }
}
