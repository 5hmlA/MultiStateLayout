package jonas.jlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static jonas.jlayout.MultiStateLayout.LayoutState.STATE_EMPTY;
import static jonas.jlayout.MultiStateLayout.LayoutState.STATE_ERROR;
import static jonas.jlayout.MultiStateLayout.LayoutState.STATE_EXCEPT;
import static jonas.jlayout.MultiStateLayout.LayoutState.STATE_LOADING;
import static jonas.jlayout.MultiStateLayout.LayoutState.STATE_UNMODIFY;

/**
 * @author yun.
 * @date 2016/7/18
 * @des [一句话描述]
 * @since [https://github.com/mychoices]
 * <p><a href="https://github.com/mychoices">github</a>
 */
public class MultiStateLayout extends RelativeLayout implements View.OnClickListener {

    private OnStateClickListener mL;
    private PointF mDown;
    private PointF mCenter;
    private RevealHelper mRevealHelper;
    private int mLoadingClor;

    @IntDef({STATE_UNMODIFY, STATE_LOADING, STATE_ERROR, STATE_EMPTY, STATE_EXCEPT})
    public @interface LayoutState {
        int STATE_UNMODIFY = -1;
        int STATE_LOADING = 0;
        int STATE_ERROR = 1;
        int STATE_EMPTY = 2;
        int STATE_EXCEPT = 3;
    }

    //    @LayoutState
    private int mLayoutState = STATE_UNMODIFY;
    private int layout_error_resid;
    private int layout_empty_resid;
    private int layout_loading_resid;
    public static final String TAG = MultiStateLayout.class.getSimpleName();
    private Context mContext;
    private View mLoadingLayout;
    private View mEmptyLayout;
    private View mErrorLayout;
    private View mExceptLayout;
    private boolean mLoadingCancel;
    private boolean mRevealable;

    @Override
    public void onClick(View v){
        int id = v.getId();
        if(id == R.id.j_multity_retry) {
            showStateLayout(STATE_LOADING);
            if(mL != null) {
                mL.onRetry(mLayoutState);
            }
        }else {
            if(mLoadingCancel) {
                showStateLayout(STATE_EXCEPT);
                if(mL != null) {
                    mL.onLoadingCancel();
                }
            }
        }
    }

    public MultiStateLayout(Context context){
        super(context, null);
    }

    public MultiStateLayout(Context context, AttributeSet attrs){
        this(context, attrs, R.attr.jmultistate);
    }

