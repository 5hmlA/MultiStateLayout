package jonas.jlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import jonas.jlayout.helper.IViewWrapper;
import jonas.jlayout.helper.ShapeShaderWrapper;

/**
 * @another 江祖赟
 * @date 2018/2/7.
 * @from http://antoine-merle.com/blog/2013/07/17/adding-a-foreground-selector-to-a-view/
 */
public class JConstraintLayout extends ConstraintLayout implements IViewWrapper.IForwardDraw {

	private List<IViewWrapper> mShapeWrapperList = new ArrayList<>();


	public JConstraintLayout(Context context) {
		this(context, null);
	}


	public JConstraintLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public JConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mShapeWrapperList.add(ShapeShaderWrapper.wrapper(this, context, attrs, defStyleAttr));
	}


	@Override protected void onFinishInflate() {
		super.onFinishInflate();
		setClickable(true);
	}


	@Override protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
		super.onSizeChanged(width, height, oldwidth, oldheight);
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			iViewWrapper.wrapperSizeChange(width, height);
		}
	}


	@Override protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			iViewWrapper.wrapperAttachedToWindow();
		}
	}


	@Override protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			iViewWrapper.wrapperDetachedFromWindow();
		}
	}


	@Override protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			iViewWrapper.wrapperOnDraw(canvas);
		}
	}


	@SuppressLint("MissingSuperCall") @Override public void draw(Canvas canvas) {
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			iViewWrapper.wrapperDraw(canvas);
		}
		//super.draw(canvas);
	}


	@Override public void dispatchDraw(Canvas canvas) {
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			iViewWrapper.wrapperDispatchDraw(canvas);
		}
	}


	@Override protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			iViewWrapper.wapperDrawChild(canvas, child, drawingTime);
		}
		return true;
	}


	@Override public boolean dispatchTouchEvent(MotionEvent ev) {
		for (IViewWrapper iViewWrapper : mShapeWrapperList) {
			if(iViewWrapper.wrapperTouchEvent(ev)) {
				//点击在限定区域之内
				return super.dispatchTouchEvent(ev);
			}
		}
		return false;
	}


	@Override public void forwardDispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}


	@SuppressLint("WrongCall") @Override public void forwardOnDraw(Canvas canvas) {
		super.onDraw(canvas);
	}


	@Override public void forwardDraw(Canvas canvas) {
		super.draw(canvas);
	}


	@Override public boolean forwardDrawChild(Canvas canvas, View child, long drawingTime) {
		return super.drawChild(canvas, child, drawingTime);
	}


	public List<IViewWrapper> getShapeWrapperList() {
		return mShapeWrapperList;
	}


	public void addIviewWrapper(IViewWrapper viewWrapper) {
		mShapeWrapperList.add(viewWrapper);
	}
}
