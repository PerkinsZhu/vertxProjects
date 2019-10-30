package com.perkins;

public class BaseTest {
    public void testArgs(String ... args){
        System.out.println(args.getClass());
        System.out.println(args.length);
    }
}
