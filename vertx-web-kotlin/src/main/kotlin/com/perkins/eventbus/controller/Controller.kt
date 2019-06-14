package com.perkins.eventbus.controller

import io.vertx.rxjava.ext.web.Router

abstract class Controller(val handlers: Router.() -> Unit) {
    abstract val router: Router
    fun create(): Router {
        return router.apply {
            handlers()
        }
    }
}