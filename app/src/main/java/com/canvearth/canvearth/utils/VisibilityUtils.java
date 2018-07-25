package com.canvearth.canvearth.utils;

import android.view.View;

public class VisibilityUtils {
    public static void toggleViewVisibility(View view) {
        int visibility = view.getVisibility();

        if (visibility == View.VISIBLE) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }
}
