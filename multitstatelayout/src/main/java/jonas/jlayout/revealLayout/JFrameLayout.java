package jonas.jlayout.revealLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import jonas.jlayout.RevealHelper;

/**
 * @author yun.
 * @date 2016/8/6
 * @des [一句话描述]
 * @since [https://github.com/mychoices]
 * <p><a href="https://github.com/mychoices">github</a>
 */
public class JFrameLayout extends FrameLayout {
    private static final long ANITIME = 300;
    private RevealHelper mRevealHelper;
    private PointF mRevPoint;

    public JFrameLayout(Context context){
        super(context);
    }

    public JFrameLayout(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public JFrameLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        //gone 不调用
        super.onSizeChanged(w, h, oldw, oldh);
        mRevealHelper = RevealHelper.create(w, h, this);
        mRevealHelper.setMinRadius(mRevealHelper.dp2px(1)).setAniDuration(ANITIME);
        mRevPoint = new PointF(w/2f, h/2f);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRevPoint.x = event.getX();
                mRevPoint.y = event.getY();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas){
        mRevealHelper.clipReveal(canvas);
        super.dispatchDraw(canvas);
    }

    public void setRealVisibility(final int visibility){
        if(mRevealHelper.isRevealRunning()) {
            return;
        }
        if(mRevealHelper != null && mRevPoint != null) {
            if(visibility == VISIBLE) {
                mRevealHelper.expandRevealAni(mRevPoint.x, mRevPoint.y);
                setVisibility(visibility);
            }else {
                mRevealHelper.cutRevealAni(mRevPoint.x, mRevPoint.y);
                postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        setVisibility(visibility);
                    }
                }, ANITIME);
            }
        }else {
            setVisibility(visibility);
        }
    }

    public JFrameLayout setRealVisibility(int visibility, float x, float y){
        mRevPoint.x = x;
        mRevPoint.y = y;
        setRealVisibility(visibility);
        return this;
    }

    public JFrameLayout setRealVisibility(int visibility, PointF revPoint){
        mRevPoint = revPoint;
        setRealVisibility(visibility);
        return this;
    }
}
