package com.canvearth.canvearth.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.canvearth.canvearth.MapsActivity;


public class MySketchThumbnailView extends AppCompatImageView {
    private Handler mHandlerForInvalidate = new Handler();
    private Paint mPaint = new Paint(Color.RED);

    public MySketchThumbnailView(Context context) {
        super(context);

    }
    public MySketchThumbnailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MySketchThumbnailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int canvasWidth = canvas.getWidth();
        final int canvasHeight = canvas.getHeight();

        MapsActivity parentActivity = ((MapsActivity) getContext());
        parentActivity.requestLocationUpdate();
        if (parentActivity.isInSeeingSketch()) {
            double xFraction = parentActivity.getXFractionInSeeingSketch();
            double yFraction = parentActivity.getYFractionInSeeingSketch();
            mPaint.setColor(Color.RED);
            canvas.drawCircle((float) (canvasWidth * xFraction), (float)(canvasHeight * yFraction), 15, mPaint);
        }

        mHandlerForInvalidate.postDelayed(this::postInvalidate, 2000);
    }
}
