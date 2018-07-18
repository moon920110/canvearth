package com.canvearth.canvearth.utils;

import android.graphics.Color;

public class Constants {
    /* develop infra related */
    public static String FIREBASE_DEV_PREFIX = "DEV";
    public static String FIREBASE_PROD_PREFIX = "PROD";

    /* pixel logic related */
    public static int LEAF_PIXEL_GRID_ZOOM_LEVEL = 20;
    public static int BITMAP_SHOW_GRID_ZOOM_LEVEL = 15;
    public static int GRID_SHOW_MAX_CAM_ZOOM_LEVEL = 18;
    public static int GRID_SHOW_MIN_CAM_ZOOM_LEVEL = 13;
    public static int VIEW_GRID_ZOOM_DIFF = 3;
    public static int RESGISTRATION_ZOOM_LEVEL = 14;
    public static int BITMAP_CACHE_RESOLUTION_FACTOR = 5;
    public static int BITMAP_PNG_MAX_BYTES = 1024 * 1024; // 1 megabyte

    /* color related */
    // we regard a color is transparent when its alpha is lower than below.
    public static int COLOR_TRANSPARENT_BOUND = 10;
    // we regard two colors are different when their diffsum is larger than below.
    public static int COLOR_DIFFERENT_BOUND = 20;

    /* drawing related */
    public static float PIX_STROKE_WIDTH = 3;
    public static int PIX_STROKE_VISIBLE_COLOR = Color.argb(50, 100, 100, 100);
    public static int PIX_STROKE_INVISIBLE_COLOR = Color.argb(0, 0, 0, 0);
    public static int PALETTE_DEFAULT_COLOR = Color.argb(100, 255, 255, 255);
    public static int RED_COLOR = 0xFFFF0000;
    public static int ORANGE_COLOR = 0xFFFF7F00;
    public static int YELLOW_COLOR = 0xFFFFFF00;
    public static int GREEN_COLOR = 0xFF00FF00;
    public static int BLUE_COLOR = 0xFF0000FF;
    public static int INDIGO_COLOR = 0xFF4B0082;
    public static int PURPLE_COLOR = 0xFF9400D3;


}
