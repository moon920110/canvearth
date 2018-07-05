package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.utils.Constants;

public class Color {
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

    public Color clone() throws CloneNotSupportedException {
        Color color = (Color) super.clone();
        color.r = this.r.longValue();
        color.g = this.g.longValue();
        color.b = this.b.longValue();
        color.a = this.a.longValue();
        return color;
    }

    public boolean isTransparent() {
        return (this.a < Constants.COLOR_TRANSPARENT_BOUND) ;

    }

    public void replaceColorPortion(Color originColor, Color newColor, double portion) {
        this.r = this.r + (long) ((newColor.r - originColor.r) * portion);
        this.g = this.g + (long) ((newColor.r - originColor.r) * portion);
        this.b = this.b + (long) ((newColor.r - originColor.r) * portion);
        this.a = this.a + (long) ((newColor.r - originColor.r) * portion);
    }

    public static Color colorCompose(Color color1, Color color2) {
        // Can cause issue -- to be changed [0, 255] to [0., 1.] later sometime.
        Color newColor = new Color();
        newColor.r = (color1.r + color2.r) / 2;
        newColor.g = (color1.g + color2.g) / 2;
        newColor.b = (color1.b + color2.b) / 2;
        newColor.a = (color1.a + color2.a) / 2;
        return newColor;
    }

    public static boolean areDifferent(Color color1, Color color2) {
        int diffSum = (int)(Math.abs(color1.r - color2.r)
                + Math.abs(color1.g - color2.g)
                + Math.abs(color1.b - color2.b)
                + Math.abs(color1.a - color2.a));
        return (diffSum > Constants.COLOR_DIFFERENT_BOUND);
    }
}
