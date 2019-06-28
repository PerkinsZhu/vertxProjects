package com.perkins.cluster.eventbus

import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Service {
    // 集群启动 当做server端
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(ServerVerticle::class.java!!.name) {
            if (it.succeeded()) {
                logger.info("service deploy success")
            } else {
                logger.error("service deploy failed", it.cause())
            }
        }

    }
}