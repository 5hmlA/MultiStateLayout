package jonas.jlayout.layout;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import jonas.jlayout.MultiStateLayout;
import jonas.jlayout.R;

/**
 * @author yun.
 * @date 2020/9/10 0010
 * @des [支持约束自己的ConstraintLayout]
 * @since [https://github.com/ZuYun]
 * <p><a href="https://github.com/ZuYun">github</a>
 */
public class AniSJConstraintLayout extends ConstraintLayout {
    private static final String TAG = "AniSJConstraintLayout";
    private static final int ANI_TIME = 250;
    private static final float PRESSEFFECT = 0.9F;//按压效果 缩放程度
    private static final String PROP_SCALEX = "scaleX";
    private static final String PROP_SCALEY = "scaleY";
    private ObjectAnimator mRevertAnimator;
    private int sidesMargin;
    private float dimensionRatioValue = 0;
    private ObjectAnimator mPressAnimator;
    private boolean determinedWidthSide;

    public AniSJConstraintLayout(@NonNull Context context) {
        this(context, (AttributeSet)null);
    }

    public AniSJConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AniSJConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AniSJConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.sidesMargin = MultiStateLayout.dp2px(24.0F);
        @SuppressLint("CustomViewStyleable") TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.ConstraintLayout_Layout, defStyleAttr, defStyleRes);
        String dimensionRatio = a.getString(R.styleable.ConstraintLayout_Layout_layout_constraintDimensionRatio);
        a.recycle();
        if(TextUtils.isEmpty(dimensionRatio)){
            return;
        }
        //1, 如果要使用这个属性，我们至少要把控件的宽或高中间的一个设置为match constraints  确定的边建议用match_parent
        //2, ratio的比值表示的是宽高比，注意，一直都是宽高比(宽:高)

        //确定的宽
        determinedWidthSide = true;
        int len = dimensionRatio.length();
        int commaIndex = dimensionRatio.indexOf(',');//逗号
        if (commaIndex == 1) {
            String dimension = dimensionRatio.substring(0, commaIndex);
            if (dimension.equalsIgnoreCase("W")) {
                determinedWidthSide = false;
                //h为基础去设置w  要计算w必须确定h
            } else if (dimension.equalsIgnoreCase("H")) {
                //w为基础去设置h  要计算h必须确定w
                determinedWidthSide = true;
            }
            ++commaIndex;
        } else {
            commaIndex = 0;
        }

        //要计算哪边,宽:高,两边边距>extra

        //w,10:1,20>extra  find extra
        int extraIndex = dimensionRatio.indexOf('>');
        if(extraIndex>0) {
            String extra = dimensionRatio.substring(extraIndex + 1);
            if("full".equalsIgnoreCase(extra)) {
                //横向全屏
            }
            //去掉后面的>extra
            dimensionRatio = dimensionRatio.substring(0, extraIndex);
            len = dimensionRatio.length();
        }

        //w,10:1,20 (最后的20是两边边距)  find marg
        int endIndex = dimensionRatio.lastIndexOf(',');//逗号 44
        String margStr;//两边边距
        if (endIndex <= commaIndex) {
            //最后一个逗号和第一个逗号位置相同说明没设置 marg
            endIndex = dimensionRatio.length();
        } else {
            margStr = dimensionRatio.substring(endIndex + 1);
            this.sidesMargin = MultiStateLayout.dp2px((float)Integer.parseInt(margStr));
            //去掉,marg
            dimensionRatio = dimensionRatio.substring(0, endIndex);
            len = dimensionRatio.length();
        }

        //w,10:1 (宽:高)
        int colonIndex = dimensionRatio.indexOf(':');//colon 冒号 58
        String heightPercentageStr;//分母
        String widthPercentageStr;
        if (colonIndex >= 0 && colonIndex < len - 1) {
            widthPercentageStr = dimensionRatio.substring(commaIndex, colonIndex);
            //分母 >高
            heightPercentageStr = dimensionRatio.substring(colonIndex + 1, endIndex);
            if (widthPercentageStr.length() > 0 && heightPercentageStr.length() > 0) {
                try {
                    float widthPercentageValue = Float.parseFloat(widthPercentageStr);//宽占比
                    float heightPercentageValue = Float.parseFloat(heightPercentageStr);//高占比
                    calcuRatioValue(widthPercentageValue, heightPercentageValue);
                } catch (NumberFormatException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        } else {
            //宽是高的几倍
            widthPercentageStr = dimensionRatio.substring(commaIndex);
            if (widthPercentageStr.length() > 0) {
                try {
                    calcuRatioValue(Float.parseFloat(widthPercentageStr),1);
                } catch (NumberFormatException var16) {
                    Log.d(TAG, var16.getMessage());
                }
            }
        }

    }

    private void calcuRatioValue(float widthPercentageValue, float heightPercentageValue){
        if (widthPercentageValue > 0.0F && heightPercentageValue > 0.0F) {
            if (determinedWidthSide) {
                //宽确定 计算高
                this.dimensionRatioValue = Math.abs(heightPercentageValue / widthPercentageValue);
            } else {
                //高确定 计算宽
                this.dimensionRatioValue = Math.abs(widthPercentageValue / heightPercentageValue);
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(dimensionRatioValue == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        //以宽度为准 高度根据比例计算 只管AT_MOST 和 EXACTLY
        if(determinedWidthSide) {
            int orignWidth = MeasureSpec.getSize(widthMeasureSpec);
            //确定宽去计算高
            boolean isWrapContent = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED;
            if(orignWidth == 0 || isWrapContent) {
                //需要计算自己的值
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                if(isWrapContent) {
                    int measuredWidth = getMeasuredWidth();
                    setMeasuredDimension(measuredWidth,
                            Math.round(measuredWidth * dimensionRatioValue));
                }
            } else {
                int width = orignWidth - this.sidesMargin * 2;
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.round((float)width * this.dimensionRatioValue), MeasureSpec.EXACTLY));
            }
        } else {
            int orignHeight = MeasureSpec.getSize(heightMeasureSpec);
            boolean isWrapContent = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED;
            if(orignHeight == 0 || isWrapContent) {
                //需要计算自己的值
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                if(isWrapContent) {
                    int measuredHeight = getMeasuredHeight();
                    setMeasuredDimension(Math.round(measuredHeight*dimensionRatioValue),
                            measuredHeight);
                }
            }else {
                int height = orignHeight - this.sidesMargin * 2;
                super.onMeasure(MeasureSpec.makeMeasureSpec(Math.round((float)height * this.dimensionRatioValue), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            animateToNormal();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 缩放
     * @return
     */
    private Animator getPressedAnimator() {
        PropertyValuesHolder propScaleXholder = PropertyValuesHolder.ofFloat(PROP_SCALEX,
                getScaleX(), PRESSEFFECT);
        PropertyValuesHolder propScaleYholder = PropertyValuesHolder.ofFloat(PROP_SCALEY,
                getScaleY(), PRESSEFFECT);
        mPressAnimator = ObjectAnimator
                .ofPropertyValuesHolder(this, propScaleXholder, propScaleYholder);
        mPressAnimator
                .setInterpolator((TimeInterpolator)new PathInterpolator(0.25F, 0.1F, 0.1F, 1.0F));
        mPressAnimator.setDuration(ANI_TIME);
        return mPressAnimator;
    }

    private void animateToNormal() {
        if (mPressAnimator != null && mPressAnimator.isRunning()) {
            mPressAnimator.cancel();
        }
        if(getScaleX() == 1 && getScaleY() ==1) {
            return;
        }
        this.getRevertAnimator()
                .start();
    }

    /**
     * 缩放还原
     * @return
     */
    private Animator getRevertAnimator() {
        PropertyValuesHolder propScaleXholder = PropertyValuesHolder.ofFloat(PROP_SCALEX,
                getScaleX(), 1);
        PropertyValuesHolder propScaleYholder = PropertyValuesHolder.ofFloat(PROP_SCALEY,
                getScaleY(), 1);
        mRevertAnimator = ObjectAnimator
                .ofPropertyValuesHolder(this, propScaleXholder, propScaleYholder);
        mRevertAnimator
                .setInterpolator((TimeInterpolator)new PathInterpolator(0.25F, 0.1F, 0.1F, 1.0F));
        mRevertAnimator.setDuration(ANI_TIME);
        return mRevertAnimator;
    }

}