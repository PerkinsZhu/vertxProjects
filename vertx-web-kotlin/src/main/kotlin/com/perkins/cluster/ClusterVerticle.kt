package com.perkins.cluster

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import org.slf4j.LoggerFactory


class ClusterVerticle constructor(val port: Int) : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        // 不同的Verticle中的vertx用的是同一个
        logger.info("ClusterVerticle-->" + vertx.hashCode())

        val router: Router = createRouter()
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"))
        router.route().handler(BodyHandler.create())
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(config().getInteger("http.port", port)) { result ->
                    if (result.succeeded()) {
                        println("------success--")
                    } else {
                        println("------failure--")
                    }
                }

    }

    private fun createRouter() = Router.router(vertx).apply {
        /* route().handler {
             it.response().end("ClusterVerticle hello")
         }*/

        route("/name").handler {
            it.response().end("ClusterVerticle")
        }
        route("/name/clu").handler {
            it.response().end("ClusterVerticle--clu")
        }


    }

}