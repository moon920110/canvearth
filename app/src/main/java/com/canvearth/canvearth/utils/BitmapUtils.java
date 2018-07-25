package com.canvearth.canvearth.utils;

import com.canvearth.canvearth.pixel.PixelColor;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class BitmapUtils {
    // This method is for Bitmap.Config.ARGB_8888
    // reference this web page: https://developer.android.com/reference/android/graphics/Bitmap.Config
    public static int intColor(int r, int g, int b, int a) {
        return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    public static int intColor(PixelColor pixelColor) {
        int r = pixelColor.r.intValue();
        int g = pixelColor.g.intValue();
        int b = pixelColor.b.intValue();
        int a = pixelColor.a.intValue();
        return intColor(r, g, b, a);
    }

    public static Bitmap getMutableBitmap(Bitmap immutableBitmap) {

        Bitmap mutableBitmap = Bitmap.createBitmap(immutableBitmap.getWidth(),
                immutableBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mutableBitmapCanvas = new Canvas(mutableBitmap);
        mutableBitmapCanvas.drawBitmap(immutableBitmap, 0, 0, null);
        return mutableBitmap;
    }

    public static Bitmap emptyBitmap(int width, int height) {
        int emptyColor = intColor(0, 0, 0, 0);
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        newBitmap.eraseColor(emptyColor);
        return newBitmap;
    }
}
