package com.canvearth.canvearth.utils;

import com.canvearth.canvearth.pixel.PixelColor;

public class BitmapUtils {
    // This method is for Bitmap.Config.ARGB_8888
    // reference this web page: https://developer.android.com/reference/android/graphics/Bitmap.Config
    public static int intColor(int r, int g, int b, int a) {
        return (a & 0xff) << 24 | (b & 0xff) << 16 | (g & 0xff) << 8 | (r & 0xff);
    }

    public static int intColor(PixelColor pixelColor) {
        int r = pixelColor.r.intValue();
        int g = pixelColor.g.intValue();
        int b = pixelColor.b.intValue();
        int a = pixelColor.a.intValue();
        return intColor(r, g, b, a);
    }
}
