package com.canvearth.canvearth.pixel;

public class FBPixelDataSquare {
    public FBPixelData leftTop;
    public FBPixelData rightBottom;

    public FBPixelDataSquare() {

    }

    public FBPixelDataSquare(FBPixelData leftTop, FBPixelData rightBottom) {
        this.leftTop = leftTop;
        this.rightBottom = rightBottom;
    }

    public PixelDataSquare toPixelDataSquare() {
        return new PixelDataSquare(leftTop.toPixelData(), rightBottom.toPixelData());
    }
}
