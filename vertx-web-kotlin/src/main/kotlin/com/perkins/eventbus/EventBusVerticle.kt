package com.perkins.eventbus

import com.perkins.Runner
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import java.text.DateFormat
import java.time.Instant
import java.util.*



fun main(args: Array<String>) {
    Runner.runExample(EventBusVerticle::class.java)
}


class EventBusVerticle : AbstractVerticle() {
    override fun start() {
        val router = Router.router(vertx)
        val opts = BridgeOptions()
                .addOutboundPermitted(PermittedOptions().setAddress("feed"))
                .addInboundPermitted(PermittedOptions().setAddress("feed"))
                .addOutboundPermitted(PermittedOptions().setAddress("callback"))
                .addInboundPermitted(PermittedOptions().setAddress("callback"))

        val ebHandler = SockJSHandler.create(vertx).bridge(opts)
        router.route("/eventbus/*").handler(ebHandler)

        router.route().handler(StaticHandler.create())

        vertx.createHttpServer().requestHandler { router.accept(it) }.listen(8080)

        val eb = vertx.eventBus()


        eb.consumer<String>("callback"){
            msg ->
            println(msg.body())
        }

        vertx.setPeriodic(1000L) { t ->
            /*val timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()))
            eb.send("feed", JsonObject().put("now", timestamp))*/
        }
    }

}