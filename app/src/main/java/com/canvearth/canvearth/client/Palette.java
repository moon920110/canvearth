package com.canvearth.canvearth.client;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.canvearth.canvearth.utils.Constants;

public class Palette {
    private static final Palette ourInstance = new Palette();
    private int color = Constants.PALETTE_DEFAULT_COLOR;

    public static Palette getInstance() {
        return ourInstance;
    }

    // It requires Color.argb(a, r, g, b) or 0xFFFFFFFF(argb) format data for parameter.
    // Cuz Color.toArgb() method needs at least api version 26.
    // But our minimum require version limit is 15.
    public void setColor(int c) {
        ourInstance.color = c;
    }

    public int getColor() {
        return ourInstance.color;
    }
}
