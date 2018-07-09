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

    public boolean transparent() {
        return (this.a < Constants.COLOR_TRANSPARENT_BOUND);
    }

    public String toString() {
        return "rgba(" + r + ", " + g + ", " + b + ", " + a + ")";
    }

    // TODO consider this one more time -- related Issue #19
    // Let's consider the situation when parent pixel's pixelColor A is composed of its children's pixelColor B,C,D,E.
    // When B is changed to B', A have to be changed accordingly.
    // In this situation, we have to call A.replaceColorPortion(B, B', 0.25) (0.25 is because B contributes 0.25 for A)
    public void replaceColorPortion(PixelColor originPixelColor, PixelColor newPixelColor, double portion) {
        long newA = this.a + (long) (portion * (newPixelColor.a - originPixelColor.a));
        if (newA == 0) {
            this.a = newA;
            return;
        }
        this.r = (this.r * this.a + (long) (portion * (newPixelColor.a * newPixelColor.r - originPixelColor.a * originPixelColor.r))) / newA;
        this.g = (this.g * this.a + (long) (portion * (newPixelColor.a * newPixelColor.g - originPixelColor.a * originPixelColor.g))) / newA;
        this.b = (this.b * this.a + (long) (portion * (newPixelColor.a * newPixelColor.b - originPixelColor.a * originPixelColor.b))) / newA;
        this.a = newA;
    }
}
