package com.perkins.cluster

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import java.util.function.Consumer
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import io.vertx.core.spi.cluster.ClusterManager


object ClusterApp {

    @JvmStatic
    fun main(args: Array<String>) {
        // 不同的verticle必须指定不同的端口，不然系统只会部署第一个verticle，其他的verticle都不会启动
        val clusterVerticle = ClusterVerticle(8085)
        val taskVerticle = TaskVerticle(8086)
        // 用不同的四个端口分别启动两个jvm，然后请求 ClusterVerticle的 /name 接口可以触发两个jvm之间发送 eventBus 消息

        val runner: Consumer<Vertx> = Consumer {
            //这里是可以部署多个verticle
            it.deployVerticle(clusterVerticle)
            it.deployVerticle(taskVerticle)
        }

        val mgr = HazelcastClusterManager()//创建ClusterManger对象
        val options = VertxOptions().setClusterManager(mgr)//设置到Vertx启动参数中

        Vertx.clusteredVertx(options) { res ->
            if (res.succeeded()) {
                val vertx = res.result()
                runner.accept(vertx)
            } else {
                res.cause().printStackTrace()
            }
        }
    }
}