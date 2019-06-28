package com.perkins.servicediscover.cluster.service

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler

class UserServiceImpl : UserService {
    override fun getUser(name: String, handler: Handler<AsyncResult<String>>) {
        println("接受到查询用户:$name 的请求")
        handler.handle(Future.succeededFuture(" i am 小红"))
    }

}