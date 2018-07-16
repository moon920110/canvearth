package com.canvearth.canvearth.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ScreenUtils {
    public static void showToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM,
                0, 200);
        toast.show();
    }
}
