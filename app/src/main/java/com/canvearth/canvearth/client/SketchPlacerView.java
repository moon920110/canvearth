package com.canvearth.canvearth.client;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SketchPlacerView extends AppCompatImageView {
    private Drawable drawable = null;
    private Rect bound = null;

    public SketchPlacerView(Context context) {
        super(context);
    }

    public SketchPlacerView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SketchPlacerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawable == null) {
            return;
        }
        drawable.setBounds(bound);
        drawable.draw(canvas);
    }

    public void setPhoto(Photo photo) {
        drawable = photo.getDrawable();
        int width = UI.getDisplayWidth();
        int height = UI.getDisplayHeight();
        bound = new Rect(
                width / 4,
                height / 4,
                width * 3 / 4,
                height * 3 / 4);
        invalidate();
    }
}
