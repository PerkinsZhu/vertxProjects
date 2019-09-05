package com.perkins.socketjs

import com.perkins.eventbus.handles.Handle
import com.perkins.eventbus.handles.HandleWithBuffer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.BridgeEvent
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import org.slf4j.LoggerFactory
import io.vertx.groovy.ext.web.client.HttpResponse_GroovyExtension.body


class SocketJsVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val router = Router.router(vertx)
        val opts = BridgeOptions()
                .addOutboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))
                .addInboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))
                .setPingTimeout(5000);


        val ebHandler = SockJSHandler.create(vertx)
                .bridge(opts)
                .socketHandler {
                    println("socketData ----> " + it.webSession().data())
                    it.webUser()
                }


        val eb = vertx.eventBus()
        eb.consumer<String>("messageMissing") { message ->
            System.out.println("I have received a message: " + message.body())
        }
        eb.localConsumer<String>("messageMissing") { message ->
            System.out.println("I have received a localConsumer: " + message.body())

        }

//        router.route("/eventbus/*").handler(ebHandler)

        router.route("/eventbus/*").handler {
            SockJSHandler.create(vertx).bridge(opts, BridgeEventHandler()).handle(it)
        }


        router.route().handler(StaticHandler.create())


        val options = HttpServerOptions()
        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(9080)
    }
}


class BridgeEventHandler : Handler<BridgeEvent> {
    override fun handle(event: BridgeEvent?) {
        println("receive data==>")
        event?.complete()

//        event.complete()
    }

}