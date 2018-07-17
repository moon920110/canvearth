package com.canvearth.canvearth.client;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

public class SketchPlacerView extends AppCompatImageView {
    private Drawable m_targetDrawable;
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
    }

    public void setPhoto(Photo photo) {
        Drawable drawable = photo.getDrawable();
        if (drawable != null)
            setImageDrawable(drawable);
    }
}
