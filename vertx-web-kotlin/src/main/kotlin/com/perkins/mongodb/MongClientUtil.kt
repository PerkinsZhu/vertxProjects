package com.perkins.mongodb

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

object MongClientUtil {
    val vertx = Vertx.vertx()
    val config = JsonObject().put("connection_string", "mongodb://localhost:27017/test") //该配置项会忽略其他所有的配置项
    val client = MongoClient.createShared(vertx, config, "MyPoolName")
}