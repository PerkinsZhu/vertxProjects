package com.perkins.servicediscover.proxy

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

class SomeDatabaseServiceImpl : SomeDatabaseService {
    override fun save(collection: String, document: JsonObject, resultHandler: Handler<AsyncResult<String>>) {
        println("远程微服务接受到消息：$collection")
        resultHandler.handle(Future.succeededFuture("返回成功结果"))
    }

}