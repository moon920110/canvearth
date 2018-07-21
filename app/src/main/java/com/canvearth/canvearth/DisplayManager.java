package com.canvearth.canvearth;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public final class DisplayManager
{
    //=========================================================================
    // Singleton
    //=========================================================================

    public static DisplayManager getInstance()
    {
        return INSTANCE;
    }

    //=========================================================================
    // Public Methods
    //=========================================================================

    public Point getDisplaySize(Context context)
    {
        final DisplayMetrics displayMetrics = getDisplayMetrics(context);

        return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
    public Point getRealDisplaySize(Context context)
    {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1)
        {
            final DisplayMetrics displayMetrics = getRealDisplayMetrics(context);

            return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }

        final Display display = getWindowManager(context).getDefaultDisplay();

        try
        {
            final Method rawCxMethod = Display.class.getMethod("getRawWidth");
            final int rawCx = (Integer) rawCxMethod.invoke(display);

            final Method rawCyMethod = Display.class.getMethod("getRawHeight");
            final int rawCy = (Integer) rawCyMethod.invoke(display);

            return new Point(rawCx, rawCy);
        }
        catch (Exception e)
        {
            return getDisplaySize(context);
        }
    }

    public int getPixelFromDp(Context context, float dp)
    {
        return (int) getPixelFromDpRaw(context, dp);
    }

    public int getPixelFromSp(Context context, float sp)
    {
        return (int) getPixelFromSpRaw(context, sp);
    }

    public float getPixelFromDpRaw(Context context, float dp)
    {
        return applyDimension(context, TypedValue.COMPLEX_UNIT_DIP, dp);
    }

    public float getPixelFromSpRaw(Context context, float sp)
    {
        return applyDimension(context, TypedValue.COMPLEX_UNIT_SP, sp);
    }

    //=========================================================================
    // Private Methods
    //=========================================================================

    private WindowManager getWindowManager(Context context)
    {
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    private DisplayMetrics getDisplayMetrics(Context context)
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
    private DisplayMetrics getRealDisplayMetrics(Context context)
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private float applyDimension(Context context, int unit, float value)
    {
        final DisplayMetrics displayMetrics = getDisplayMetrics(context);
        return TypedValue.applyDimension(unit, value, displayMetrics);
    }

    //=========================================================================
    // Constants
    //=========================================================================

    private static final DisplayManager INSTANCE = new DisplayManager();
}