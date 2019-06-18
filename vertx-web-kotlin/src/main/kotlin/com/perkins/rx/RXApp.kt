package com.perkins.rx

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import java.util.function.Consumer

object RXApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val runner: Consumer<Vertx> = Consumer {
            // 通过 Consumer 部署
            val mainVerticle = MainVerticle()
            it.deployVerticle(mainVerticle)
        }
        runner.accept(Vertx.vertx(VertxOptions()))
    }
}