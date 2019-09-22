package com.perkins.lifecycle.ww;

import rx.Single;

public class TestApp {
    public static void main(String[] args) throws InterruptedException {
        Single.just(new BBB("aa")).map(x -> Single.just(x + "")).subscribe(s -> {
            System.out.printf("end");
        });

        Thread.sleep(1000);
    }
}
