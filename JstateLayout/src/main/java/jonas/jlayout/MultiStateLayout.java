package jonas.jlayout;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
  private int mLoadingBgClor = STATE_UNMODIFY;
  private TextView mErrorLayoutTips;
  private CharSequence mErrorTips;
  private CharSequence mLoadingTips;
  private CharSequence mEmptyTips;
  private TextView mEmptyLayoutTips;
  private TextView mLoadingLayoutTips;

  @Retention(RetentionPolicy.SOURCE)
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
  private View mCurrentStateLayout;
  private boolean mLoadingCancel;
  private boolean mRevealable;
  private long mLastRetryTime;

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.j_multity_retry) {
      if (mL != null && (mLastRetryTime == 0 || System.currentTimeMillis() - mLastRetryTime > 600)) {
        mLastRetryTime = System.currentTimeMillis();
        showStateLayout(STATE_LOADING);
        mL.onRetry(mLayoutState);
      }
    } else {
      if (mLoadingCancel) {
        showStateLayout(STATE_EXCEPT);
        if (mL != null) {
          mL.onLoadingCancel();
        }
      }
    }
  }

  public MultiStateLayout(Context context) {
    super(context, null);
  }

  public MultiStateLayout(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.jmultistate);
  }

  public MultiStateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiStateLayout, defStyleAttr, R.style.Jmultistate_style);
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
  protected void onFinishInflate() {
    super.onFinishInflate();
    mContext = getContext();
    if (mLayoutState != STATE_UNMODIFY) {
      showStateLayout2(mLayoutState);
    }
  }

  @Override
  public void addView(View child) {
    super.addView(child);
    if (mCurrentStateLayout != null) {
      bringChildToFront(mCurrentStateLayout);
    }
  }

  @Override
  public void addView(View child, ViewGroup.LayoutParams params) {
    super.addView(child, params);
    if (mCurrentStateLayout != null) {
      bringChildToFront(mCurrentStateLayout);
    }
  }

  @Override
  public void addView(View child, int width, int height) {
    super.addView(child, width, height);
    if (mCurrentStateLayout != null) {
      bringChildToFront(mCurrentStateLayout);
    }
  }

  @Override
  public void addView(View child, int index) {
    super.addView(child, index);
    if (mCurrentStateLayout != null) {
      bringChildToFront(mCurrentStateLayout);
    }
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    super.addView(child, index, params);
    if (mCurrentStateLayout != null) {
      bringChildToFront(mCurrentStateLayout);
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mCenter = new PointF(w / 2f, h / 2f);
    mRevealHelper = RevealHelper.create(w, h, this).setMinRadius(dp2px(40));
  }

  public MultiStateLayout showStateLayout(@LayoutState int state) {
    if (mLayoutState != state) {
      showStateLayout2(state);
    }
    return this;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  public void changeBgColor(View view, int color) {
    if (view instanceof ViewGroup) {
      view.setBackgroundColor(color);
      for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
        changeBgColor(((ViewGroup) view).getChildAt(i), color);
      }
    }
  }

  private synchronized MultiStateLayout showStateLayout2(@LayoutState int state) {
    mLayoutState = state;
    if (mLayoutState == STATE_LOADING) {
      if (mLoadingLayout == null) {
        createLoadingLayout();
      } else {
        visibleState(mCurrentStateLayout = mLoadingLayout);
      }
      if (mLoadingLayout != null) {
        mLoadingLayout.setOnClickListener(this);
        if ((mLoadingLayoutTips = mLoadingLayout.findViewById(R.id.j_multity_loading_msg)) != null && !TextUtils.isEmpty(mLoadingTips)) {
          mLoadingLayoutTips.setText(mLoadingTips);
        }
      }
      mCurrentStateLayout = mLoadingLayout;
      goneOthers(mErrorLayout);
      goneOthers(mEmptyLayout);
      if (mLoadingBgClor != STATE_UNMODIFY) {
        changeBgColor(mLoadingLayout, mLoadingBgClor);
      }
    } else if (mLayoutState == STATE_EMPTY) {
      if (mEmptyLayout == null) {
        createEmptyLayout();
        if (mEmptyLayout != null) {
          if (mEmptyLayout.findViewById(R.id.j_multity_retry) != null) {
            mEmptyLayout.findViewById(R.id.j_multity_retry).setOnClickListener(this);
          }
          if ((mEmptyLayoutTips = mEmptyLayout.findViewById(R.id.j_multity_empt_msg)) != null && !TextUtils.isEmpty(mEmptyTips)) {
            mEmptyLayoutTips.setText(mEmptyTips);
          }
        }
      } else {
        visibleState(mCurrentStateLayout = mEmptyLayout);
      }
      mCurrentStateLayout = mEmptyLayout;
      goneOthers(mLoadingLayout);
      goneOthers(mErrorLayout);
    } else if (mLayoutState == STATE_ERROR) {
      if (mErrorLayout == null) {
        createErrorLayout();
        if (mErrorLayout != null) {
          if (mErrorLayout.findViewById(R.id.j_multity_retry) != null) {
            mErrorLayout.findViewById(R.id.j_multity_retry).setOnClickListener(this);
          }
          if ((mErrorLayoutTips = mErrorLayout.findViewById(R.id.j_multity_error_msg)) != null && !TextUtils.isEmpty(mErrorTips)) {
            mErrorLayoutTips.setText(mErrorTips);
          }
        }
      } else {
        visibleState(mCurrentStateLayout = mErrorLayout);
      }
      mCurrentStateLayout = mErrorLayout;
      goneOthers(mLoadingLayout);
      goneOthers(mEmptyLayout);
    } else if (mLayoutState == STATE_EXCEPT) {
      mCurrentStateLayout = null;
      if (mExceptLayout != null) {
        visibleState(mExceptLayout);
      }
      goneOthers(mLoadingLayout);
      goneOthers(mErrorLayout);
      goneOthers(mEmptyLayout);
    }
    if (mRevealHelper != null) {
      if (mLayoutState != STATE_LOADING) {
        mRevealHelper.expandRevealAni(mCenter.x, mCenter.y);
      } else {
        mRevealHelper.cutRevealAni(mCenter.x, mCenter.y);
      }
    }
    return this;
  }

  private void goneOthers(View view) {
    if (view != null) {
      removeView(view);
      //            view.setVisibility(GONE);
    }
  }

  public void visibleState(View child) {
    if (indexOfChild(child) > 0) {
      bringChildToFront(child);
    } else {
      addView(child, -1, -1);
    }
    //            child.setVisibility(VISIBLE);
    //            bringChildToFront(child);
  }

  private void createErrorLayout() {
    mErrorLayout = createLayout(layout_error_resid);
  }

  private void createEmptyLayout() {
    mEmptyLayout = createLayout(layout_empty_resid);
  }

  private void createLoadingLayout() {
    mLoadingLayout = createLayout(layout_loading_resid);
  }

  /**
   * 设置 自定义 状态布局
   *
   * @param state 对应的状态
   * @param showState 是否立刻显示该状态
   */
  public MultiStateLayout CustomStateLayout(View view, @LayoutState int state, boolean showState) {
    if (state == STATE_LOADING) {
      if (mLoadingLayout != null) {
        removeView(mLoadingLayout);
      }
      mLoadingLayout = view;
    } else if (state == STATE_EMPTY) {
      if (mEmptyLayout != null) {
        removeView(mEmptyLayout);
      }
      mEmptyLayout = view;
    } else if (state == STATE_ERROR) {
      if (mErrorLayout != null) {
        removeView(mErrorLayout);
      }
      mErrorLayout = view;
    } else if (state == STATE_EXCEPT) {
      if (mExceptLayout != null) {
        removeView(mExceptLayout);
      }
      mExceptLayout = view;
    }
    if (showState) {
      showStateLayout2(state);
    }
    return this;
  }

  /**
   * 设置 自定义 状态布局<br>
   *
   * @param layutID 布局id
   * @param state 对应的状态
   */
  public MultiStateLayout registStateLayout(@LayoutRes int layutID, @LayoutState int state) {
    if (state == STATE_EMPTY) {
      layout_empty_resid = layutID;
    } else if (state == STATE_ERROR) {
      layout_error_resid = layutID;
    } else if (state == STATE_LOADING) {
      layout_loading_resid = layutID;
      if (mLayoutState == STATE_LOADING) {
        //如果当前是loading的话 需要移除原来的loading 重新加载新的loading,因为一开始可能就是loading状态(不可能会是empty,error)
        CustomStateLayout(LayoutInflater.from(getContext()).inflate(layout_loading_resid, this, false), state, true);
      }
    }
    return this;
  }

  public MultiStateLayout registStateLayout(View custView, @LayoutState int state) {
    if (state == STATE_EMPTY) {
      mEmptyLayout = custView;
    } else if (state == STATE_ERROR) {
      mErrorLayout = custView;
    } else if (state == STATE_LOADING) {
      if (mLayoutState == STATE_LOADING) {
        CustomStateLayout(custView, state, true);
      } else {
        mLoadingLayout = custView;
      }
    } else if (state == STATE_EXCEPT) {
      mExceptLayout = custView;
    }
    return this;
  }

  /**
   * 设置 自定义 状态布局
   *
   * @param layutID 布局id
   * @param state 对应的状态
   * @param showState 是否立刻显示该状态
   */
  public MultiStateLayout CustomStateLayout(@LayoutRes int layutID, @LayoutState int state, boolean showState) {
    return CustomStateLayout(LayoutInflater.from(getContext()).inflate(layutID, this, false), state, showState);
  }

  public MultiStateLayout setLoadingCancelAble(boolean loadingCancel) {
    mLoadingCancel = loadingCancel;
    return this;
  }

  public boolean isRevealable() {
    return mRevealable;
  }

  public MultiStateLayout setRevealable(boolean revealable) {
    mRevealable = revealable;
    return this;
  }

  private View createLayout(int layoutid) {
    View inflateView = mCurrentStateLayout = LayoutInflater.from(mContext).inflate(layoutid, this, false);
    inflateView.setClickable(true);
    //        ViewGroup.LayoutParams layoutParams = inflateView.getLayoutParams();
    //        if(!( layoutParams instanceof RelativeLayout.LayoutParams )) {
    //            layoutParams = new RelativeLayout.LayoutParams(-1, -1);
    //        }
    //        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    //            inflateView.setElevation(26);
    //        }
    addView(inflateView, -1, -1);
    return inflateView;
  }

  public MultiStateLayout setOnStateClickListener(OnStateClickListener l) {
    mL = l;
    return this;
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    if (mRevealHelper != null && mRevealable) {
      mRevealHelper.clipReveal(canvas);
    }
    super.dispatchDraw(canvas);
  }

  public static int dp2px(float dpVal) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, Resources.getSystem().getDisplayMetrics());
  }

  public static int sp2px(float dpVal) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpVal, Resources.getSystem().getDisplayMetrics());
  }

  public void showStateSucceed() {
    showStateLayout(MultiStateLayout.LayoutState.STATE_EXCEPT);
  }

  public void showStateLoading() {
    showStateLayout(MultiStateLayout.LayoutState.STATE_LOADING);
  }

  public void setLoadingPageBgColor(@ColorInt int loadingClor) {
    mLoadingBgClor = loadingClor;
    if (mLoadingLayout != null && mLoadingBgClor != STATE_UNMODIFY) {
      changeBgColor(mLoadingLayout, mLoadingBgClor);
    }
  }

  public void setLoadingTips(CharSequence loadingTips) {
    mLoadingTips = loadingTips;
    if (mLoadingLayoutTips != null) {
      mLoadingLayoutTips.setText(mLoadingTips);
    }
  }

  public void setEmptyTips(CharSequence emptyTips) {
    mEmptyTips = emptyTips;
    if (mEmptyLayoutTips != null) {
      mEmptyLayoutTips.setText(mEmptyTips);
    }
  }

  public void setErrorTips(CharSequence errorTips) {
    mErrorTips = errorTips;
    if (mErrorLayoutTips != null) {
      mErrorLayoutTips.setText(mErrorTips);
    }
  }

  public void showStateEmpty() {
    showStateLayout(MultiStateLayout.LayoutState.STATE_EMPTY);
  }

  public void showStateError() {
    showStateLayout(LayoutState.STATE_ERROR);
  }

  public View getLoadingLayout() {
    return mLoadingLayout;
  }

  public View getErrorLayout() {
    return mErrorLayout;
  }

  public View getEmptyLayout() {
    return mEmptyLayout;
  }

  @LayoutState
  public int getCurrentState() {
    return mLayoutState;
  }

  public boolean isShowEmpty() {
    return mLayoutState == LayoutState.STATE_EMPTY;
  }

  public boolean isShowError() {
    return mLayoutState == LayoutState.STATE_ERROR;
  }

  public boolean isShowSucceed() {
    return mLayoutState == LayoutState.STATE_EXCEPT;
  }

  public boolean isShowLoading() {
    return mLayoutState == LayoutState.STATE_LOADING;
  }
}
