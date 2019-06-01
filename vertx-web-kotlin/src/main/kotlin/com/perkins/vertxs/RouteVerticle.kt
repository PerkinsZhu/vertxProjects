package com.perkins.vertxs

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class RouteVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start(startFuture: Future<Void>?) {
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.get("/hello").handler {
            it.response().end("hello")
        }

        router.post("/form").handler { ctx ->
            ctx.response().putHeader("Content-Type", "text/plain")

            ctx.response().isChunked = true

            for (f in ctx.fileUploads()) {
                println("f")
                ctx.response().write("Filename: " + f.fileName())
                ctx.response().write("\n")
                ctx.response().write("Size: " + f.size())
            }

            ctx.response().end()
        }



        router.get("/download").handler {
            logger.info("接受到下载文件请求")
            //从本地磁盘读取文件返回
            it.response().sendFile("uploads/0644d274-c987-499b-8286-8d72105b15e1")
        }


        router.get("/downloadFromUrl").handler {
            logger.info("接受到下载文件请求")
            //从本地磁盘读取文件返回
            it.response().sendFile("uploads/0644d274-c987-499b-8286-8d72105b15e1")
        }

        server.requestHandler(router::accept).listen(8080)
    }
}