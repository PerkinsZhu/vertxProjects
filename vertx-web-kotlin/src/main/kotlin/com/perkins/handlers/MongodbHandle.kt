package com.perkins.handlers

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

object MongodbHandle {
    val logger = LoggerFactory.getLogger(this.javaClass)

    fun <T> baseHandle(msg: String) = run {
        Handler<AsyncResult<T>> {
            if (it.succeeded()) {
                logger.debug("执行[$msg]成功")
            } else {
                logger.error("执行[$msg]失败")
            }
        }
    }
}