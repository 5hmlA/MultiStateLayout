package jonas.jlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * @another 江祖赟
 * @date 2018/2/7.
 * @from    https://zhuanlan.zhihu.com/p/20689459
 */
public class JConstraintLayout211 extends ConstraintLayout {

    private Drawable mForegroundDrawable;
    private float[] radii = new float[8];   // top-left, top-right, bottom-right, bottom-left
    private Path mClipPath;                 // 剪裁区域路径
    private Paint mPaint;                   // 画笔
    private boolean mRoundAsCircle = false; // 圆形
    private int mStrokeColor;               // 描边颜色
    private int mStrokeWidth;               // 描边半径
    private Region mAreaRegion;             // 内容区域
    private int mEdgeFix = 10;              // 边缘修复
    private RectF mLayer;                   // 画布图层大小
    {
        mClipPath = new Path();
        mAreaRegion = new Region();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
    }

    public JConstraintLayout211(Context context){
        super(context);
    }

    public JConstraintLayout211(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public JConstraintLayout211(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        mForegroundDrawable = a.getDrawable(0);
        if (mForegroundDrawable != null) {
            //set a callback, or the selector won't be animated
            mForegroundDrawable.setCallback(this);
        }
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        mForegroundDrawable.setBounds(0, 0, width, height);

        mLayer = new RectF(0, 0, width, height);
        RectF areas = new RectF(0, 0, width, height);
//        areas.left = getPaddingLeft();
//        areas.top = getPaddingTop();
//        areas.right = width - getPaddingRight();
//        areas.bottom = height - getPaddingBottom();
        mClipPath.reset();
        if (mRoundAsCircle) {
            float d = areas.width() >= areas.height() ? areas.height() : areas.width();
            float r = d / 2;
            PointF center = new PointF(width / 2, height / 2);
            mClipPath.addCircle(center.x, center.y, r, Path.Direction.CW);
            mClipPath.moveTo(-mEdgeFix, -mEdgeFix);  // 通过空操作让Path区域占满画布
            mClipPath.moveTo(width + mEdgeFix, height + mEdgeFix);
        } else {
            mClipPath.addRoundRect(areas, radii, Path.Direction.CW);
        }
        Region clip = new Region((int) areas.left, (int) areas.top,
                (int) areas.right, (int) areas.bottom);
        mAreaRegion.setPath(mClipPath, clip);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(mLayer, null, Canvas.ALL_SAVE_FLAG);

        super.dispatchDraw(canvas);
        mForegroundDrawable.draw(canvas);

        if(mStrokeWidth>0) {
            // 支持半透明描边，将与描边区域重叠的内容裁剪掉
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(mStrokeWidth*2);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mClipPath, mPaint);
            // 绘制描边
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            mPaint.setColor(mStrokeColor);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mClipPath, mPaint);
        }
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mClipPath, mPaint);
        canvas.restore();
    }

    @Override public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mClipPath);
        super.draw(canvas);
        canvas.restore();
    }

    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mAreaRegion.contains((int) ev.getX(), (int) ev.getY())) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

//    Then we have to Override jumpDrawablesToCurrentState to indicate our selector to do
//    transition animations between states, and verifyDrawable to indicate the view we are displaying our own drawable.
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mForegroundDrawable);
    }

    @TargetApi(11)
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        mForegroundDrawable.jumpToCurrentState();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (mForegroundDrawable != null) {
            mForegroundDrawable.setHotspot(x, y);
        }
    }

//    In the View class, a method is called each time the state of the view changes. This method is drawableStateChanged()
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mForegroundDrawable.setState(getDrawableState());
        //redraw
        invalidate();
    }

}
