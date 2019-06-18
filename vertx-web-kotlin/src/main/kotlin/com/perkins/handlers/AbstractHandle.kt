package com.perkins.handlers

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient

open class AbstractHandle {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val vertx = Vertx.vertx()

    protected val client: MongoClient

    init {
        //TODO 数据库连接池的创建不应防在init方法中
        val config = JsonObject()
        config.put("connection_string", "mongodb://localhost:27017/test")
        client = MongoClient.createShared(vertx, config, "MyPoolName")
    }

    protected fun getResult(data: JsonObject, code: Int, msg: String? = null): JsonObject {
        val result = JsonObject()
        result.put("code", code)
        if (msg.isNullOrBlank()) {
            val message = when (code) {
                0 -> "处理成功"
                else -> "处理失败"
            }
            result.put("msg", message)
        } else {
            result.put("msg", msg)
        }
        result.put("data", data)
        return result
    }


}