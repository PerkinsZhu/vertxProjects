package com.perkins.servicediscover.services

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen

@ProxyGen
@VertxGen
interface MyService {
    fun show(str: String)
}