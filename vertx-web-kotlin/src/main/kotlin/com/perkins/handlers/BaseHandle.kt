package com.perkins.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

object BaseHandle {
    val indexHandle = Handler<RoutingContext> {
        it.response().end("welcome")
    }
}