package com.canvearth.canvearth.utils;

import com.canvearth.canvearth.pixel.Color;

public class ColorUtils {
    public static Color colorCompose(Color color1, Color color2) {
        // Can cause issue -- to be changed [0, 255] to [0., 1.] later sometime.
        Color newColor = new Color();
        double portion = color1.a / (color1.a + color2.a);
        newColor.r = (long)(color1.r * portion + color2.r * (1 - portion));
        newColor.g = (long)(color1.g * portion + color2.g * (1 - portion));
        newColor.b = (long)(color1.b * portion + color2.b * (1 - portion));
        newColor.a = (color1.a + color2.a) / 2;
        return newColor;
    }

    // TODO this logic is wierd
    public static boolean areDifferent(Color color1, Color color2) {
        int diffSum = (int)(Math.abs(color1.r - color2.r)
                + Math.abs(color1.g - color2.g)
                + Math.abs(color1.b - color2.b)
                + Math.abs(color1.a - color2.a));
        return (diffSum > Constants.COLOR_DIFFERENT_BOUND);
    }
}
