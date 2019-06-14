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
    val uploadFile = Handler<RoutingContext> {
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
    val redirectHandle = Handler<RoutingContext> {
        val request = it.request()
        val key = request.getParam("key")
        val bucketName = request.getParam("bucketName")
        println(key)
        println(bucketName)
        println("params-->" + request.params())
        println("formAttributes--->" + request.formAttributes())
        if (key == "123") {
            it.reroute("/path1/$key/$bucketName")
        } else {
            it.reroute("/path2/$key")
        }
    }
    val path1 = Handler<RoutingContext> {
        println("-----path--1----")
        val request = it.request()
        val key = request.getParam("key")
        val bucketName = request.getParam("bucketName")
        println(key)
        println(bucketName)
        println("params-->" + request.params())
        println("formAttributes--->" + request.formAttributes())
        if (key == "123") {

        }
        it.response().end("path1")
    }
    val path2 = Handler<RoutingContext> {
        println("-----path--2----")
        val request = it.request()
        val key = request.getParam("key")
        println(key)
        println("params-->" + request.params())
        println("formAttributes--->" + request.formAttributes())
        if (key == "123") {

        }
        it.response().end("path2")
    }

    val sessionTest = Handler<RoutingContext> {
        val session = it.session()
        session.put("name", "jack")
    }

    val setSession = Handler<RoutingContext> {
        val session = it.session()
        session.put("userName", "jack")
        session.put("userAge", 10)
        it.response().end("set success")
    }
    val getSession = Handler<RoutingContext> {
        val session = it.session()
        println(session)
        it.response().end(JsonObject(session.data()).toString())
    }

}