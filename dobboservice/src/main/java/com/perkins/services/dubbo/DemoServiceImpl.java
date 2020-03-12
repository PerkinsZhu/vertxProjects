package com.perkins.services.dubbo;


public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        System.out.println("init : " + name);
        return "hello " + name;
    }

}