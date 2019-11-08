package com.perkins.mongodb

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

object MongClientUtil {
    val vertx = Vertx.vertx()
    val config = JsonObject().put("connection_string", "mongodb://localhost:27017/test") //该配置项会忽略其他所有的配置项
    val client = lazy { MongoClient.createShared(vertx, config, "MyPoolName") }


    fun getRxClient(): io.vertx.rxjava.ext.mongo.MongoClient {
        val vertx = io.vertx.rxjava.core.Vertx.vertx()
        val config = JsonObject().put("connection_string", "mongodb://1234:1234123@192.168.1.1:27017/work_order") //该配置项会忽略其他所有的配置项
        return io.vertx.rxjava.ext.mongo.MongoClient.createShared(vertx, config, "MyPoolName")
    }

    fun getClient():MongoClient {
        val vertx =Vertx.vertx()
//        waitQueue = waitQueueMultiple * maxPoolSize
//        如何未设置waitQueueMultiple，则 waitQueue = 500
        val config = JsonObject().put("connection_string", "mongodb://1234:1234123@192.168.1.1:27017/work_order?maxPoolSize=800&waitQueueMultiple=3") //该配置项会忽略其他所有的配置项
        return MongoClient.createShared(vertx, config, "MyPoolName")
    }

}