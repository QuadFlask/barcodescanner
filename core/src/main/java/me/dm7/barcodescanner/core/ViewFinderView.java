package me.dm7.barcodescanner.core;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ViewFinderView extends View {
	private static final String TAG = "ViewFinderView";

	private Rect mFramingRect;

	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 240;

	private static final float LANDSCAPE_WIDTH_RATIO = 5f / 8;
	private static final float LANDSCAPE_HEIGHT_RATIO = 5f / 8;
	private static final int LANDSCAPE_MAX_FRAME_WIDTH = (int) (1920 * LANDSCAPE_WIDTH_RATIO); // = 5/8 * 1920
	private static final int LANDSCAPE_MAX_FRAME_HEIGHT = (int) (1080 * LANDSCAPE_HEIGHT_RATIO); // = 5/8 * 1080

	private static final float PORTRAIT_WIDTH_RATIO = 7f / 8;
	private static final float PORTRAIT_HEIGHT_RATIO = 4f / 8;
	private static final int PORTRAIT_MAX_FRAME_WIDTH = (int) (1080 * PORTRAIT_WIDTH_RATIO); // = 7/8 * 1080
	private static final int PORTRAIT_MAX_FRAME_HEIGHT = (int) (1920 * PORTRAIT_HEIGHT_RATIO); // = 3/8 * 1920

	private Paint maskPaint = new Paint();
	private Paint borderPaint = new Paint();
	private int borderLineLength;

	public ViewFinderView(Context context) {
		super(context);
	}

	public ViewFinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setupViewFinder() {
		initPaints();
		updateFramingRect();
		invalidate();
	}

	private void initPaints() {
		Resources resources = getResources();
		maskPaint.setColor(resources.getColor(R.color.viewfinder_mask));

		borderPaint.setColor(resources.getColor(R.color.viewfinder_border));
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(resources.getInteger(R.integer.viewfinder_border_width));
		borderLineLength = resources.getInteger(R.integer.viewfinder_border_length);
	}

	public Rect getFramingRect() {
		return mFramingRect;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (mFramingRect == null) {
			return;
		}

		drawViewFinderMask(canvas);
		drawViewFinderBorder(canvas);
	}

	public void drawViewFinderMask(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		canvas.drawRect(0, 0, width, mFramingRect.top, maskPaint);
		canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, maskPaint);
		canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, maskPaint);
		canvas.drawRect(0, mFramingRect.bottom + 1, width, height, maskPaint);
	}

	public void drawViewFinderBorder(Canvas canvas) {
		float border = borderPaint.getStrokeWidth() / 2;
		canvas.drawLine(
				mFramingRect.left - 1,
				mFramingRect.top - 1 - border,
				mFramingRect.left - 1,
				mFramingRect.top - 1 + borderLineLength,
				borderPaint);
		canvas.drawLine(
				mFramingRect.left - 1 - border,
				mFramingRect.top - 1,
				mFramingRect.left - 1 + borderLineLength,
				mFramingRect.top - 1,
				borderPaint);

		canvas.drawLine(
				mFramingRect.left - 1,
				mFramingRect.bottom + 1 + border,
				mFramingRect.left - 1,
				mFramingRect.bottom + 1 - borderLineLength,
				borderPaint);
		canvas.drawLine(
				mFramingRect.left - 1 - border,
				mFramingRect.bottom + 1,
				mFramingRect.left - 1 + borderLineLength,
				mFramingRect.bottom + 1,
				borderPaint);

		canvas.drawLine(
				mFramingRect.right + 1,
				mFramingRect.top - 1 - border,
				mFramingRect.right + 1,
				mFramingRect.top - 1 + borderLineLength,
				borderPaint);
		canvas.drawLine(
				mFramingRect.right + 1 + border,
				mFramingRect.top - 1,
				mFramingRect.right + 1 - borderLineLength,
				mFramingRect.top - 1,
				borderPaint);

		canvas.drawLine(
				mFramingRect.right + 1,
				mFramingRect.bottom + 1 + border,
				mFramingRect.right + 1,
				mFramingRect.bottom + 1 - borderLineLength,
				borderPaint);
		canvas.drawLine(
				mFramingRect.right + 1 + border,
				mFramingRect.bottom + 1,
				mFramingRect.right + 1 - borderLineLength,
				mFramingRect.bottom + 1,
				borderPaint);
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		updateFramingRect();
	}

	public synchronized void updateFramingRect() {
		Point viewResolution = new Point(getWidth(), getHeight());
		if (viewResolution == null) {
			return;
		}
		int width;
		int height;
		int orientation = DisplayUtils.getScreenOrientation(getContext());

		if (orientation != Configuration.ORIENTATION_PORTRAIT) {
			width = findDesiredDimensionInRange(LANDSCAPE_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, LANDSCAPE_MAX_FRAME_WIDTH);
			height = findDesiredDimensionInRange(LANDSCAPE_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, LANDSCAPE_MAX_FRAME_HEIGHT);
		} else {
			width = findDesiredDimensionInRange(PORTRAIT_WIDTH_RATIO, viewResolution.x, MIN_FRAME_WIDTH, PORTRAIT_MAX_FRAME_WIDTH);
			height = findDesiredDimensionInRange(PORTRAIT_HEIGHT_RATIO, viewResolution.y, MIN_FRAME_HEIGHT, PORTRAIT_MAX_FRAME_HEIGHT);
		}

		int leftOffset = (viewResolution.x - width) / 2;
		int topOffset = (viewResolution.y - height) / 2;
		mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
	}

	private static int findDesiredDimensionInRange(float ratio, int resolution, int hardMin, int hardMax) {
		int dim = (int) (ratio * resolution);
		if (dim < hardMin) {
			return hardMin;
		}
		if (dim > hardMax) {
			return hardMax;
		}
		return dim;
	}

}
