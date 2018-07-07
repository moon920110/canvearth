package com.canvearth.canvearth.utils;

import android.graphics.Color;

public class Constants {
    public static int LEAF_PIXEL_ZOOM_LEVEL = 20;
    public static int GRID_SHOW_MAX_ZOOM_LEVEL = 18;
    public static int GRID_SHOW_MIN_ZOOM_LEVEL = 14;
    public static int VIEW_GRID_ZOOM_DIFF = 3;
    // we regard a color is transparent when its alpha is lower than below.
    public static int COLOR_TRANSPARENT_BOUND = 10;

    // we regard two colors are different when their diffsum is larger than below.
    public static int COLOR_DIFFERENT_BOUND = 20;
    public static float PIX_STROKE_WIDTH = 3;
    public static int PIX_STROKE_COLOR = Color.argb(50, 100, 100, 100);
    public static int PIX_COLOR_RED = Color.argb(100, 255, 0, 0);

    public static String FIREBASE_DEV_PREFIX = "DEV";
    public static String FIREBASE_PROD_PREFIX = "PROD";
}
