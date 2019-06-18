package com.perkins.rx

import io.vertx.core.http.HttpServerOptions
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.core.RxHelper
import io.vertx.rxjava.ext.web.Router
import org.slf4j.LoggerFactory

class RXAPPVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        super.start()
        val router = Router.router(vertx)
        router.route().handler {
            it.response().end("i am deployVerticle rxJava")
        }

        val options = HttpServerOptions()
        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(8082){
            if (it.succeeded()) {
                logger.info("服务器启动成功....")
            }
        }
    }
}