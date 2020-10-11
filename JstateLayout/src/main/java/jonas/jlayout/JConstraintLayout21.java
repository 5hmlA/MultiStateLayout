package jonas.jlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * @another 江祖赟
 * @date 2018/2/7.
 * @from    https://zhuanlan.zhihu.com/p/20689459
 */
public class JConstraintLayout21 extends ConstraintLayout {

    public JConstraintLayout21(Context context){
        super(context);
    }

    public JConstraintLayout21(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public JConstraintLayout21(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        super.onSizeChanged(width, height, oldwidth, oldheight);
	    path.reset();
        setClickable(true);
	    path.addCircle(width / 2, height / 2, Math.min(width, height) / 2, Path.Direction.CCW);
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline){
                outline.setAlpha(0.5F);
            }
        });
//        setOutlineAmbientShadowColor();
//        setOutlineSpotShadowColor();
    }

    Drawable mViewBackground;
	Path path = new Path();
	@Override public void draw(Canvas canvas) {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速
		if(getBackground() != null) {
			mViewBackground = getBackground();//取出背景
			mViewBackground.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
			setBackground(null);//不让控件绘制  如果不置空背景的话 会有黑色剪切区域外背景
		}
		canvas.clipPath(path);//剪切形状
		mViewBackground.setState(getDrawableState());//如何获取状态
		mViewBackground.draw(canvas);//重新绘制背景
		super.draw(canvas);
	}
}
