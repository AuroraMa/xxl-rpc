package com.xxl.rpc.core.remoting.invoker.call;

/**
 * @author xuxueli 2018-10-23
 */
public abstract class XxlRpcInvokeCallback<T> {

    private static ThreadLocal<XxlRpcInvokeCallback> threadInvokerFuture = new ThreadLocal<XxlRpcInvokeCallback>();

    /**
     * get callback
     *
     * @return
     */
    public static XxlRpcInvokeCallback getCallback() {
        XxlRpcInvokeCallback invokeCallback = threadInvokerFuture.get();
        threadInvokerFuture.remove();
        return invokeCallback;
    }

    // ---------------------- thread invoke callback ----------------------

    /**
     * set future
     *
     * @param invokeCallback
     */
    public static void setCallback(XxlRpcInvokeCallback invokeCallback) {
        threadInvokerFuture.set(invokeCallback);
    }

    /**
     * remove future
     */
    public static void removeCallback() {
        threadInvokerFuture.remove();
    }

    public abstract void onSuccess(T result);

    public abstract void onFailure(Throwable exception);

}
