package jonas.jlayout.helper;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * <pre>
 *     author : jinagzuyun
 *     e-mail : jonas.jzy@gmail.com
 *     time   : 2018/09/21
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface IViewWrapper {

	void wrapperView(IForwardDraw view);

	void wrapperSizeChange(int w, int h);

	void wrapperOnDraw(Canvas canvas);

	void wrapperDraw(Canvas canvas);

	void wrapperDispatchDraw(Canvas canvas);

	boolean wapperDrawChild(Canvas canvas, View child, long drawingTime);

	void wrapperAttachedToWindow();

	void wrapperDetachedFromWindow();

	boolean wrapperTouchEvent(MotionEvent ev);

	public interface IForwardDraw {
		void forwardDispatchDraw(Canvas canvas);

		void forwardOnDraw(Canvas canvas);

		void forwardDraw(Canvas canvas);

		boolean forwardDrawChild(Canvas canvas, View child, long drawingTime);
	}
}
