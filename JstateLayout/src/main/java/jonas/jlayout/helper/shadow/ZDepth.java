package jonas.jlayout.helper.shadow;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

public enum ZDepth {

    Depth0( // TODO
            0,
            0,
            0,
            0,
            0,
            0
    ),

    Depth1(
            30, // alpha to black
            61, // alpha to black
            1.0f, // dp
            1.0f, // dp
            1.5f, // dp
            1.0f  // dp
    ),
    Depth2(
            40,
            58,
            3.0f,
            3.0f,
            3.0f,
            3.0f
    ),
    Depth3(
            48,
            58,
            10.0f,
            6.0f,
            10.0f,
            3.0f
    ),
    Depth4(
            64,
            56,
            14.0f,
            10.0f,
            14.0f,
            5.0f
    ),
    Depth5(
            76,
            56,
            19.0f,
            15.0f,
            19.0f,
            6.0f
    );

    public int mAlphaTopShadow; // alpha to black
    public int mAlphaBottomShadow; // alpha to black

    public final float mOffsetYTopShadow; // dp
    public final float mOffsetYBottomShadow; // dp

    public final float mBlurTopShadow; // dp
    public final float mBlurBottomShadow; // dp

    public float mOffsetYTopShadowPx; // px
    public float mOffsetYBottomShadowPx; // px

    public float mBlurTopShadowPx; // px
    public float mBlurBottomShadowPx; // px

    ZDepth(int alphaTopShadow, int alphaBottomShadow, float offsetYTopShadow, float offsetYBottomShadow, float blurTopShadow, float blurBottomShadow) {
        mAlphaTopShadow = alphaTopShadow;
        mAlphaBottomShadow = alphaBottomShadow;
        mOffsetYTopShadow = offsetYTopShadow;
        mOffsetYBottomShadow = offsetYBottomShadow;
        mBlurTopShadow = blurTopShadow;
        mBlurBottomShadow = blurBottomShadow;
    }

    public int getAlphaTopShadow() {
        return mAlphaTopShadow;
    }

    public int getAlphaBottomShadow() {
        return mAlphaBottomShadow;
    }

    public float getOffsetYTopShadowPx() {
        return dp2px(mOffsetYTopShadow);
    }

    public float getOffsetYBottomShadowPx() {
        return dp2px(mOffsetYBottomShadow);
    }

    public float getBlurTopShadowPx() {
        return dp2px(mBlurTopShadow);
    }

    public float getBlurBottomShadowPx() {
        return dp2px(mBlurBottomShadow);
    }

    public void initZDepth() {
        mOffsetYTopShadowPx = getOffsetYTopShadowPx();
        mOffsetYBottomShadowPx = getOffsetYBottomShadowPx();
        mBlurTopShadowPx =getBlurTopShadowPx();
        mBlurBottomShadowPx = getBlurBottomShadowPx();
    }

    public int getColorTopShadow() {
        return Color.argb(mAlphaTopShadow, 0, 0, 0);
    }

    public int getColorBottomShadow() {
        return Color.argb(mAlphaBottomShadow, 0, 0, 0);
    }


    public static int dp2px(float dpVal){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, Resources.getSystem().getDisplayMetrics());
    }

    public static int sp2px(float dpVal){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpVal, Resources.getSystem().getDisplayMetrics());
    }

}
