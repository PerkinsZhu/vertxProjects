package com.perkins.dubbo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( new String[] { "applicationProvider.xml" });
        context.start();

        System.out.println("输入任意按键退出 ~ ");
        System.in.read();
        context.close();
    }
}
