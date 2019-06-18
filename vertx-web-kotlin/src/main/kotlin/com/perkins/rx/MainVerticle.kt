package com.perkins.rx

import io.vertx.core.http.HttpServerOptions
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.core.RxHelper
import io.vertx.rxjava.ext.web.Router
import org.slf4j.LoggerFactory

class MainVerticle : AbstractVerticle() {

    override fun start() {
        super.start()
        val router = Router.router(vertx)
        router.route().handler {
            val logger = LoggerFactory.getLogger(this.javaClass)
            logger.info("i am logger")
            it.response().end("i am RXJava")
        }

        // 通过 RXHelper部署Verticle
        val rxVerticle = RXAPPVerticle()
        RxHelper.deployVerticle(vertx, rxVerticle).subscribe {
            print(it)
        }

        val options = HttpServerOptions()
        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(8081)
    }
}