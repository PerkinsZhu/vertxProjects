package com.perkins.socketjs

import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.bridge.BridgeEventType
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.BridgeEvent
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.kotlin.core.json.JsonObject

class BridgeEventBusVerticle : AbstractVerticle() {
    override fun start() {
        val router = Router.router(vertx)
        val opts = BridgeOptions()
                .addOutboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))
                .addInboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))
                .setPingTimeout(10000); // 超过这个时间没有客户端ping 该socket，则说明该socket已经空闲。空闲之后该socket会被关闭掉，之后客户端就需要重新连接，无法再继续发送消息
        val sockJSHandler = SockJSHandler.create(vertx);
        sockJSHandler.bridge(opts, Handler {
            println(it.type())
            println(it.rawMessage)
            // 这里可以根据各种消息类型做一些事件处理
//             这里类似于socket的生命周期一致
            if (it.type() == BridgeEventType.RECEIVE) {
                it.socket().write(Buffer.buffer(JsonObject().put("errormessage","werwerwerwe").encode()))
                it.complete(false)
            } else if (it.type() == BridgeEventType.PUBLISH || it.type() == BridgeEventType.SEND) {
                val rowMessage = it.rawMessage
                val headers = JsonObject().put("aaa", "aaa")
                rowMessage.put("headers", headers)
                it.rawMessage = rowMessage
                it.complete(true)
            } else {
                it.complete(true)
            }
        })
        router.route("/eventbus/*").handler(sockJSHandler);

        router.route().handler(StaticHandler.create())
        val options = HttpServerOptions()
        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(9080)
    }

}