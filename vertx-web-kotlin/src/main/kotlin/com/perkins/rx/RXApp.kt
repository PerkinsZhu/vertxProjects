package com.perkins.rx

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Consumer

object RXApp {
    @JvmStatic
    fun main(args: Array<String>) {
//        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

        val runner: Consumer<Vertx> = Consumer {
            // 通过 Consumer 部署
            val mainVerticle = MainVerticle()
            it.deployVerticle(mainVerticle)
        }
        runner.accept(Vertx.vertx(VertxOptions()))
    }
}