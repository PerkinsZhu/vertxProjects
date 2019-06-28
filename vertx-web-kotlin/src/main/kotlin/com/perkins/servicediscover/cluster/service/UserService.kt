package com.perkins.servicediscover.cluster.service

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler

@ProxyGen
interface UserService {
    fun getUser(name: String, handler: Handler<AsyncResult<String>>)
}
object UserServiceObj{
    val ADDRESS = "cluster-user-service"
}