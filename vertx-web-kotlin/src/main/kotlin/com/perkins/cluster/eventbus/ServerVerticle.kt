package com.perkins.cluster.eventbus

import io.vertx.core.AbstractVerticle


class ServerVerticle : AbstractVerticle() {

    override fun start() {
        val eb = vertx.eventBus()
        eb.consumer<String>("test.message") {
            println("server --> ${it.body()}")
            it.reply(" msg ok ---> ${it.body()}")
        }

        eb.send<String>("test.message", " init test") {
            if (it.succeeded()) {
                println(it.result())
            }
        }
    }
}