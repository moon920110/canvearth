package com.canvearth.canvearth.pixel;

public class FBPixelData {
    public Integer x;
    public Integer y;
    public Integer zoom;

    public FBPixelData() {
    }

    public FBPixelData(PixelData pixelData) {
        this.x = pixelData.x;
        this.y = pixelData.y;
        this.zoom = pixelData.zoom;
    }

    public PixelData toPixelData() {
        return new PixelData(x, y, zoom);
    }
}
