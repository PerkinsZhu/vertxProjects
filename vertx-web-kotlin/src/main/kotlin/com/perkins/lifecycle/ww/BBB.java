package com.perkins.lifecycle.ww;

public class BBB {
    private String name;

    public BBB(String name) {
        this.name = name;
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println(name + "-执行销毁销毁");
        super.finalize();
    }
}
