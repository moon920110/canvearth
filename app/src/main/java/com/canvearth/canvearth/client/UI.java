package com.canvearth.canvearth.client;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public final class UI
{
    private static float s_density;
    private static float s_scaledDensity;
    private static int s_displayWidth;
    private static int s_displayHeight;

    public static void init(Context context)
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();

        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);

        s_density = displayMetrics.density;
        s_scaledDensity = displayMetrics.scaledDensity;
        s_displayWidth = displayMetrics.widthPixels;
        s_displayHeight = displayMetrics.heightPixels;
    }

    public static int getDisplayWidth()
    {
        return s_displayWidth;
    }

    public static int getDisplayHeight()
    {
        return s_displayHeight;
    }

    public static float dp2px(float dp)
    {
        return dp * s_density;
    }

    public static int dp2px_r(float dp)
    {
        return Math.round(dp2px(dp));
    }

    public static float sp2px(float sp)
    {
        return sp * s_scaledDensity;
    }
}