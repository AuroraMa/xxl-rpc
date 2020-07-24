package com.xxl.rpc.core.remoting.provider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.rpc.core.registry.Register;
import com.xxl.rpc.core.remoting.net.Server;
import com.xxl.rpc.core.remoting.net.params.BaseCallback;
import com.xxl.rpc.core.remoting.net.params.XxlRpcRequest;
import com.xxl.rpc.core.remoting.net.params.XxlRpcResponse;
import com.xxl.rpc.core.util.IpUtil;
import com.xxl.rpc.core.util.NetUtil;
import com.xxl.rpc.core.util.ThrowableUtil;
import com.xxl.rpc.core.util.XxlRpcException;

/**
 * provider
 *
 * @author xuxueli 2015-10-31 22:54:27
 */
public class XxlRpcProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(XxlRpcProviderFactory.class);

    /**
     * server 启动 基础 config
     */
    private ProviderConfig providerConfig;

    /**
     * server服务接口 map
     */
    private Map<String, Object> serviceData = new HashMap<String, Object>();

    /**
     * start / stop
     */
    public void start() throws Exception {
        logger.debug("XxlRpcProviderFactory.start()");
        // 校验 provider 基础参数
        if (this.providerConfig.getServer() == null) {
            throw new XxlRpcException("xxl-rpc provider server missing.");
        }
        if (this.providerConfig.getSerializer() == null) {
            throw new XxlRpcException("xxl-rpc provider serializer missing.");
        }
        if (!(this.providerConfig.getCorePoolSize() > 0 && this.providerConfig.getMaxPoolSize() > 0
            && this.providerConfig.getMaxPoolSize() >= this.providerConfig.getCorePoolSize())) {
            this.providerConfig.setCorePoolSize(60);
            this.providerConfig.setMaxPoolSize(300);
        }
        if (this.providerConfig.getIp() == null) {
            this.providerConfig.setIp(IpUtil.getIp());
        }
        if (this.providerConfig.getPort() <= 0) {
            this.providerConfig.setPort(7080);
        }
        if (this.providerConfig.getRegistryAddress() == null
            || this.providerConfig.getRegistryAddress().trim().length() == 0) {
            this.providerConfig
                .setRegistryAddress(IpUtil.getIpPort(this.providerConfig.getIp(), this.providerConfig.getPort()));
        }
        if (NetUtil.isPortUsed(this.providerConfig.getPort())) {
            throw new XxlRpcException("xxl-rpc provider port[" + this.providerConfig.getPort() + "] is used.");
        }

        // start server
        Server serverInstance = this.providerConfig.getServer().newInstance();

        // serviceRegistry started
        serverInstance.setStartedCallback(new BaseCallback() {
            @Override
            public void run() throws Exception {
                // start registry
                if (providerConfig.getServiceRegistry() != null) {
                    Register registerInstance = providerConfig.getServiceRegistry().newInstance();
                    registerInstance.start(providerConfig.getServiceRegistryParam());
                    if (serviceData.size() > 0) {
                        registerInstance.registry(serviceData.keySet(), providerConfig.getRegistryAddress());
                    }
                }
            }
        });
        // serviceRegistry stoped
        serverInstance.setStopedCallback(new BaseCallback() {
            @Override
            public void run() throws IllegalAccessException, InstantiationException {
                // stop registry
                Register registerInstance = providerConfig.getServiceRegistry().newInstance();
                if (registerInstance != null) {
                    if (serviceData.size() > 0) {
                        registerInstance.remove(serviceData.keySet(), providerConfig.getRegistryAddress());
                    }
                    registerInstance.stop();
                }
            }
        });
        serverInstance.start(this);
    }

    public void stop() throws Exception {
        logger.debug("XxlRpcProviderFactory.stop()");
        // stop server
        this.providerConfig.getServer().newInstance().stop();
    }

    // ---------------------- server invoke ----------------------

    /**
     * make service key
     *
     * @param iface
     * @param version
     * @return
     */
    public static String makeServiceKey(String iface, String version) {
        String serviceKey = iface;
        if (version != null && version.trim().length() > 0) {
            serviceKey += "#".concat(version);
        }
        return serviceKey;
    }

    /**
     * add service
     *
     * @param iface
     * @param version
     * @param serviceBean
     */
    public void addService(String iface, String version, Object serviceBean) {
        String serviceKey = makeServiceKey(iface, version);
        serviceData.put(serviceKey, serviceBean);

        logger.info(">>>>>>>>>>> xxl-rpc, provider factory add service success. serviceKey = {}, serviceBean = {}",
            serviceKey, serviceBean.getClass());
    }

    /**
     * invoke service
     *
     * @param xxlRpcRequest
     * @return
     */
    public XxlRpcResponse invokeService(XxlRpcRequest xxlRpcRequest) {
        logger.debug("XxlRpcProviderFactory.invokeService()");
        // make response
        XxlRpcResponse xxlRpcResponse = new XxlRpcResponse();
        xxlRpcResponse.setRequestId(xxlRpcRequest.getRequestId());

        // 获取要执行的接口
        String serviceKey = makeServiceKey(xxlRpcRequest.getClassName(), xxlRpcRequest.getVersion());
        Object serviceBean = serviceData.get(serviceKey);

        // 校验执行接口是否存在（是否注册）
        if (serviceBean == null) {
            xxlRpcResponse.setErrorMsg("The serviceKey[" + serviceKey + "] not found.");
            return xxlRpcResponse;
        }
        // 校验是否请求超时
        if (System.currentTimeMillis() - xxlRpcRequest.getCreateMillisTime() > 3 * 60 * 1000) {
            xxlRpcResponse.setErrorMsg("The timestamp difference between admin and executor exceeds the limit.");
            return xxlRpcResponse;
        }
        // 校验AccessToken
        if (providerConfig.getAccessToken() != null && providerConfig.getAccessToken().trim().length() > 0
            && !providerConfig.getAccessToken().trim().equals(xxlRpcRequest.getAccessToken())) {
            xxlRpcResponse.setErrorMsg("The access token[" + xxlRpcRequest.getAccessToken() + "] is wrong.");
            return xxlRpcResponse;
        }

        try {
            // invoke
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = xxlRpcRequest.getMethodName();
            Class<?>[] parameterTypes = xxlRpcRequest.getParameterTypes();
            Object[] parameters = xxlRpcRequest.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(serviceBean, parameters);
            xxlRpcResponse.setResult(result);
        } catch (Throwable t) {
            // catch error
            logger.error("xxl-rpc provider invokeService error.", t);
            xxlRpcResponse.setErrorMsg(ThrowableUtil.toString(t));
        }
        return xxlRpcResponse;
    }

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    public Map<String, Object> getServiceData() {
        return serviceData;
    }

}
