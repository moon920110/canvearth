package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.utils.Constants;

public class Color implements Cloneable {
    public Long r;
    public Long g;
    public Long b;
    public Long a;

    public Color() {
        // Default constructor required for Firebase db
    }

    public Color(Long r, Long g, Long b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255L;
    }

    public Color(Long r, Long g, Long b, Long a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color clone() throws CloneNotSupportedException {
        Color color = (Color) super.clone();
        color.r = this.r.longValue();
        color.g = this.g.longValue();
        color.b = this.b.longValue();
        color.a = this.a.longValue();
        return color;
    }

    public boolean equals(Color color) {
        return this.r.longValue() == color.r.longValue()
                && this.g.longValue() == color.g.longValue()
                && this.b.longValue() == color.b.longValue()
                && this.a.longValue() == color.a.longValue();
    }

    public boolean transparent() {
        return (this.a < Constants.COLOR_TRANSPARENT_BOUND) ;
    }

    public String toString() {
        return "rgba(" + r + ", " + g + ", " + b + ", " + a + ")";
    }

    // TODO consider this one more time
    public void replaceColorPortion(Color originColor, Color newColor, double portion) {
        long newA = this.a + (long)(portion * (newColor.a - originColor.a));
        if (newA == 0) {
            this.a = newA;
            return;
        }
        this.r = (this.r * this.a + (long)(portion * (newColor.a * newColor.r - originColor.a * originColor.r))) / newA;
        this.g = (this.g * this.a + (long)(portion * (newColor.a * newColor.g - originColor.a * originColor.g))) / newA;
        this.b = (this.b * this.a + (long)(portion * (newColor.a * newColor.b - originColor.a * originColor.b))) / newA;
        this.a = newA;
    }
}
