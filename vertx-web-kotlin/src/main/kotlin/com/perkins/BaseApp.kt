package com.perkins

import com.perkins.mysql.JDBCAPP
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import org.slf4j.LoggerFactory

abstract class BaseApp {
    val logger = LoggerFactory.getLogger(this.javaClass)

    fun <T> handle(msg: String, action: (T) -> Unit): Handler<AsyncResult<T>> {
        return Handler<AsyncResult<T>> {
            if (it.succeeded()) {
                action(it.result())
            } else {
                JDBCAPP.logger.error(msg, it.cause())
            }
        }
    }
}

//第四次提交