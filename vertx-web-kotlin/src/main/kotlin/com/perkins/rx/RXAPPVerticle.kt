package com.perkins.rx

import io.vertx.rxjava.core.AbstractVerticle
import org.slf4j.LoggerFactory

class RXAPPVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        super.start()

        val server = vertx.createHttpServer()
        server.listen(8084) {
            if (it.succeeded()) {
                logger.info("服务器启动成功....")
            }
        }

    }
}