package com.canvearth.canvearth.utils;

import android.graphics.Color;

public class Constants {
    /* develop infra related */
    public final static String FIREBASE_DEV_PREFIX = "DEV";
    public final static String FIREBASE_PROD_PREFIX = "PROD";

    /* pixel logic related */
    public final static int BITMAP_CACHE_RESOLUTION_FACTOR = 4;
    public final static int BITMAP_PNG_MAX_BYTES = 1024 * 1024; // 1 megabyte
    public final static int LEAF_PIXEL_GRID_ZOOM_LEVEL = 20;
    public final static int BITMAP_SHOW_MAX_GRID_ZOOM_LEVEL = LEAF_PIXEL_GRID_ZOOM_LEVEL - BITMAP_CACHE_RESOLUTION_FACTOR;
    public final static int BITMAP_SHOW_MIN_GRID_ZOOM_LEVEL = BITMAP_SHOW_MAX_GRID_ZOOM_LEVEL - 2;
    public final static int GRID_SHOW_MAX_CAM_ZOOM_LEVEL = 18;
    public final static int GRID_SHOW_MIN_CAM_ZOOM_LEVEL = 13;
    public final static int VIEW_GRID_ZOOM_DIFF = 3;
    public final static int REGISTRATION_ZOOM_LEVEL = 14;
    public final static int REGISTRATION_GRID_ZOOM_LEVEL = 17;

    /* color related */
    // we regard a color is transparent when its alpha is lower than below.
    public final static int COLOR_TRANSPARENT_BOUND = 10;
    // we regard two colors are different when their diffsum is larger than below.
    public final static int COLOR_DIFFERENT_BOUND = 20;

    /* drawing related */
    public final static float PIX_STROKE_WIDTH = 3;
    public final static int PIX_STROKE_VISIBLE_COLOR = Color.argb(50, 100, 100, 100);
    public final static int PIX_STROKE_INVISIBLE_COLOR = Color.argb(0, 0, 0, 0);
    public final static int PALETTE_DEFAULT_COLOR = Color.argb(100, 255, 255, 255);

    /* navigation drawer related */
    public final static int DRAWER_ID_SHARE = 1;
    public final static int DRAWER_ID_STARRED_SKETCHES = 2;
    public final static int DRAWER_ID_NEARBY_SKETCHES = 3;
    public final static int DRAWER_ID_SIGNOUT = 4;
    public final static String DRAWER_TEXT_SHARE = "Share on Facebook";
    public final static String DRAWER_TEXT_STARRED_SKETCHES = "Starred sketches";
    public final static String DRAWER_TEXT_NEARBY_SKETCHES = "Show nearby sketches";
    public final static String DRAWER_TEXT_SIGNOUT = "Sign out";

    /* user related */
    public final static String userName = "user_name";
    public final static String userEmail = "user_email";
    public final static String userPhotoUrl = "user_photo_url";
}
