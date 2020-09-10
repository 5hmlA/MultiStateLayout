package jonas.jlayout.helper;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * <pre>
 *     author : jinagzuyun
 *     e-mail : jonas.jzy@gmail.com
 *     time   : 2018/09/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public abstract class JViewWrapper implements IViewWrapper {
	protected View mView;
	protected IForwardDraw mForwardDraw;
	/**
	 * <b>实际绘制区域的高度</b><br>
	 * 去掉了预留的阴影控件，pading边距，也可能有去掉边框线的宽度
	 */
	protected int mContentH;
	/**
	 * <b>实际绘制区域的宽度</b><br>
	 * 去掉了预留的阴影控件，pading边距，也可能有去掉边框线的宽度
	 */
	protected int mContentW;
	protected int mOrignW;
	protected int mOrignH;


	@Override public void wrapperView(IForwardDraw view) {
		mView = (View) view;
		mForwardDraw = view;
		mView.setWillNotDraw(false);
		//mView.setLayerType(LAYER_TYPE_SOFTWARE, null);
	}


	@Override public void wrapperSizeChange(int w, int h) {
		mOrignH = h;
		mOrignW = w;
		mContentW = w - mView.getPaddingStart() - mView.getPaddingEnd();
		mContentH = h - mView.getPaddingTop() - mView.getPaddingBottom();
	}


	@Override public void wrapperDispatchDraw(Canvas canvas) {

	}


	@Override public void wrapperDraw(Canvas canvas) {

	}


	@Override public boolean wapperDrawChild(Canvas canvas, View child, long drawingTime) {
		return mForwardDraw.forwardDrawChild(canvas, child, drawingTime);
	}


	@Override public boolean wrapperTouchEvent(MotionEvent ev) {
		return true;
	}


	@Override public void wrapperAttachedToWindow() {

	}


	@Override public void wrapperDetachedFromWindow() {

	}
}
