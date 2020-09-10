package jonas.jlayout.helper.shadow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.FloatRange;
import jonas.jlayout.helper.JViewWrapper;

/**
 * <pre>
 *     author : jinagzuyun
 *     e-mail : jonas.jzy@gmail.com
 *     time   : 2018/09/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class AutoShadow extends JViewWrapper {

	// Default shadow values
	private final static float DEFAULT_SHADOW_RADIUS = 30.0F;
	private final static float DEFAULT_SHADOW_DISTANCE = 15.0F;
	private final static float DEFAULT_SHADOW_ANGLE = 45.0F;
	private final static int DEFAULT_SHADOW_COLOR = Color.DKGRAY;

	// Shadow bounds values
	private final static int MAX_ALPHA = 255;
	private final static float MAX_ANGLE = 360.0F;
	private final static float MIN_RADIUS = 0.1F;
	private final static float MIN_ANGLE = 0.0F;
	// Shadow paint
	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
		{
			setDither(true);
			setFilterBitmap(true);
		}
	};
	// Shadow bitmap and canvas
	private Bitmap mBitmap;
	private final Canvas mCanvas = new Canvas();
	// View bounds
	private final Rect mBounds = new Rect();
	// Check whether need to redraw shadow
	private boolean mInvalidateShadow = true;

	// Shadow variables
	private int mShadowColor;
	private int mShadowAlpha;
	private float mShadowRadius;
	private float mShadowDistance;
	private float mShadowAngle;
	private float mOffsetDy;
	private float mOffsetDx;
	private float mZoomDy;
	private boolean mDrawCenter = true;


	private void wrapperView(IForwardDraw view, Context context, AttributeSet attrs, int defStyleAttr) {
		wrapperView(view);

		//TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.jshadowpe, defStyleAttr, R.style.Jmultistate_style);
//		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.jshadowpe);
//		setShadowRadius(typedArray.getDimension(R.styleable.jshadowpe_android_shadowRadius, DEFAULT_SHADOW_RADIUS));
//		mOffsetDx = typedArray.getDimensionPixelSize(R.styleable.jshadowpe_android_shadowDx, Integer.MAX_VALUE);
//		mOffsetDy = typedArray.getDimensionPixelSize(R.styleable.jshadowpe_android_shadowDy, Integer.MAX_VALUE);
//		mShadowDistance = typedArray.getDimension(R.styleable.jshadowpe_android_shadowDx, 0);
//		setShadowColor(typedArray.getColor(R.styleable.jshadowpe_android_shadowColor, DEFAULT_SHADOW_COLOR));
//		mZoomDy = typedArray.getDimensionPixelSize(R.styleable.jshadowpe_z_depth, 0);

		//setShadowAngle(typedArray.getInteger(R.styleable.ShadowLayout_sl_shadow_angle, (int) DEFAULT_SHADOW_ANGLE));

		// Set padding for shadow bitmap
		final int padding = (int) (mShadowRadius + Math.max(mOffsetDx, mOffsetDy));
		mView.setPadding(padding, padding, padding, padding);
//		typedArray.recycle();
	}


	@Override public void wrapperView(IForwardDraw view) {
		mView = (View) view;
		mForwardDraw = view;
		mView.setWillNotDraw(false);
		mView.setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
	}


	@Override public void wrapperDetachedFromWindow() {
		// Clear shadow bitmap
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}


	public float getShadowDistance() {
		return mShadowDistance;
	}


	public void setShadowDistance(final float shadowDistance) {
		mShadowDistance = shadowDistance;
		resetShadow();
	}


	public float getShadowAngle() {
		return mShadowAngle;
	}


	public void setShadowAngle(@FloatRange(from = MIN_ANGLE, to = MAX_ANGLE) final float shadowAngle) {
		mShadowAngle = Math.max(MIN_ANGLE, Math.min(shadowAngle, MAX_ANGLE));
		resetShadow();
	}


	public float getShadowRadius() {
		return mShadowRadius;
	}


	public void setShadowRadius(final float shadowRadius) {
		mShadowRadius = Math.max(MIN_RADIUS, shadowRadius);

		if(mView.isInEditMode()) {
			return;
		}
		// Set blur filter to paint
		mPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL));
		invalidateShadow();
	}


	public void setRadius(final float shadowRadius) {
		mShadowRadius = Math.max(MIN_RADIUS, shadowRadius);
		if(mView.isInEditMode()) {
			return;
		}
		// Set blur filter to paint
		mPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL));
		mInvalidateShadow = true;
		mView.postInvalidate();
	}


	public int getShadowColor() {
		return mShadowColor;
	}


	public void setShadowColor(final int shadowColor) {
		mShadowColor = shadowColor;
		mShadowAlpha = Color.alpha(shadowColor);
		invalidateShadow();
	}


	public void invalidateShadow() {
		mInvalidateShadow = true;
		mView.postInvalidate();
	}


	public float getOffsetDx() {
		return mOffsetDx;
	}


	public float getOffsetDy() {
		return mOffsetDy;
	}


	// Reset shadow layer
	private void resetShadow() {
		// Detect shadow axis offset
		if(mShadowDistance > 0) {
			mOffsetDx = (float) ((mShadowDistance) * Math.cos(mShadowAngle / 180.0F * Math.PI));
			mOffsetDy = (float) ((mShadowDistance) * Math.sin(mShadowAngle / 180.0F * Math.PI));
		}

		mInvalidateShadow = true;
		mView.postInvalidate();
	}


	private int adjustShadowAlpha(final boolean adjust) {
		return Color.argb(adjust ? MAX_ALPHA : mShadowAlpha, Color.red(mShadowColor), Color.green(mShadowColor), Color.blue(mShadowColor));
	}


	@Override public void wrapperSizeChange(int w, int h) {
		super.wrapperSizeChange(w, h);
		mBounds.set(0, 0, w, h);
	}


	@Override public void wrapperOnDraw(Canvas canvas) {

	}


	@Override public void wrapperDispatchDraw(Canvas canvas) {
		// If need to redraw shadow
		if(mInvalidateShadow) {
			//创建 阴影轮廓及图片
			// If bounds is zero
			if(mBounds.width() != 0 && mBounds.height() != 0) {
				// Reset bitmap to bounds
				mBitmap = Bitmap.createBitmap(mBounds.width(), mBounds.height(), Bitmap.Config.ARGB_8888);
				// Canvas reset
				mCanvas.setBitmap(mBitmap);

				// We just redraw
				mInvalidateShadow = false;
				// Main feature of this lib. We create the local copy of all content, so now
				// we can draw bitmap as a bottom layer of natural canvas.
				// We draw shadow like blur effect on bitmap, cause of setShadowLayer() method of
				// paint does`t draw shadow, it draw another copy of bitmap
				mForwardDraw.forwardDispatchDraw(mCanvas);

				// Get the alpha bounds of bitmap
				Bitmap extractedAlpha = mBitmap.extractAlpha();//获取绘制好的alpha图层 获取图片的色彩通道
				// Clear past content content to draw shadow
				mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

				// Draw extracted alpha bounds of our local canvas
				mPaint.setColor(adjustShadowAlpha(false));
				if(mZoomDy != 0f && mZoomDy != Integer.MAX_VALUE) {
					extractedAlpha = getScaleBitmap(extractedAlpha, mZoomDy);
				}
				if(mDrawCenter) {
					final int w = extractedAlpha.getWidth();
					final int h = extractedAlpha.getHeight();
					float l = (mCanvas.getWidth() - w) / 2 + (mOffsetDx == Integer.MAX_VALUE ? 0 : mOffsetDx);
					float t = (mCanvas.getHeight() - h) / 2 + (mOffsetDy == Integer.MAX_VALUE ? 0 : mOffsetDy);
					mCanvas.drawBitmap(extractedAlpha, l, t, mPaint);
				} else {
					mCanvas.drawBitmap(extractedAlpha, mOffsetDx == Integer.MAX_VALUE ? 0 : mOffsetDx, mOffsetDy == Integer.MAX_VALUE ? 0 : mOffsetDy,
							mPaint);
				}

				// Recycle and clear extracted alpha
				extractedAlpha.recycle();
			} else {
				// Create placeholder bitmap when size is zero and wait until new size coming up
				mBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
			}
		}

		// Reset alpha to draw child with full alpha
		mPaint.setColor(adjustShadowAlpha(true));
		// Draw shadow bitmap
		if(mCanvas != null && mBitmap != null && !mBitmap.isRecycled()) {
			canvas.drawBitmap(mBitmap, 0.0F, 0.0F, mPaint);
		}

		// Draw child`s
		mForwardDraw.forwardDispatchDraw(canvas);
	}


	public void setZoomDy(float dy) {
		mInvalidateShadow = true;
		mZoomDy = dy;
		mView.postInvalidate();
	}


	public void setOffsetDx(float dx) {
		mInvalidateShadow = true;
		mOffsetDx = dx;
		mView.postInvalidate();
	}


	public void setOffsetDy(float dy) {
		mInvalidateShadow = true;
		mOffsetDy = dy;
		mView.postInvalidate();
	}


	public Bitmap getScaleBitmap(Bitmap mBitmap, float dy) {
		int width = mBitmap.getWidth();
		int height = mBitmap.getHeight();
		float h = height + dy;
		if(h <= 1) {
			h = 1;
		}
		float scale = h / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap mScaleBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
		return mScaleBitmap;
	}


	public void setDrawCenter(boolean drawCenter) {
		this.mDrawCenter = drawCenter;
		mInvalidateShadow = true;
		mView.postInvalidate();
	}
}
