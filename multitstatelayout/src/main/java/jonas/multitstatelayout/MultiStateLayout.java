package jonas.multitstatelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static jonas.multitstatelayout.MultiStateLayout.LayoutState.STATE_EMPTY;
import static jonas.multitstatelayout.MultiStateLayout.LayoutState.STATE_ERROR;
import static jonas.multitstatelayout.MultiStateLayout.LayoutState.STATE_EXCEPT;
import static jonas.multitstatelayout.MultiStateLayout.LayoutState.STATE_LOADING;
import static jonas.multitstatelayout.MultiStateLayout.LayoutState.STATE_UNMODIFY;

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

    @Override
    public void onClick(View v){
        int id = v.getId();
        if(id == R.id.j_multity_retry) {
            logdOut("onClick reloading");
            showStateLayout(STATE_LOADING);
            if(mL != null) {
                mL.onRetry(mLayoutState);
            }
        }else {
            if(mLoadingCancel) {
                logdOut("onClick cancel loading");
                showStateLayout(STATE_EXCEPT);
                if(mL != null) {
                    mL.onLoadingCancel();
                }
            }
        }
    }

    private void logdOut(String slog){
        if(BuildConfig.DEBUG) {
            Log.d(TAG, slog);
        }
    }

    public MultiStateLayout(Context context){
        super(context);
    }

    public MultiStateLayout(Context context, AttributeSet attrs){
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiStateLayout);
        layout_error_resid = a.getResourceId(R.styleable.MultiStateLayout_error, R.layout.j_multitylayout_error);
        layout_loading_resid = a.getResourceId(R.styleable.MultiStateLayout_loading, R.layout.j_multitylayout_loading);
        layout_empty_resid = a.getResourceId(R.styleable.MultiStateLayout_empty, R.layout.j_multitylayout_empty);
        mLayoutState = a.getInt(R.styleable.MultiStateLayout_state, LayoutState.STATE_UNMODIFY);
        a.recycle();
    }

    public MultiStateLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        mContext = getContext();
        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        showStateLayout(mLayoutState);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public MultiStateLayout showStateLayout(@LayoutState int state){
        mLayoutState = state;
        if(mLayoutState == STATE_UNMODIFY || mLayoutState == STATE_LOADING) {
            goneOthers(mErrorLayout);
            goneOthers(mEmptyLayout);
            if(mLoadingLayout == null) {
                createLoadingLayout();
                mLoadingLayout.setOnClickListener(this);
            }else {
                bringChildToFront(mLoadingLayout);
            }
        }else if(mLayoutState == STATE_EMPTY) {
            goneOthers(mLoadingLayout);
            goneOthers(mErrorLayout);
            if(mEmptyLayout == null) {
                createEmptyLayout();
                mEmptyLayout.findViewById(R.id.j_multity_retry).setOnClickListener(this);
            }else {
                bringChildToFront(mEmptyLayout);
            }
        }else if(mLayoutState == STATE_ERROR) {
            goneOthers(mLoadingLayout);
            goneOthers(mEmptyLayout);
            if(mErrorLayout == null) {
                createErrorLayout();
                mErrorLayout.findViewById(R.id.j_multity_retry).setOnClickListener(this);
            }else {
                bringChildToFront(mErrorLayout);
            }
        }else {
            //            if (mLayoutState == STATE_EXCEPT)
            goneOthers(mLoadingLayout);
            goneOthers(mErrorLayout);
            goneOthers(mEmptyLayout);
            if(mExceptLayout != null) {
                bringChildToFront(mExceptLayout);
            }
        }
        return this;
    }

    private void goneOthers(View view){
        if(view != null) {
            view.setVisibility(GONE);
        }
    }

    @Override
    public void bringChildToFront(View child){
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

    private View createLayout(int layoutid){
        View inflateView = LayoutInflater.from(mContext).inflate(layoutid, this, false);
        ViewGroup.LayoutParams layoutParams = inflateView.getLayoutParams();
        if(!( layoutParams instanceof RelativeLayout.LayoutParams )) {
            layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        }
        addView(inflateView, layoutParams);
        return inflateView;
    }

    public MultiStateLayout setOnStateClickListener(OnStateClickListener l){
        mL = l;
        return this;
    }
}
