package com.perkins.vertxs

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.logging.LoggerFactory
import io.vertx.rx.java.ObservableFuture
import io.vertx.rx.java.RxHelper


class RXVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val observable: ObservableFuture<HttpServer> = RxHelper.observableFuture<HttpServer>()
        observable.subscribe(
                { server ->
                    server.requestHandler { req ->
                        run {
                            req.response().end("==hello=")
                        }
                    }
                },
                { failure ->
                    logger.error(failure)
                }
        )
        val handle = observable.toHandler()
        logger.info(handle)

        /**
         *  todo 这里报错
         * java.lang.IllegalStateException: Set request or websocket handler first
        at io.vertx.core.http.impl.HttpServerImpl.listen(HttpServerImpl.java:221)
        at io.vertx.core.http.impl.HttpServerImpl.listen(HttpServerImpl.java:201)
         */
        vertx.createHttpServer(HttpServerOptions().setPort(1234).setHost("localhost")).listen(handle)

    }

}