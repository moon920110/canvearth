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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setColor(Color c) {
        ourInstance.color = c.toArgb();
    }

    public int getColor() {
        return ourInstance.color;
    }
}
