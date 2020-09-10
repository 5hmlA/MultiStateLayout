package jonas.jlayout;

/**
 * @author  江祖赟
 * @date 2018/1/13 0013.14:20
 */
interface StateEventInterceptor {
    boolean onRetry();

    boolean onShowLoading();

    boolean onShowEmpty();

    boolean onShowError();

    boolean onShowExcept();
}
