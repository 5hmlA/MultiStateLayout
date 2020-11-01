package jonas.jlayout.layout;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

/**
 * @author yun.
 * @date 2020/9/10 0010
 * @des [一句话描述]
 * @since [https://github.com/ZuYun]
 * <p><a href="https://github.com/ZuYun">github</a>
 */
public class AniCardView extends CardView implements ValueAnimator.AnimatorUpdateListener {

    private static final long ANI_TIME = 250L;
    private static final float PRESSEFFECT = 0.9F;
    private static final String SHOWPRESSHOLDER = "showPress";
    private float mShowPressOffset = 1;
    private ValueAnimator mRevertAnimator;
    private ValueAnimator mPressAnimator;
    Path mCircularReveal = new Path();
    PointF mCenter = new PointF();
    private float mRevealLenth;

    public AniCardView(@NonNull Context context) {
        super(context);
    }

    public AniCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AniCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int)(size * (111 / 99.33F)), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setAlpha(.35f);
                outline.setRoundRect(0,0,getWidth(),getHeight(),getRadius());
            }
        });
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (!this.isFocused() && !this.isSelected() && !this.isPressed()) {
            if (this.isEnabled()) {
                this.animateToNormal();
            }
        } else {
            this.animateToPressed();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setEnabled(true);
                }
            }, ANI_TIME / 2);
        }

    }

    private void animateToPressed() {
        if (mRevertAnimator != null && mRevertAnimator.isRunning()) {
            mRevertAnimator.cancel();
        }
        this.getPressedAnimator()
                .start();
    }

    private Animator getPressedAnimator() {
        PropertyValuesHolder expandHolder = PropertyValuesHolder.ofFloat(SHOWPRESSHOLDER,
                mShowPressOffset, PRESSEFFECT);
        mPressAnimator = ValueAnimator.ofPropertyValuesHolder(expandHolder);
        mPressAnimator.setInterpolator((TimeInterpolator)new PathInterpolator(0.25F, 0.1F, 0.1F, 1.0F));
        mPressAnimator.setDuration(ANI_TIME);
        mPressAnimator.addUpdateListener(this);
        return mPressAnimator;
    }

    private void animateToNormal() {
        if (mPressAnimator != null && mPressAnimator.isRunning()) {
            mPressAnimator.cancel();
        }
        this.getRevertAnimator()
                .start();
    }

    private Animator getRevertAnimator() {
        PropertyValuesHolder expandHolder = PropertyValuesHolder.ofFloat(SHOWPRESSHOLDER,
                mShowPressOffset, 1);
        mRevertAnimator = ValueAnimator.ofPropertyValuesHolder(expandHolder);
        mRevertAnimator
                .setInterpolator((TimeInterpolator)new PathInterpolator(0.25F, 0.1F, 0.1F, 1.0F));
        mRevertAnimator.setDuration(ANI_TIME);
        mRevertAnimator.addUpdateListener(this);
        return mRevertAnimator;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mShowPressOffset = (float)animation.getAnimatedValue(SHOWPRESSHOLDER);
        setScaleX(mShowPressOffset);
        setScaleY(mShowPressOffset);
    }

    @Override
    public void setCardBackgroundColor(int color) {
        super.setCardBackgroundColor(color);
    }


    @Override
    public void onDrawForeground(Canvas canvas) {
        if (getForeground() != null) {
            canvas.save();
            canvas.clipPath(mCircularReveal);
            super.onDrawForeground(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenter.set(w / 2F, h / 2F);
        mRevealLenth = (float)Math.sqrt(Math.pow(Math.max(w, h), 2) / 2D);
    }

}
