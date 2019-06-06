package com.perkins.eventbus

import io.vertx.core.Vertx
import org.junit.Test
import io.vertx.core.VertxOptions




class App {

    @Test
    fun testClusterEventBus() {
        val options = VertxOptions()
        Vertx.clusteredVertx(options) { res ->
            if (res.succeeded()) {
                val vertx = res.result()
                val eventBus = vertx.eventBus()
                println("We now have a clustered event bus: $eventBus")
            } else {
                System.out.println("Failed: " + res.cause())
            }
        }
    }
}