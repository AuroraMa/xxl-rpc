package com.xxl.rpc.sample.server;

import java.util.concurrent.TimeUnit;

import com.xxl.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.xxl.rpc.core.remoting.provider.ProviderConfig;
import com.xxl.rpc.core.remoting.provider.XxlRpcProviderFactory;
import com.xxl.rpc.core.serialize.impl.HessianSerializer;
import com.xxl.rpc.sample.api.DemoService;
import com.xxl.rpc.sample.server.service.DemoServiceImpl;

/**
 * @author xuxueli 2018-10-21 20:48:40
 */
public class XxlRpcServerApplication {

    public static void main(String[] args) throws Exception {

        // init
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setServer(NettyServer.class);
        providerConfig.setSerializer(HessianSerializer.class);
        providerConfig.setCorePoolSize(-1);
        providerConfig.setMaxPoolSize(-1);
        providerConfig.setIp(null);
        providerConfig.setPort(7080);
        providerConfig.setAccessToken(null);
        providerConfig.setServiceRegistry(null);
        providerConfig.setServiceRegistryParam(null);

        XxlRpcProviderFactory providerFactory = new XxlRpcProviderFactory();
        providerFactory.setProviderConfig(providerConfig);

        // add services
        providerFactory.addService(DemoService.class.getName(), null, new DemoServiceImpl());

        // start
        providerFactory.start();

        while (!Thread.currentThread().isInterrupted()) {
            TimeUnit.HOURS.sleep(1);
        }

        // stop
        providerFactory.stop();

    }

}
