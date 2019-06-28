package com.perkins.cluster.eventbus

import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Client {
    // 集群启动 当做client端
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(ClientVerticle::class.java!!.name) {
            if (it.succeeded()) {
                logger.info("ClientVerticle deploy success")
            } else {
                logger.error("ClientVerticle deploy failed", it.cause())
            }
        }

    }
}