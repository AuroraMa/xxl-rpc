package com.xxl.rpc.sample.server.conf;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xxl.rpc.core.registry.impl.XxlRpcAdminRegister;
import com.xxl.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.xxl.rpc.core.remoting.provider.ProviderConfig;
import com.xxl.rpc.core.remoting.provider.impl.XxlRpcSpringProviderFactory;
import com.xxl.rpc.core.serialize.impl.HessianSerializer;

/**
 * xxl-rpc provider config
 *
 * @author xuxueli 2018-10-19
 */
@Configuration
public class XxlRpcProviderConfig {
    private Logger logger = LoggerFactory.getLogger(XxlRpcProviderConfig.class);

    @Value("${xxl-rpc.remoting.port}")
    private int port;

    @Value("${xxl-rpc.registry.xxlrpcadmin.address}")
    private String address;

    @Value("${xxl-rpc.registry.xxlrpcadmin.env}")
    private String env;

    @Bean
    public XxlRpcSpringProviderFactory xxlRpcSpringProviderFactory() {
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setServer(NettyServer.class);
        providerConfig.setSerializer(HessianSerializer.class);
        providerConfig.setCorePoolSize(-1);
        providerConfig.setMaxPoolSize(-1);
        providerConfig.setIp(null);
        providerConfig.setPort(port);
        providerConfig.setAccessToken(null);
        providerConfig.setServiceRegistry(XxlRpcAdminRegister.class);
        providerConfig.setServiceRegistryParam(new HashMap<String, String>() {
            {
                put(XxlRpcAdminRegister.ADMIN_ADDRESS, address);
                put(XxlRpcAdminRegister.ENV, env);
            }
        });
        XxlRpcSpringProviderFactory providerFactory = new XxlRpcSpringProviderFactory();
        providerFactory.setProviderConfig(providerConfig);

        logger.info(">>>>>>>>>>> xxl-rpc provider config init finish.");
        return providerFactory;
    }

}