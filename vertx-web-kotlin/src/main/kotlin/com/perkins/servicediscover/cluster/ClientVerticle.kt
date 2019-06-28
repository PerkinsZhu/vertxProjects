package com.perkins.servicediscover.cluster

import com.perkins.servicediscover.cluster.service.UserService
import com.perkins.servicediscover.cluster.service.UserServiceObj
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.serviceproxy.ProxyHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class ClientVerticle : AbstractVerticle() {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val router: Router = createRouter()
        router.route().handler(BodyHandler.create())
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(config().getInteger("http.port", 8082)) { result ->
                    if (result.succeeded()) {
                        println("------success--")
                    } else {
                        println("------failure--")
                    }
                }


    }

    private fun createRouter() = Router.router(vertx).apply {
        val eb = vertx.eventBus()
        route("/get").handler {

            eb.send<String>("server.message", "i am client ${LocalDateTime.now()}") {
                if (it.succeeded()) {
                    val res = it.result().body()
                    println("消息发送成功---> $res")
                } else {
                    println("消息发送失败")
                }
            }

            it.response().end("ClientService--clu")
        }


        route("/rpc").handler { rc ->
            val userService = ProxyHelper.createProxy(UserService::class.java, vertx, UserServiceObj.ADDRESS)
            userService.getUser("RPC client ", Handler {
                println("接受到回调结果:${it.result()}")
                rc.response().end("RCE 回应 --->${it.result()}")
            })

        }


    }
}