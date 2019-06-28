package com.perkins.servicediscover.proxy

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

@ProxyGen
interface SomeDatabaseService {
    fun save(collection: String, document: JsonObject, resultHandler: Handler<AsyncResult<String>>)

}