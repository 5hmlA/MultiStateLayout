package jonas.jlayout.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * <pre>
 *     author : jinagzuyun
 *     e-mail : jonas.jzy@gmail.com
 *     time   : 2018/09/21
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ShapeShaderWrapper extends JViewWrapper {

	private Paint mXfermodePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
		{
			setStyle(Style.FILL);
		}
	};
	private Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
		{
			setStyle(Style.FILL);
		}
	};
	private Paint mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
		{
			setStyle(Style.STROKE);
		}
	};

	FramePathEffectProvider mFramePathEffectProvider;
	private Path mXfermodePath = new Path();
	private Path mFramePath = new Path();
	//上左，上右，下右，下左
	//private int[] mRadii = new int[] { DpHelper.dp2pxCeilInt(3), DpHelper.dp2pxCeilInt(3), DpHelper.dp2pxCeilInt(3), DpHelper.dp2pxCeilInt(3) };
	private float[] mRadii = new float[8];   // top-left, top-right, bottom-right, bottom-left

	private PorterDuffXfermode mDstInXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
	private boolean mRoundAsCircle;
	private Region mAreaRegion = new Region();
	private float mFrameAlpha = 1;//边框透明度(0~1)
	private int mFrameColor;//边框颜色
	private int mFrameWidth;//边框宽度
	private int mShaderColor;//阴影颜色
	private float mCornerRadius;
	private float mShadowRadius, mShadowDx, mShadowDy;
	private int mShadowZdepth;
	RectF mArea = new RectF();
	Bitmap mTempBitmap;
	private Drawable mViewBackground;


	public static ShapeShaderWrapper wrapper(IForwardDraw view, Context context, AttributeSet attrs, int defStyleAttr) {
		ShapeShaderWrapper shapeWrapper = new ShapeShaderWrapper();
		shapeWrapper.wrapperView(view, context, attrs, defStyleAttr);
		return shapeWrapper;
	}


	private void wrapperView(IForwardDraw view, Context context, AttributeSet attrs, int defStyleAttr) {
		wrapperView(view);
		//getDimension：返回类型为float，
		//getDimensionPixelSize：返回类型为int，由浮点型转成整型时，采用四舍五入原则。
		//getDimensionPixelOffset：返回类型为int，由浮点型转成整型时，原则是忽略小数点部分。
		//TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.jshadowpe, defStyleAttr, R.style.Jmultistate_style);
//		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.jshadowpe);
//		mFrameAlpha = a.getFloat(R.styleable.jshadowpe_frameAlpha, 1);
//		mFrameColor = a.getColor(R.styleable.jshadowpe_frameColor, Color.TRANSPARENT);
//		mFrameWidth = a.getDimensionPixelOffset(R.styleable.jshadowpe_frameWidth, 0);
//		mShaderColor = a.getInt(R.styleable.jshadowpe_android_shadowColor, Color.TRANSPARENT);
//		mShadowZdepth = a.getInt(R.styleable.jshadowpe_z_depth, 0);
//		mShadowDx = a.getInt(R.styleable.jshadowpe_android_shadowDx, 0);
//		mShadowDy = a.getInt(R.styleable.jshadowpe_android_shadowDy, 0);
//		mCornerRadius = a.getDimension(R.styleable.jshadowpe_cornerRadius, 0);
//		a.recycle();
		//mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}


	@Override public void wrapperSizeChange(int w, int h) {
		configFramePaint();
		//if(isNeedShadow()) {
		//	//需要绘制阴影，给阴影预留空间
		//	mView.setPadding(mView.getPaddingStart(), mView.getPaddingTop(), mView.getPaddingEnd(), mView.getPaddingBottom());
		//}
		//边框直接画在上层
		if(mFrameAlpha < 1) {
			//边框需要透明度  那么裁剪部分也把边框去掉
			mView.setPadding(mFrameWidth + mView.getPaddingStart(), mFrameWidth + mView.getPaddingTop(), mView.getPaddingEnd() + mFrameWidth,
					mView.getPaddingBottom() + mFrameWidth);
		}

		super.wrapperSizeChange(w, h);

		mArea.left = mView.getPaddingStart();
		mArea.top = mView.getPaddingTop();
		mArea.right = mArea.left + mContentW;
		mArea.bottom = mArea.top + mContentH;

		mXfermodePath.reset();
		//mXfermodePath.moveTo(mRadii[1], 0);
		////上右
		//mXfermodePath.lineTo(w - mRadii[2], 0);
		//mXfermodePath.quadTo(w, 0, w, mRadii[3]);
		////下右
		//mXfermodePath.lineTo(w, h - mRadii[4]);
		//mXfermodePath.quadTo(w, h, w - mRadii[5], h);
		////下左
		//mXfermodePath.lineTo(mRadii[6], h);
		//mXfermodePath.quadTo(0, h, 0, h - mRadii[7]);
		////上左
		//mXfermodePath.lineTo(0, mRadii[0]);
		//mXfermodePath.quadTo(0, 0, mRadii[1], 0);

		if(mRoundAsCircle) {
			float r = Math.min(mContentW, mContentH) / 2;
			//在支持圆形的时候有一个坑需要注意一下，就是控件长宽比不一致的情况下，由于是按照最短的边计算的，
			PointF center = new PointF(mView.getPaddingStart() + mContentW / 2, mView.getPaddingTop() + mContentH / 2);
			mXfermodePath.addCircle(center.x, center.y, r, Path.Direction.CW);
			//在长宽比不一致的情况下，直接向 Path 添加圆形， Path 是无法填充满画布的，
			// 在绘制的时候可能会出现圆形之外依旧有内容被绘制出来，所以这里使用了两个 moveTo 操作来让 Path 填充满画布
			mXfermodePath.moveTo(0, 0);  // 通过空操作让Path区域占满画布
			mXfermodePath.moveTo(w, h);

			mFramePath.addCircle(center.x, center.y, r - mFrameWidth / 2, Path.Direction.CW);
			mFramePath.moveTo(0, 0);  // 通过空操作让Path区域占满画布
			mFramePath.moveTo(w, h);
		} else {
			mCornerRadius = Math.min(mCornerRadius, Math.min(mContentW, mContentH));
			for (int i = 0; i < mRadii.length; i++) {
				mRadii[i] = mCornerRadius;
			}
			mXfermodePath.addRoundRect(mArea, mCornerRadius, mCornerRadius, Path.Direction.CW);
			if(isNeedDrawFrame()) {
				float[] frameRadii = new float[8];
				for (int i = 0; i < mRadii.length; i++) {
					frameRadii[i] = mCornerRadius - mFrameWidth / 2;
				}
				//mXfermodePath.addRoundRect(mArea, mRadii, Path.Direction.CW);
				//绘制边框的时候 不要忘记边框的宽度
				mFramePath.addRoundRect(new RectF(mArea.left + mFrameWidth / 2, mArea.top + mFrameWidth / 2, mArea.right - mFrameWidth / 2,
						mArea.bottom - mFrameWidth / 2), frameRadii, Path.Direction.CW);
			}
		}
		Region clip = new Region((int) mArea.left, (int) mArea.top, (int) mArea.right, (int) mArea.bottom);
		mAreaRegion.setPath(mXfermodePath, clip);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mView.setClipToOutline(true);
			mView.setOutlineProvider(new ViewOutlineProvider() {
				@Override public void getOutline(View view, Outline outline) {
					outline.setConvexPath(mXfermodePath);
				}
			});
		}
	}


	private void configFramePaint() {
		if(mFramePathEffectProvider != null) {
			mFramePaint.setPathEffect(mFramePathEffectProvider.getPathEffect());
		}
		mFramePaint.setStrokeWidth(mFrameWidth);
		mFramePaint.setColor(mFrameColor);
		mFramePaint.setAlpha(Math.round(255 * mFrameAlpha));
	}


	@Override public void wrapperOnDraw(Canvas canvas) {
		mForwardDraw.forwardOnDraw(canvas);
	}


	@Override public void wrapperDraw(Canvas canvas) {
		super.wrapperDraw(canvas);
		//由于 绘制阴影BlurMaskFilter 必须关闭硬件加速，但是在关闭硬件加速之后 在控件有背景的情况下剪切出的形状区域外会出现黑色背景，无解
		//尝试过取出背景，然后给控件设置无背景，再次根据剪切形状利用canvas.clipPath和drawable.draw()手动绘制背景，问题是会导致按下等状态改变的时候 无法更新背景状态
		//如果需要同时将控件剪切出指定形状，和阴影的情况，在高版本上可以设置elevate和translationZ来处理阴影，或者外层包一个绘制阴影的控件

		////放前面 无法绘制出mViewBackground，mViewBackground.draw无效
		//if(mView.getBackground() != null && mViewBackground != mView.getBackground()) {
		//	mViewBackground = mView.getBackground();
		//	//mViewBackground.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		//	mView.setBackground(null);
		//}
		////
		//if(mViewBackground != null) {
		//	canvas.save();
		//	canvas.clipPath(mXfermodePath);
		//	mViewBackground.setState(mView.getDrawableState());
		//	mViewBackground.draw(canvas);
		//	canvas.restore();
		//}

		//剪切控件为指定形状
		// 1 设置outLineProvider来剪切背景 剪切的形状有限 只能设置圆角矩形/凸多边形
		// 2 bitmapShader
		// 2 xfermode

		//mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);//当控件设置了背景之后 会导致被剪切之外的区域有黑色背景，没设置背景不会出现黑边

		//1，通过canvas.clipPath方式 最简单，但是有锯齿，硬件加速关闭同时又有背景的时候 剪切形状之外的区域会出现黑色背景
		//shapeByClipPath(canvas);
		//2，通过bitmapShader来绘制指定形状，
		//shapeByBitmapShader(canvas);
		shapeByXfermode(canvas);
		//shapeAsBitmapByXfermode(canvas);

		if(isNeedDrawFrame()) {
			//绘制边框
			canvas.drawPath(mFramePath, mFramePaint);
		}

		//放前面 无法绘制出mViewBackground，mViewBackground.draw无效
		//if(mView.getBackground() != null && mViewBackground != mView.getBackground()) {
		//	mViewBackground = mView.getBackground();
		//	//mViewBackground.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		//	mView.setBackground(null);
		//}
		////
		//if(mViewBackground != null) {
		//	canvas.save();
		//	canvas.clipPath(mXfermodePath);
		//	//mViewBackground.setState(mView.getDrawableState());
		//	mViewBackground.draw(canvas);
		//	canvas.restore();
		//}
	}


	/**
	 * 1,必须保存新图层，否则会有黑边<p>
	 * int saveLayer = canvas.saveLayer(0, 0, mContentW, mContentH, null, ALL_SAVE_FLAG);<p>
	 * super.dispatchDraw(canvas);<p>
	 * wrapperDispatchDraw(saveLayer,canvas);<p>
	 */
	@Override public void wrapperDispatchDraw(Canvas canvas) {
		mForwardDraw.forwardDispatchDraw(canvas);
	}


	/**
	 * 此方法 不能关闭硬件加速 但是绘制阴影必须关闭硬件加速
	 * 需要注意的是关闭硬件加速会导致xfermode无效
	 * https://blog.csdn.net/wingichoy/article/details/50534175
	 */
	private void shapeByXfermode(Canvas canvas) {
		//剪切出圆角
		//1,必须保存新图层，否则会有黑边
		int saveLayer = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
		//控件绘制原始内容
		mForwardDraw.forwardDraw(canvas);
		//2,画笔 style ① Paint.Style.FILL -- ② Paint.Style.STROKE+paint.setStrokeWidth 是环
		//3,绘制顺序 是先DST(下层)，后SRC(上层)
		//两次绘制取DST的相交部分
		mXfermodePaint.setXfermode(mDstInXfermode);

		//setLayerType(LAYER_TYPE_SOFTWARE, null); //关闭硬件加速 会导致 setXfermode异常DST_IN无效
		//mXfermodePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawPath(mXfermodePath, mXfermodePaint);
		mXfermodePaint.setXfermode(null);
		canvas.restoreToCount(saveLayer);
	}


	/**
	 * 使用Xfermode绘制两图片
	 * 关闭硬件加速的时候 DST_IN依然有效果，但是当设置背景图片之后，DST_OUT的区域会变为黑色
	 */
	private void shapeAsBitmapByXfermode(Canvas canvas) {
		//mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速之后，通过两张bitmap 的方式 PorterDuff.Mode.DST_IN可以正常处理，但是当控件有background的时候会有黑色背景无法去掉
		Bitmap destBtmp = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas destCanvas = new Canvas(destBtmp);
		//控件绘制原始内容 到 destBitmap上
		mForwardDraw.forwardDraw(destCanvas);
		Bitmap srcBtmp = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas srcCanvas = new Canvas(srcBtmp);
		//将指定的形状 到 srcBitmap上
		srcCanvas.drawPath(mXfermodePath, mXfermodePaint);

		//通过xfermode绘制出形状
		//1,必须保存新图层，否则会有黑边
		int saveLayer = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);

		//2,画笔 style ① Paint.Style.FILL -- ② Paint.Style.STROKE+paint.setStrokeWidth 是环
		//3,绘制顺序 是先DST(下层)，后SRC(上层)
		//两次绘制取DST的相交部分
		//先画 dest
		canvas.drawBitmap(destBtmp, 0, 0, mXfermodePaint);
		mXfermodePaint.setXfermode(mDstInXfermode);
		//再画 src
		canvas.drawBitmap(srcBtmp, 0, 0, mXfermodePaint);
		mXfermodePaint.setXfermode(null);
		canvas.restoreToCount(saveLayer);
	}


	private void shapeByClipPath(Canvas canvas) {
		canvas.save();
		canvas.clipPath(mXfermodePath);
		mForwardDraw.forwardDraw(canvas);
		canvas.restore();
	}


	private void shapeByBitmapShader(Canvas canvas) {
		//mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); //关闭硬件加速 会导致 出现黑色背景、、 当控件设置了背景之后 会导致被剪切之外的区域有黑色背景，没设置背景不会出现黑边
		//创建bitmap，canvas,然后给画笔设置bitmapShader，
		if(mTempBitmap == null) {
			mTempBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
		}
		Canvas tempcanvas = new Canvas(mTempBitmap);
		//将控件绘制到新的canvas上(也就是绘制到bitmap上)，
		mForwardDraw.forwardDraw(tempcanvas);
		//然后通过drawPath绘制指定形状
		mShadowPaint.setShader(new BitmapShader(mTempBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
		canvas.drawPath(mXfermodePath, mShadowPaint);
		mShadowPaint.setShader(null);
	}


	private boolean isNeedDrawFrame() {
		return isNeedDraw(mFrameWidth, mFrameColor);
	}


	protected boolean isNeedDraw(float width, int color) {
		return width > 0 && color != Color.TRANSPARENT;
	}


	Path mChildXfermodePath = new Path();


	@Override public boolean wapperDrawChild(Canvas canvas, View child, long drawingTime) {
		int saveLayer = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
		//控件绘制原始内容
		boolean b = mForwardDraw.forwardDrawChild(canvas, child, drawingTime);
		//2,画笔 style ① Paint.Style.FILL -- ② Paint.Style.STROKE+paint.setStrokeWidth 是环
		//3,绘制顺序 是先DST(下层)，后SRC(上层)
		//两次绘制取DST的相交部分
		mXfermodePaint.setXfermode(mDstInXfermode);

		//setLayerType(LAYER_TYPE_SOFTWARE, null); //关闭硬件加速 会导致 setXfermode异常DST_IN无效
		//mXfermodePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawPath(mChildXfermodePath, mXfermodePaint);
		mXfermodePaint.setXfermode(null);
		canvas.restoreToCount(saveLayer);
		return b;
	}


	@Override public void wrapperAttachedToWindow() {

	}


	@Override public void wrapperDetachedFromWindow() {

	}


	@Override public boolean wrapperTouchEvent(MotionEvent ev) {
		return mAreaRegion.contains((int) ev.getX(), (int) ev.getY());
	}


	/**
	 * 获取边框画笔
	 */
	public Paint getFramePaint() {
		return mFramePaint;
	}


	public interface FramePathEffectProvider {
		PathEffect getPathEffect();
	}
}
