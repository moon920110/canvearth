package com.canvearth.canvearth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.canvearth.canvearth.client.UI;

import org.apache.commons.lang3.RandomUtils;

public final class SplashForegroundView extends View
{
	private final int lineColor = 0xFF101010;
	//=========================================================================
	// Constructors
	//=========================================================================

	public SplashForegroundView(Context context)
	{
		super(context);
		initView(context);
	}

	public SplashForegroundView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initView(context);
	}

	public SplashForegroundView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initView(context);
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (m_state == State.NOT_STARTED || m_path == null)
		{
			return;
		}

		if (m_pathMaxLength == 0)
		{
			final Matrix matrix = new Matrix();

			matrix.setScale(
					canvas.getWidth() / m_pathWidth,
					canvas.getHeight() / m_pathHeight);

			m_path.transform(matrix);

			final PathMeasure pathMeasure = new PathMeasure(m_path, false);

			while (true)
			{
				m_pathMaxLength = Math.max(m_pathMaxLength, pathMeasure.getLength());

				if (pathMeasure.nextContour() == false)
				{
					break;
				}
			}
		}

		final long elapsedTime = System.currentTimeMillis() - m_initialTime - m_startDelay;

		if (elapsedTime > m_strokeDuration)
		{
			if (m_fillDrawable == null)
			{
				changeState(State.FINISHED);
				return;
			}

			if (m_state < State.FILL_STARTED)
			{
				changeState(State.FILL_STARTED);
			}

			drawFill(canvas, elapsedTime);
		}
		else if (elapsedTime > 0)
		{
			drawStroke(canvas, elapsedTime);
		}

		if (elapsedTime > m_strokeDuration + m_fillDuration)
		{
			if (m_state < State.FINISHED)
			{
				changeState(State.FINISHED);
			}
		}

		ViewCompat.postInvalidateOnAnimation(this);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public void setPath(Path path, float pathWidth, float pathHeight)
	{
		m_path = path;
		m_pathWidth = pathWidth;
		m_pathHeight = pathHeight;
		m_pathMaxLength = 0;
	}

	public void setFillDrawable(@DrawableRes int fillDrawableRes)
	{
		m_fillDrawable = ResourcesCompat.getDrawable(getResources(), fillDrawableRes, null);
	}

	public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener)
	{
		m_stateChangeListener = onStateChangeListener;
	}

	public void start()
	{
		checkPath();
		checkPathDimensions();

		changeState(State.STROKE_STARTED);

		m_initialTime = System.currentTimeMillis();
		ViewCompat.postInvalidateOnAnimation(this);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initView(Context context)
	{
		ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);

		m_startDelay = 300;
		m_strokeDuration = 2500;
		m_fillDuration = 4000;

		m_dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_dashPaint.setStyle(Paint.Style.STROKE);
		m_dashPaint.setStrokeCap(Paint.Cap.ROUND);
		m_dashPaint.setStrokeJoin(Paint.Join.ROUND);

		m_strokeWidth = UI.getPixelFromDp(context, 5);
		m_dashPaint.setStrokeWidth(m_strokeWidth);

		m_dashPaint.setColor(0xFF000000);

		m_state = State.NOT_STARTED;
	}

	private void checkPath()
	{
		if (m_path == null)
		{
			throw new IllegalArgumentException(
					"You must provide a not empty path in order to draw the view properly.");
		}
	}

	private void checkPathDimensions()
	{
		if (m_pathWidth <= 0 || m_pathHeight <= 0)
		{
			throw new IllegalArgumentException(
					"You must provide the path dimensions in order map the coordinates properly.");
		}
	}

	private void changeState(int state)
	{
		if (m_state == state)
		{
			return;
		}

		m_state = state;

		if (m_stateChangeListener != null)
		{
			m_stateChangeListener.onStateChange(state);
		}
	}

	private void drawStroke(Canvas canvas, long elapsedTime)
	{
		Log.d("debug", String.valueOf(elapsedTime) + String.valueOf(m_strokeDuration));

		final float phase = minmax(elapsedTime / (float) m_strokeDuration, 0f, 1f);
		final float interpolation = ANIMATION_INTERPOLATOR.getInterpolation(phase);

		final PathEffect pathEffect1 = new DashPathEffect(new float[] { m_pathMaxLength * interpolation, m_pathMaxLength }, 0);
		m_dashPaint.setPathEffect(pathEffect1);

		m_dashPaint.setColor(lineColor);
		canvas.drawPath(m_path, m_dashPaint);

		final PathEffect pathEffect2 = new DashPathEffect(new float[] { 0, m_pathMaxLength * interpolation - m_strokeWidth / 2f, m_strokeWidth / 2f, m_pathMaxLength }, 0);
		m_dashPaint.setPathEffect(pathEffect2);

		m_dashPaint.setColor(lineColor);
		canvas.drawPath(m_path, m_dashPaint);
	}

	private void drawFill(Canvas canvas, long elapsedTime)
	{
		final int width = canvas.getWidth();
		final int height = canvas.getHeight();

		final float phase = minmax((elapsedTime - m_strokeDuration) / (float) m_fillDuration, 0f, 1f);
		final float interpolation = minmax(ANIMATION_INTERPOLATOR.getInterpolation(phase), 0f, 0.85f);

		canvas.save();

		wavesClipping(canvas, width, height, interpolation);

		m_fillDrawable.setBounds(0, 0, width, height);
		m_fillDrawable.draw(canvas);

		canvas.restore();

		//m_dashPaint.setColor(0xFF000000);
		m_dashPaint.setPathEffect(null);
		canvas.drawPath(m_path, m_dashPaint);
	}

	private void wavesClipping(Canvas canvas, int width, int height, float phase)
	{
		if (m_clippingPath == null)
		{
			buildClippingPath(width, height);
		}

		final Path newPath = new Path();
		m_clippingPath.offset(m_wavesIndex, height * -phase, newPath);

		canvas.clipPath(newPath, Region.Op.DIFFERENCE);

		m_wavesIndex = (m_wavesIndex + width / 128f) % width;
	}

	private void buildClippingPath(int width, int height)
	{
		final float variations[] = new float[8];

		variations[0] = variations[7] = RandomUtils.nextFloat(0, 20) + height / 25f;
		variations[1] = variations[6] = RandomUtils.nextFloat(0, 20) + height / 25f;
		variations[2] = variations[5] = RandomUtils.nextFloat(0, 20) + height / 25f;
		variations[3] = variations[4] = RandomUtils.nextFloat(0, 20) + height / 25f;

		final float bottom = height - 20;

		m_clippingPath = new Path();
		m_clippingPath.moveTo(-width, 0);
		m_clippingPath.lineTo(-width, bottom);

		final float dx = width / variations.length;

		int direction = 1;

		for (int index = 0; index < variations.length; index++)
		{
			m_clippingPath.quadTo(
					-width + dx * (index * 2 + 1),
					bottom + variations[index] * direction,
					-width + dx * (index * 2 + 2),
					bottom);

			direction *= -1;
		}

		m_clippingPath.lineTo(width, bottom);
		m_clippingPath.lineTo(width, 0);
		m_clippingPath.close();
	}

    private float minmax(float value, float min, float max)
    {
        return Math.min(Math.max(value, min), max);
    }

	//=========================================================================
	// OnStateChangeListener
	//=========================================================================

	public interface OnStateChangeListener
	{
		void onStateChange(int state);
	}

	//=========================================================================
	// State
	//=========================================================================

	public class State
	{
		public static final int NOT_STARTED = 0;
		public static final int STROKE_STARTED = 1;
		public static final int FILL_STARTED = 2;
		public static final int FINISHED = 3;
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final Interpolator ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

	//=========================================================================
	// Variables
	//=========================================================================

	private Path m_path;
	private float m_pathWidth;
	private float m_pathHeight;
	private float m_pathMaxLength;
	private float m_strokeWidth;
	private Drawable m_fillDrawable;
	private long m_initialTime;
	private long m_startDelay;
	private long m_strokeDuration;
	private long m_fillDuration;
	private Paint m_dashPaint;
	private Path m_clippingPath;
	private float m_wavesIndex;
	private int m_state;
	private OnStateChangeListener m_stateChangeListener;
}
