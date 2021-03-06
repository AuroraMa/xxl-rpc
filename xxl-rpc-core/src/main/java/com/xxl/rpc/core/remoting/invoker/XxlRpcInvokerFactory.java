package com.xxl.rpc.core.remoting.invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.rpc.core.registry.Register;
import com.xxl.rpc.core.registry.impl.LocalRegister;
import com.xxl.rpc.core.remoting.net.params.BaseCallback;
import com.xxl.rpc.core.remoting.net.params.XxlRpcFutureResponse;
import com.xxl.rpc.core.remoting.net.params.XxlRpcResponse;
import com.xxl.rpc.core.util.XxlRpcException;

/**
 * xxl-rpc invoker factory, init service-registry
 *
 * @author xuxueli 2018-10-19
 */
public class XxlRpcInvokerFactory {
    private static Logger logger = LoggerFactory.getLogger(XxlRpcInvokerFactory.class);

    // ---------------------- default instance ----------------------

    private static volatile XxlRpcInvokerFactory instance = new XxlRpcInvokerFactory(LocalRegister.class, null);
    // class.forname
    private Class<? extends Register> serviceRegistryClass;

    // ---------------------- config ----------------------
    private Map<String, String> serviceRegistryParam;
    private Register register;
    private List<BaseCallback> stopCallbackList = new ArrayList<BaseCallback>();
    private ConcurrentMap<String, XxlRpcFutureResponse> futureResponsePool =
        new ConcurrentHashMap<String, XxlRpcFutureResponse>();

    // ---------------------- start / stop ----------------------
    private ThreadPoolExecutor responseCallbackThreadPool = null;

    public XxlRpcInvokerFactory() {}

    // ---------------------- service registry ----------------------

    public XxlRpcInvokerFactory(Class<? extends Register> serviceRegistryClass,
        Map<String, String> serviceRegistryParam) {
        this.serviceRegistryClass = serviceRegistryClass;
        this.serviceRegistryParam = serviceRegistryParam;
    }

    public static XxlRpcInvokerFactory getInstance() {
        return instance;
    }

    // ---------------------- service registry ----------------------

    public void start() throws Exception {
        // start registry
        if (serviceRegistryClass != null) {
            register = serviceRegistryClass.newInstance();
            register.start(serviceRegistryParam);
        }
    }

    public void stop() throws Exception {
        // stop registry
        if (register != null) {
            register.stop();
        }

        // stop callback
        if (stopCallbackList.size() > 0) {
            for (BaseCallback callback : stopCallbackList) {
                try {
                    callback.run();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        // stop CallbackThreadPool
        stopCallbackThreadPool();
    }

    // ---------------------- future-response pool ----------------------

    // XxlRpcFutureResponseFactory

    public Register getRegister() {
        return register;
    }

    public void addStopCallBack(BaseCallback callback) {
        stopCallbackList.add(callback);
    }

    public void setInvokerFuture(String requestId, XxlRpcFutureResponse futureResponse) {
        futureResponsePool.put(requestId, futureResponse);
    }

    public void removeInvokerFuture(String requestId) {
        futureResponsePool.remove(requestId);
    }

    // ---------------------- response callback ThreadPool ----------------------

    public void notifyInvokerFuture(String requestId, final XxlRpcResponse xxlRpcResponse) {

        // get
        final XxlRpcFutureResponse futureResponse = futureResponsePool.get(requestId);
        if (futureResponse == null) {
            return;
        }

        // notify
        if (futureResponse.getInvokeCallback() != null) {

            // callback type
            try {
                executeResponseCallback(new Runnable() {
                    @Override
                    public void run() {
                        if (xxlRpcResponse.getErrorMsg() != null) {
                            futureResponse.getInvokeCallback()
                                .onFailure(new XxlRpcException(xxlRpcResponse.getErrorMsg()));
                        } else {
                            futureResponse.getInvokeCallback().onSuccess(xxlRpcResponse.getResult());
                        }
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {

            // other nomal type
            futureResponse.setResponse(xxlRpcResponse);
        }

        // do remove
        futureResponsePool.remove(requestId);

    }

    public void executeResponseCallback(Runnable runnable) {

        if (responseCallbackThreadPool == null) {
            synchronized (this) {
                if (responseCallbackThreadPool == null) {
                    responseCallbackThreadPool = new ThreadPoolExecutor(10, 100, 60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>(1000), new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r,
                                    "xxl-rpc, XxlRpcInvokerFactory-responseCallbackThreadPool-" + r.hashCode());
                            }
                        }, new RejectedExecutionHandler() {
                            @Override
                            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                throw new XxlRpcException("xxl-rpc Invoke Callback Thread pool is EXHAUSTED!");
                            }
                        }); // default maxThreads 300, minThreads 60
                }
            }
        }
        responseCallbackThreadPool.execute(runnable);
    }

    public void stopCallbackThreadPool() {
        if (responseCallbackThreadPool != null) {
            responseCallbackThreadPool.shutdown();
        }
    }

}