    public MultiStateLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        TypedArray a = context
                .obtainStyledAttributes(attrs, R.styleable.MultiStateLayout, defStyleAttr, R.style.Jmultistate_style);
        //        layout_error_resid = a.getResourceId(R.styleable.MultiStateLayout_error, R.layout.j_multitylayout_error);
        //        layout_empty_resid = a.getResourceId(R.styleable.MultiStateLayout_empty, R.layout.j_multitylayout_empty);
        //        layout_loading_resid = a.getResourceId(R.styleable.MultiStateLayout_loading, R.layout.j_multitylayout_loading);
        layout_error_resid = a.getResourceId(R.styleable.MultiStateLayout_error, View.NO_ID);
        layout_loading_resid = a.getResourceId(R.styleable.MultiStateLayout_loading, View.NO_ID);
        layout_empty_resid = a.getResourceId(R.styleable.MultiStateLayout_empty, View.NO_ID);
        mLayoutState = a.getInt(R.styleable.MultiStateLayout_state, LayoutState.STATE_UNMODIFY);
        a.recycle();
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        mContext = getContext();
        showStateLayout2(mLayoutState);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mCenter = new PointF(w/2f, h/2f);
        mRevealHelper = RevealHelper.create(w, h, this).setMinRadius(dp2px(40));
    }

    public MultiStateLayout showStateLayout(@LayoutState int state){
        if(mLayoutState != state) {
            showStateLayout2(state);
        }
        return this;
    }

    private MultiStateLayout showStateLayout2(@LayoutState int state){
        mLayoutState = state;
        if(mLayoutState == STATE_LOADING) {
            if(mLoadingLayout == null) {
                createLoadingLayout();
            }else {
                visibleState(mLoadingLayout);
            }
            if(mLoadingLayout != null) {
                mLoadingLayout.setOnClickListener(this);
            }
            goneOthers(mErrorLayout);
            goneOthers(mEmptyLayout);
            bringChildToFront(mLoadingLayout);
            if(mLoadingClor != 0) {
                mLoadingLayout.setBackgroundColor(mLoadingClor);
            }
        }else if(mLayoutState == STATE_EMPTY) {
            if(mEmptyLayout == null) {
                createEmptyLayout();
                if(mEmptyLayout != null) {
                    if(mEmptyLayout.findViewById(R.id.j_multity_retry) != null) {
                        mEmptyLayout.findViewById(R.id.j_multity_retry).setOnClickListener(this);
                    }
                }
            }else {
                visibleState(mEmptyLayout);
            }
            goneOthers(mLoadingLayout);
            goneOthers(mErrorLayout);
            bringChildToFront(mEmptyLayout);
        }else if(mLayoutState == STATE_ERROR) {
            if(mErrorLayout == null) {
                createErrorLayout();
                if(mErrorLayout != null) {
                    if(mErrorLayout.findViewById(R.id.j_multity_retry) != null) {
                        mErrorLayout.findViewById(R.id.j_multity_retry).setOnClickListener(this);
                    }
                }
            }else {
                visibleState(mErrorLayout);
            }
            goneOthers(mLoadingLayout);
            goneOthers(mEmptyLayout);
            bringChildToFront(mErrorLayout);
        }else if(mLayoutState == STATE_EXCEPT) {
            if(mExceptLayout != null) {
                visibleState(mExceptLayout);
            }
            goneOthers(mLoadingLayout);
            goneOthers(mErrorLayout);
            goneOthers(mEmptyLayout);
        }
        if(mRevealHelper != null) {
            if(mLayoutState != STATE_LOADING) {
                mRevealHelper.expandRevealAni(mCenter.x, mCenter.y);
            }else {
                mRevealHelper.cutRevealAni(mCenter.x, mCenter.y);
            }
        }
        return this;
    }

    private void goneOthers(View view){
        if(view != null) {
            view.setVisibility(GONE);
        }
    }

    public void visibleState(View child){
        child.setVisibility(VISIBLE);
        //        if (indexOfChild(child) < getChildCount()-1) {
        //            super.bringChildToFront(child);
        //        }
    }

    private void createErrorLayout(){
        mErrorLayout = createLayout(layout_error_resid);
    }

    private void createEmptyLayout(){
        mEmptyLayout = createLayout(layout_empty_resid);
    }

    private void createLoadingLayout(){
        mLoadingLayout = createLayout(layout_loading_resid);
    }

    public MultiStateLayout CustomStateLayout(View view, @LayoutState int state){
        return CustomStateLayout(view, state, false);
    }

    /**
     * 设置 自定义 状态布局
     *
     * @param view
     * @param state
     *         对应的状态
     * @param showState
     *         是否立刻显示该状态
     */
    public MultiStateLayout CustomStateLayout(View view, @LayoutState int state, boolean showState){
        if(state == STATE_LOADING) {
            if(mLoadingLayout != null) {
                removeView(mLoadingLayout);
            }
            mLoadingLayout = view;
        }else if(state == STATE_EMPTY) {
            if(mEmptyLayout != null) {
                removeView(mEmptyLayout);
            }
            mEmptyLayout = view;
        }else if(state == STATE_ERROR) {
            if(mErrorLayout != null) {
                removeView(mErrorLayout);
            }
            mErrorLayout = view;
        }else if(state == STATE_EXCEPT) {
            if(mExceptLayout != null) {
                removeView(mExceptLayout);
            }
            mExceptLayout = view;
        }
        addView(view, -1, -1);
        if(showState) {
            showStateLayout(state);
        }
        return this;
    }

    /**
     * 设置 自定义 状态布局
     *
     * @param layutID
     *         布局id
     * @param state
     *         对应的状态
     */
    public MultiStateLayout CustomStateLayout(@LayoutRes int layutID, @LayoutState int state){
        return CustomStateLayout(layutID, state, false);
    }

    /**
     * 设置 自定义 状态布局
     *
     * @param layutID
     *         布局id
     * @param state
     *         对应的状态
     * @param showState
     *         是否立刻显示该状态
     */
    public MultiStateLayout CustomStateLayout(@LayoutRes int layutID, @LayoutState int state, boolean showState){
        if(state == STATE_LOADING) {
            if(mLoadingLayout != null) {
                removeView(mLoadingLayout);
            }
            mLoadingLayout = createLayout(layutID);

        }else if(state == STATE_EMPTY) {
            if(mEmptyLayout != null) {
                removeView(mEmptyLayout);
            }
            mEmptyLayout = createLayout(layutID);

        }else if(state == STATE_ERROR) {
            if(mErrorLayout != null) {
                removeView(mErrorLayout);
            }
            mErrorLayout = createLayout(layutID);

        }else if(state == STATE_EXCEPT) {
            if(mExceptLayout != null) {
                removeView(mExceptLayout);
            }
            mExceptLayout = createLayout(layutID);
        }
        if(showState) {
            showStateLayout(state);
        }
        return this;
    }

    public MultiStateLayout setLoadingCancelAble(boolean loadingCancel){
        mLoadingCancel = loadingCancel;
        return this;
    }

    public boolean isRevealable(){
        return mRevealable;
    }

    public MultiStateLayout setRevealable(boolean revealable){
        mRevealable = revealable;
        return this;
    }

    private View createLayout(int layoutid){
        View inflateView = LayoutInflater.from(mContext).inflate(layoutid, this, false);
        ViewGroup.LayoutParams layoutParams = inflateView.getLayoutParams();
        if(!( layoutParams instanceof RelativeLayout.LayoutParams )) {
            layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        }
        inflateView.setClickable(true);
        addView(inflateView, layoutParams);
        return inflateView;
    }

    public MultiStateLayout setOnStateClickListener(OnStateClickListener l){
        mL = l;
        return this;
    }

    @Override
    protected void dispatchDraw(Canvas canvas){
        if(mRevealHelper != null && mRevealable) {
            mRevealHelper.clipReveal(canvas);
        }
        super.dispatchDraw(canvas);
    }

    private int dp2px(float dpVal){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }

    private int sp2px(float dpVal){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpVal, getResources().getDisplayMetrics());
    }

    public void showStateSucceed(){
        showStateLayout(MultiStateLayout.LayoutState.STATE_EXCEPT);
    }

    public void showStateLoading(){
        showStateLayout(MultiStateLayout.LayoutState.STATE_LOADING);
    }

    public void showStateLoading(@ColorInt int loadingClor){
        mLoadingClor = loadingClor;
        showStateLayout(MultiStateLayout.LayoutState.STATE_LOADING);
    }

    public void showStateEmpty(){
        showStateLayout(MultiStateLayout.LayoutState.STATE_EMPTY);
    }

    public void showStateError(){
        showStateLayout(LayoutState.STATE_ERROR);
    }

    public View getLoadingLayout(){
        return mLoadingLayout;
    }

    public View getErrorLayout(){
        return mErrorLayout;
    }

    public View getEmptyLayout(){
        return mEmptyLayout;
    }

    public
    @LayoutState
    int getLayoutState(){
        return mLayoutState;
    }

    public boolean isShowSucceed(){
        return mLayoutState == LayoutState.STATE_EXCEPT;
    }
}
