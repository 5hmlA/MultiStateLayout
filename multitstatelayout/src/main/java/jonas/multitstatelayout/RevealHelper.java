package jonas.multitstatelayout;

import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;

/**
 * @author jiangzuyun.
 * @date 2016/7/19
 * @des [一句话描述]
 * @since [产品/模版版本]
 */


public class RevealHelper {

    private PointF mCenter;
    private float mRadius;
    private float mMinRadius = 100;
    private Path mPath;
    private float mW;
    private float mH;
    private View mView;
    private ObjectAnimator mOa = ObjectAnimator.ofFloat(this, "radius", 100, mW);
    private long durationani = 300;

    private RevealHelper(float w, float h, View view) {
        mW = w;
        mH = h;
        mView = view;
        mCenter = new PointF(w / 2f, h / 2f);
        mPath = new Path();
        mPath.addCircle(mCenter.x, mCenter.y, 100, Path.Direction.CW);
    }

    public static RevealHelper create(float w, float h, View view) {
        return new RevealHelper(w, h, view);
    }

    public void clipReveal(Canvas canvas) {
        canvas.clipPath(mPath);
    }

    private float getRadius() {
        return mRadius;
    }

    private void setRadius(float radius) {
        mRadius = radius;
        mPath.reset();
        mPath.addCircle(mCenter.x, mCenter.y, mRadius, Path.Direction.CW);
        mView.invalidate();
    }

    public void touchEvent(MotionEvent ev) {
        if (mOa.isRunning()) {
            mOa.cancel();
        }
        mPath.reset();
        mCenter.x = ev.getX();
        mCenter.y = ev.getY();
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mPath.addCircle(mCenter.x, mCenter.y, mMinRadius, Path.Direction.CW);
            startAnimation(mCenter.x, mCenter.y);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            mPath.addCircle(mCenter.x, mCenter.y, mMinRadius, Path.Direction.CW);
        }
        mView.invalidate();
    }

    private float getLonsgRadius(float x, float y) {
        if (y > mH / 2f) {
            //上部分
            if (x > mW / 2f) {
                return (float) getPointLength(x, y, 0, 0);
            } else {
                return (float) getPointLength(x, y, mW, 0);
            }
        } else {
            if (x > mW / 2f) {
                return (float) getPointLength(x, y, 0, mH);
            } else {
                return (float) getPointLength(x, y, mW, mH);
            }
        }
    }

    private double getPointLength(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow((y1 - y2), 2));
    }

    private void startAnimation(float x, float y) {
        mOa.setFloatValues(mMinRadius, getLonsgRadius(x, y));
        mOa.setDuration(durationani);
        mOa.start();
    }

    public void cutRevealAni(float x, float y){
        mOa.setFloatValues(getLonsgRadius(x, y),mMinRadius);
        mOa.setInterpolator(new BounceInterpolator());
        mOa.setDuration(durationani);
        mOa.start();
    }
    public void expandRevealAni(float x, float y){
        mOa.setFloatValues(mMinRadius, getLonsgRadius(x, y));
        mOa.setInterpolator(new AccelerateInterpolator());
        mOa.setDuration(durationani);
        mOa.start();
    }


}
//AccelerateDecelerateInterpolator （效果）加速减速插补器（先慢后快再慢）
//
//AccelerateInterpolator 加速插补器（先慢后快）
//
//AnticipateInterpolator 向前插补器（先往回跑一点，再加速向前跑）
//
//AnticipateOvershootInterpolator 向前向后插补器（先往回跑一点，再向后跑一点，再回到终点）
//
//BounceInterpolator 反弹插补器（在动画结束的时候回弹几下，如果是竖直向下运动的话，就是玻璃球下掉弹几下的效果）
//
//CycleInterpolator 循环插补器（按指定的路径以指定时间（或者是偏移量）的1/4、变速地执行一遍，再按指定的轨迹的相反反向走1/2的时间，再按指定的路径方向走完剩余的1/4的时间，最后回到原点。假如：默认是让a从原点往东跑100米。它会先往东跑100米，然后往西跑200米，再往东跑100米回到原点。可在代码中指定循环的次数）
//
//DecelerateInterpolator 减速插补器（先快后慢）
//
//LinearInterpolator 直线插补器（匀速）
//
//OvershootInterpolator 超出插补器（向前跑直到越界一点后，再往回跑）