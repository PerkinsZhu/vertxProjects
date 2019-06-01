package com.perkins

import com.perkins.handlers.BaseHandle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router

class MainVerticle : AbstractVerticle() {
    val router = createRouter()
    override fun start(startFuture: Future<Void>) {
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(config().getInteger("http.port", 8080)) { result ->
                    if (result.succeeded()) {
                        startFuture.complete()
                    } else {
                        startFuture.fail(result.cause())
                    }
                }
    }


    private fun createRouter() = Router.router(vertx).apply {
        get("/").handler(BaseHandle.indexHandle)
    }

}