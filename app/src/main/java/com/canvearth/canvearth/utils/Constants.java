package com.canvearth.canvearth.utils;

import android.graphics.Color;

public class Constants {
    /* develop infra related */
    public static String FIREBASE_DEV_PREFIX = "DEV";
    public static String FIREBASE_PROD_PREFIX = "PROD";

    /* pixel logic related */
    public static int LEAF_PIXEL_ZOOM_LEVEL = 20;
    public static int GRID_SHOW_MAX_ZOOM_LEVEL = 18;
    public static int GRID_SHOW_MIN_ZOOM_LEVEL = 14;
    public static int VIEW_GRID_ZOOM_DIFF = 3;
    public static int BITMAP_CACHE_RESOLUTION_FACTOR = 5;
    public static int BITMAP_PNG_MAX_BYTES = 1024 * 1024; // 1 megabyte

    /* color related */
    // we regard a color is transparent when its alpha is lower than below.
    public static int COLOR_TRANSPARENT_BOUND = 10;
    // we regard two colors are different when their diffsum is larger than below.
    public static int COLOR_DIFFERENT_BOUND = 20;

    /* drawing related */
    public static float PIX_STROKE_WIDTH = 3;
    public static int PIX_STROKE_COLOR = Color.argb(50, 100, 100, 100);
    public static int PIX_COLOR_RED = Color.argb(100, 255, 0, 0);

}
