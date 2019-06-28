package com.perkins.servicediscover.cluster

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import java.util.function.Consumer


object ClusterService {

    @JvmStatic
    fun main(args: Array<String>) {
//        val verticle = ServiceVerticle()
        val verticle = ClientVerticle()

        val runner: Consumer<Vertx> = Consumer {
            it.deployVerticle(verticle)
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