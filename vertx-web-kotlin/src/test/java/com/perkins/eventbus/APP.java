package com.perkins.eventbus;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import org.junit.Test;

public class APP {

    // 基础用法测试
    @Test
    public void testEventBuse() throws InterruptedException {

        Vertx vertx = Vertx.vertx();
        EventBus eb = vertx.eventBus();
        MessageConsumer<String> consumer = eb.consumer("news.uk.sport", message -> {
            System.out.println("01----I have received a message: " + message.body());
            message.reply("01---- replay ");
        });
        eb.consumer("news.uk.sport", message -> {
            System.out.println("02----I have received a message: " + message.body());
            message.fail(100, "我主动失败了");
        });
        eb.consumer("news.uk.sport", message -> {
            System.out.println("03----I have received a message: " + message.body());
        });

        consumer.completionHandler(res -> {
            if (res.succeeded()) {
                System.out.println("The handler registration has reached all nodes");
            } else {
                System.out.println("Registration failed!");
            }
        });

        while (true) {
            Thread.sleep(1000);
            DeliveryOptions options = new DeliveryOptions();
            options.addHeader("some-header", "some-value");
            options.setSendTimeout(2000);


            eb.publish("news.uk.sport", "----publish message---", options);
            eb.send("news.uk.sport", "---send message----", options, ar -> {
                if (ar.succeeded()) {
                    System.out.println("Received reply: " + ar.result().body());
                } else {
                    System.out.println("消息发送失败: " + ar.result());
                    System.out.println("消息发送失败: " + ar.cause());
                }
            });
        }
    }

}
