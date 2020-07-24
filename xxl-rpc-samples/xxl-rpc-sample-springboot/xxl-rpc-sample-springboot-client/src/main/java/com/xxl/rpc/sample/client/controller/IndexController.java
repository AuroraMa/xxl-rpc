package com.xxl.rpc.sample.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxl.rpc.core.remoting.invoker.annotation.XxlRpcReference;
import com.xxl.rpc.sample.api.DemoService;
import com.xxl.rpc.sample.api.dto.UserDTO;

@Controller
public class IndexController {

    @XxlRpcReference
    private DemoService demoService;

    @RequestMapping("")
    @ResponseBody
    public UserDTO http(String name) {

        try {
            return demoService.sayHi(name);
        } catch (Exception e) {
            e.printStackTrace();
            return new UserDTO(null, e.getMessage());
        }
    }

}
