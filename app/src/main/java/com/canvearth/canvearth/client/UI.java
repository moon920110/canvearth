package com.canvearth.canvearth.client;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public final class UI {
    private static float s_density;
    private static float s_scaledDensity;
    private static int s_displayWidth;
    private static int s_displayHeight;

    //=========================================================================
    // Public Methods
    //=========================================================================

    public static void init(Context context) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();

        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);

        s_density = displayMetrics.density;
        s_scaledDensity = displayMetrics.scaledDensity;
        s_displayWidth = displayMetrics.widthPixels;
        s_displayHeight = displayMetrics.heightPixels;
    }

    public static int getDisplayWidth() {
        return s_displayWidth;
    }

    public static int getDisplayHeight() {
        return s_displayHeight;
    }

    public static float dp2px(float dp) {
        return dp * s_density;
    }

    public static int dp2px_r(float dp) {
        return Math.round(dp2px(dp));
    }

    public static float sp2px(float sp) {
        return sp * s_scaledDensity;
    }


    public static Point getDisplaySize(Context context) {
        final DisplayMetrics displayMetrics = getDisplayMetrics(context);

        return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Point getRealDisplaySize(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final DisplayMetrics displayMetrics = getRealDisplayMetrics(context);

            return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }

        final Display display = getWindowManager(context).getDefaultDisplay();

        try {
            final Method rawCxMethod = Display.class.getMethod("getRawWidth");
            final int rawCx = (Integer) rawCxMethod.invoke(display);

            final Method rawCyMethod = Display.class.getMethod("getRawHeight");
            final int rawCy = (Integer) rawCyMethod.invoke(display);

            return new Point(rawCx, rawCy);
        } catch (Exception e) {
            return getDisplaySize(context);
        }
    }

    public static int getPixelFromDp(Context context, float dp) {
        return (int) getPixelFromDpRaw(context, dp);
    }

    public static int getPixelFromSp(Context context, float sp) {
        return (int) getPixelFromSpRaw(context, sp);
    }

    public static float getPixelFromDpRaw(Context context, float dp) {
        return applyDimension(context, TypedValue.COMPLEX_UNIT_DIP, dp);
    }

    public static float getPixelFromSpRaw(Context context, float sp) {
        return applyDimension(context, TypedValue.COMPLEX_UNIT_SP, sp);
    }

    //=========================================================================
    // Private Methods
    //=========================================================================

    private static WindowManager getWindowManager(Context context) {
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static DisplayMetrics getRealDisplayMetrics(Context context) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private static float applyDimension(Context context, int unit, float value) {
        final DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return TypedValue.applyDimension(unit, value, displayMetrics);
    }
}