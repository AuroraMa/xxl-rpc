package com.xxl.rpc.core.remoting.provider;

import java.util.Map;

import com.xxl.rpc.core.registry.Register;
import com.xxl.rpc.core.remoting.net.Server;
import com.xxl.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.xxl.rpc.core.serialize.Serializer;
import com.xxl.rpc.core.serialize.impl.HessianSerializer;

/**
 * @author yangma
 * @version V1.0.0
 * @Description: TODO
 * @date 2020/7/24
 */

public class ProviderConfig {

    private Class<? extends Server> server = NettyServer.class;
    private Class<? extends Serializer> serializer = HessianSerializer.class;

    private Class<? extends Register> serviceRegistry = null;
    private Map<String, String> serviceRegistryParam = null;
    /**
     * default use registryAddress to registry , if registryAddress is null use ip:port
     */
    private String registryAddress;

    private int corePoolSize = 60;
    private int maxPoolSize = 300;

    /**
     * server ip, for registry
     */
    private String ip = null;

    /**
     * server default port
     */
    private int port = 7080;

    private String accessToken = null;

    public Class<? extends Server> getServer() {
        return server;
    }

    public void setServer(Class<? extends Server> server) {
        this.server = server;
    }

    public Class<? extends Serializer> getSerializer() {
        return serializer;
    }

    public void setSerializer(Class<? extends Serializer> serializer) {
        this.serializer = serializer;
    }

    public Class<? extends Register> getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(Class<? extends Register> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public Map<String, String> getServiceRegistryParam() {
        return serviceRegistryParam;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProviderConfig{");
        sb.append("server=").append(server);
        sb.append(", serializer=").append(serializer);
        sb.append(", serviceRegistry=").append(serviceRegistry);
        sb.append(", serviceRegistryParam=").append(serviceRegistryParam);
        sb.append(", corePoolSize=").append(corePoolSize);
        sb.append(", maxPoolSize=").append(maxPoolSize);
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", port=").append(port);
        sb.append(", registryAddress='").append(registryAddress).append('\'');
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
