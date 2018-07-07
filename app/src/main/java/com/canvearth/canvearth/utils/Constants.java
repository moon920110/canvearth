package com.canvearth.canvearth.utils;

public class Constants {
    public static int LEAF_PIXEL_LEVEL = 20; // TODO to be fixed

    // we regard a color is transparent when its alpha is lower than below.
    public static int COLOR_TRANSPARENT_BOUND = 10;

    // we regard two colors are different when their diffsum is larger than below.
    public static int COLOR_DIFFERENT_BOUND = 20;

    public static String FIREBASE_DEV_PREFIX = "DEV";
    public static String FIREBASE_PROD_PREFIX = "PROD";
}
