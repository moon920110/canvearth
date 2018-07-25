package com.canvearth.canvearth.pixel;

import android.graphics.Color;

import com.canvearth.canvearth.utils.Constants;

public class PixelColor implements Cloneable {
    public Long r;
    public Long g;
    public Long b;
    public Long a;

    public PixelColor() {
        // Default constructor required for Firebase db
    }

    public PixelColor(Long r, Long g, Long b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255L;
    }

    public PixelColor(Long r, Long g, Long b, Long a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public PixelColor(int color) {
        this.r = Long.valueOf(Color.red(color));
        this.g = Long.valueOf(Color.green(color));
        this.b = Long.valueOf(Color.blue(color));
        this.a = Long.valueOf(Color.alpha(color));
    }

    public PixelColor clone() throws CloneNotSupportedException {
        PixelColor pixelColor = (PixelColor) super.clone();
        pixelColor.r = this.r.longValue();
        pixelColor.g = this.g.longValue();
        pixelColor.b = this.b.longValue();
        pixelColor.a = this.a.longValue();
        return pixelColor;
    }

    public boolean equals(PixelColor pixelColor) {
        return this.r.longValue() == pixelColor.r.longValue()
                && this.g.longValue() == pixelColor.g.longValue()
                && this.b.longValue() == pixelColor.b.longValue()
                && this.a.longValue() == pixelColor.a.longValue();
    }

    public int convertIntColor() {
        byte[] colorByteArr = {a.byteValue(), r.byteValue(), g.byteValue(), b.byteValue()};
        return byteArrToInt(colorByteArr);
    }


    private int byteArrToInt(byte[] colorByteArr) {
        return (colorByteArr[0] << 24) + ((colorByteArr[1] & 0xFF) << 16) + ((colorByteArr[2] & 0xFF) << 8) + (colorByteArr[3] & 0xFF);
    }

    public boolean transparent() {
        return (this.a < Constants.COLOR_TRANSPARENT_BOUND);
    }

    public String toString() {
        return "rgba(" + r + ", " + g + ", " + b + ", " + a + ")";
    }
}
