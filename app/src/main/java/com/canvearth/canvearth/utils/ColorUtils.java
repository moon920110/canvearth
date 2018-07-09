package com.canvearth.canvearth.utils;

import com.canvearth.canvearth.pixel.PixelColor;

public class ColorUtils {
    public static PixelColor colorCompose(PixelColor pixelColor1, PixelColor pixelColor2) {
        // Can cause issue -- to be changed [0, 255] to [0., 1.] later sometime.
        PixelColor newPixelColor = new PixelColor();
        double portion = pixelColor1.a / (pixelColor1.a + pixelColor2.a);
        newPixelColor.r = (long)(pixelColor1.r * portion + pixelColor2.r * (1 - portion));
        newPixelColor.g = (long)(pixelColor1.g * portion + pixelColor2.g * (1 - portion));
        newPixelColor.b = (long)(pixelColor1.b * portion + pixelColor2.b * (1 - portion));
        newPixelColor.a = (pixelColor1.a + pixelColor2.a) / 2;
        return newPixelColor;
    }

    // TODO this logic is wierd
    public static boolean areDifferent(PixelColor pixelColor1, PixelColor pixelColor2) {
        int diffSum = (int)(Math.abs(pixelColor1.r - pixelColor2.r)
                + Math.abs(pixelColor1.g - pixelColor2.g)
                + Math.abs(pixelColor1.b - pixelColor2.b)
                + Math.abs(pixelColor1.a - pixelColor2.a));
        return (diffSum > Constants.COLOR_DIFFERENT_BOUND);
    }
}
