package com.perkins.socketjs

import com.perkins.Runner

fun main() {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
//    Runner.runExample(SocketJsVerticle::class.java)
    Runner.runExample(BridgeEventBusVerticle::class.java)
}