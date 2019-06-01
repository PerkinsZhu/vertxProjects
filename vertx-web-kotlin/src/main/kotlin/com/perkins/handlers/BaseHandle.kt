package com.perkins.handlers

import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

object BaseHandle : AbstractHandle() {
    val indexHandle = Handler<RoutingContext> {
        it.response().end("welcome")
    }
    val jsonHandle = Handler<RoutingContext> {
        val result = JsonObject().put("code", 0).put("msg", "请求成功").put("data", JsonArray())
        val response = it.response()
        response.putHeader("content-type", "application/json");
        response.end(result.encode())
    }
    val uploadFile = Handler<RoutingContext>{
        val request = it.request()
        request.params().map { logger.info(it) }
        request.headers().map { logger.info(it) }
        logger.info(request.absoluteURI())

        it.fileUploads().map { file ->
            println(file.name())
            println(file.fileName())
            println(file.uploadedFileName())
        }
        it.response().end("success")
    }
}