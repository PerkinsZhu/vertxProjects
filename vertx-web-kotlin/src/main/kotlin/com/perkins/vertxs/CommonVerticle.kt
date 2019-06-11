package com.perkins.vertxs

import com.perkins.handlers.BaseHandle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class CommonVerticle : AbstractVerticle() {
    override fun start(startFuture: Future<Void>) {
        val router: Router = createRouter()
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"))
        router.route().handler(BodyHandler.create())
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(config().getInteger("http.port", 8082)) { result ->
                    if (result.succeeded()) {
                        startFuture.complete()
                    } else {
                        startFuture.fail(result.cause())
                    }
                }
    }


    private fun createRouter() = Router.router(vertx).apply {
        //        route().handler(BodyHandler.create())
        get("/").handler(BaseHandle.indexHandle)
        post("/json").handler(BaseHandle.jsonHandle)
        post("/uploadFile").handler(BaseHandle.uploadFile)
        get("/redirect/:key").handler(BaseHandle.redirectHandle)
        get("/path1/:key/bucketName").handler(BaseHandle.path1)
        get("/path2/:key").handler(BaseHandle.path2)
    }

}