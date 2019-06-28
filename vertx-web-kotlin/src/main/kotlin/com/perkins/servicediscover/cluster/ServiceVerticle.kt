package com.perkins.servicediscover.cluster

import com.perkins.servicediscover.cluster.service.UserService
import com.perkins.servicediscover.cluster.service.UserServiceImpl
import com.perkins.servicediscover.cluster.service.UserServiceObj
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.serviceproxy.ProxyHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ServiceVerticle : AbstractVerticle() {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {

        val eb = vertx.eventBus()
        eb.consumer<String>("server.message") {
            val body = it.body()
            println("service: $body")
            it.reply("receive -> $body")
        }


        val service = UserServiceImpl()
        ProxyHelper.registerService(UserService::class.java, vertx, service, UserServiceObj.ADDRESS)

        val userService = ProxyHelper.createProxy(UserService::class.java, vertx, UserServiceObj.ADDRESS)
        userService.getUser("小李", Handler {
            println("接受到回调结果:${it.result()}")
        })

    }

}