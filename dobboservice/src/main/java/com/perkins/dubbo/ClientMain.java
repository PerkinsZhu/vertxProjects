package com.perkins.dubbo;

import com.perkins.services.dubbo.DemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientMain {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"applicationConsumer.xml"});
        context.start();
        DemoService service = (DemoService) context.getBean("demoService");
        System.out.println(service.sayHello("world"));
        context.close();
    }
}