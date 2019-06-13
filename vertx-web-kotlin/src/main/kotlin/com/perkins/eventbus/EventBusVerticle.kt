package com.perkins.eventbus

import com.perkins.Runner
import com.perkins.eventbus.handles.Handle
import com.perkins.eventbus.handles.HandleWithBuffer
import com.perkins.handlers.BaseHandle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.eventbus.impl.codecs.BufferMessageCodec
import io.vertx.core.eventbus.impl.codecs.ByteArrayMessageCodec
import io.vertx.core.file.OpenOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
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
import java.io.File
import java.sql.Blob
import java.text.DateFormat
import java.time.Instant
import java.util.*


fun main(args: Array<String>) {
    Runner.runExample(EventBusVerticle::class.java)
}


class EventBusVerticle : AbstractVerticle() {
    override fun start() {
        val handler = Handle(vertx)
        val handlerWithBuffer = HandleWithBuffer(vertx)
        val router = Router.router(vertx)
        val opts = BridgeOptions()
                .addOutboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))
                .addInboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))


        val uploadFile = Handler<RoutingContext> {
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
                .socketHandler {
                    println(it.webSession().data())
                    it.webUser()
                }
//                .handle()
        router.route("/eventbus/*").handler(ebHandler)
//        router.route("/eventbus/*").handler(uploadFile)

        router.route().handler(StaticHandler.create())

        val options = HttpServerOptions()
        // 设置每次http请求长度最大值，该设置会影响整个服务
//        options.maxWebsocketFrameSize = 1024 * 1024 * 5
//        options.maxWebsocketMessageSize = 1024 *1024 * 10

        vertx.createHttpServer(options).requestHandler { router.accept(it) }.listen(8081)

        val eb = vertx.eventBus()
        val fs = vertx.fileSystem()
        eb.consumer<String>("callback") { msg ->
            println("body---" + msg.body())
        }
        eb.consumer<JsonObject>("fileData", handler.fileData)
        eb.consumer<JsonObject>("startMulitUpload", handler.startMulitUpload)
        eb.consumer<JsonObject>("mulitUpload", handler.mulitUpload)
        eb.consumer<String>("mulitUploadWithBase64", handler.mulitUploadWithBase64)
        eb.consumer<JsonObject>("startMulitUploadToS3", handler.startMulitUploadToS3)
        eb.consumer<String>("mulitUploadWithBase64AndSendToS3", handler.mulitUploadWithBase64AndSendToS3)
        eb.consumer<String>("mulitUploadWithByteStringAndSendToS3", handler.mulitUploadWithByteStringAndSendToS3)
        // 通过缓存存储文件数据
        eb.consumer<JsonObject>("startSendWithBuffer", handlerWithBuffer.startMulitUploadToS3)
        eb.consumer<String>("uploadSendWithBuffer", handlerWithBuffer.mulitUploadWithByteStringAndSendToS3)

    }

}