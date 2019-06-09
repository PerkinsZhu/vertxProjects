package com.perkins.eventbus

import com.perkins.Runner
import com.perkins.handlers.BaseHandle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.eventbus.impl.codecs.BufferMessageCodec
import io.vertx.core.eventbus.impl.codecs.ByteArrayMessageCodec
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.impl.WebSocketImplBase
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import java.sql.Blob
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
                .addOutboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))
                .addInboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))

        val uploadFile = Handler<RoutingContext>{
            val request = it.request()
            request.params().map { BaseHandle.logger.info(it) }
            request.headers().map { BaseHandle.logger.info(it) }
            BaseHandle.logger.info(request.absoluteURI())

            it.response().end("success")
        }
        uploadFile

//        val da = WebSocketImplBase()
        val ebHandler = SockJSHandler.create(vertx)
                .bridge(opts)
                .socketHandler{
                    println(it.webSession().data())
                    it.webUser()
                }
//                .handle()
        router.route("/eventbus/*").handler(ebHandler)
//        router.route("/eventbus/*").handler(uploadFile)

        router.route().handler(StaticHandler.create())

        vertx.createHttpServer().requestHandler { router.accept(it) }.listen(8080)

        val eb = vertx.eventBus()


       val codec = ByteArrayMessageCodec()
       println(codec.name())

        eb.consumer<String>("callback"){
            msg ->
            println("body---"+msg.body())
        }
        val consumer = eb.consumer<JsonObject>("fileData"){
            msg ->
            println("fileData---"+msg.body())
        }

        consumer.bodyStream().handler{
            data ->
            println(data)
//            val contetn = data.getBinary("data")
            val string = data.getString("data")
//            println(contetn)
            println(String(string.toByteArray(Charsets.UTF_16),Charsets.UTF_8))
            println(String(string.toByteArray(Charsets.UTF_16BE),Charsets.UTF_8))
            println(String(string.toByteArray(Charsets.UTF_16LE),Charsets.UTF_8))
            print(data.javaClass)
        }

        /*vertx.setPeriodic(1000L) { t ->
            val timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()))
            eb.send("feed", JsonObject().put("now", timestamp))
        }*/
    }

}