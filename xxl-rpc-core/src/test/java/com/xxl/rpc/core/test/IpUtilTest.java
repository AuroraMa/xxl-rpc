package com.xxl.rpc.core.test;

import java.net.UnknownHostException;

import com.xxl.rpc.core.util.IpUtil;

/**
 * @author xuxueli 2018-10-23
 */
public class IpUtilTest {

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(IpUtil.getIp());
        System.out.println(IpUtil.getIpPort(8080));
    }

}
