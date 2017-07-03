package jonas.jlayout;

/**
 * @author yun.
 * @date 2016/7/18
 * @des [一句话描述]
 * @since [https://github.com/mychoices]
 * <p><a href="https://github.com/mychoices">github</a>
 */
public interface OnStateClickListener {
    void onRetry(@MultiStateLayout.LayoutState int layoutState);
    void onLoadingCancel();
}
