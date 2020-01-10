package com.perkins.restful

import com.perkins.Runner

object MainApp {
    @JvmStatic
    fun main(args: Array<String>) {
        //替换掉vertx内容部的日志类
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
        Runner.runExample(BaseVerticle::class.java)
    }
}