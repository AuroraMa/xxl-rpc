package com.xxl.rpc.core.remoting.invoker.route.impl;

import java.util.Random;
import java.util.TreeSet;

import com.xxl.rpc.core.remoting.invoker.route.XxlRpcLoadBalance;

/**
 * random
 *
 * @author xuxueli 2018-12-04
 */
public class XxlRpcLoadBalanceRandomStrategy extends XxlRpcLoadBalance {

    private Random random = new Random();

    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        // arr
        String[] addressArr = addressSet.toArray(new String[addressSet.size()]);

        // random
        String finalAddress = addressArr[random.nextInt(addressSet.size())];
        return finalAddress;
    }

}
