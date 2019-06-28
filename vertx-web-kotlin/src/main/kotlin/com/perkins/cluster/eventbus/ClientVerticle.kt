package com.perkins.cluster.eventbus

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class ClientVerticle : AbstractVerticle() {

    override fun start() {
        val router: Router = createRouter()
        router.route().handler(BodyHandler.create())
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(config().getInteger("http.port", 8081)) { result ->
                    if (result.succeeded()) {
                        println("ClientVerticle SUCCESS")
                    } else {
                        println("ClientVerticle FAILED")
                    }
                }

    }


    private fun createRouter() = Router.router(vertx).apply {
        route("/rpc").handler { rc ->

            val eb = vertx.eventBus()
            eb.send<String>("test.message", "hello i am server") {
                if (it.succeeded()) {
                    val body = it.result()
                    println("成功---$body")
                    rc.response().end("RPC --> $body")
                } else {
                    it.cause().printStackTrace()
                    println("失败")
                    rc.response().end("RPC --> 失败")
                }
            }

        }


    }
}