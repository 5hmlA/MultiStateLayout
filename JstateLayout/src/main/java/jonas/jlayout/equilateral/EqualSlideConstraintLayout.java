package jonas.jlayout.equilateral;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.view.View.MeasureSpec.EXACTLY;

/**
 * @author yun.
 * @date 2020/9/10 0010
 * @des [一句话描述]
 * @since [https://github.com/ZuYun]
 * <p><a href="https://github.com/ZuYun">github</a>
 */
public class EqualSlideConstraintLayout extends androidx.constraintlayout.widget.ConstraintLayout {

    public EqualSlideConstraintLayout(@NonNull Context context) {
        super(context);
    }

    public EqualSlideConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EqualSlideConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int slide = Math.min(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(MeasureSpec.makeMeasureSpec(slide, EXACTLY), MeasureSpec.makeMeasureSpec(slide, EXACTLY));

    }

    //<editor-fold desc="debug for show sth">
//        Paint paint = new Paint();
//    {
//        paint.setTextSize(dp2px(20));
//        paint.setTextAlign(Paint.Align.CENTER);
//    }
//
//    @Override
//    public void onDrawForeground(Canvas canvas) {
//        super.onDrawForeground(canvas);
//        int measuredWidth = getMeasuredWidth();
//        int height = getMeasuredHeight();
//        String wh = String.format("%d_%d", measuredWidth, height);
//        canvas.drawText(wh, measuredWidth/2, height/2, paint);
//    }
    //</editor-fold>
}
