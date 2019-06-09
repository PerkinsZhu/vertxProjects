package com.perkins.eventbus

import com.perkins.Runner
import com.perkins.handlers.BaseHandle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.eventbus.impl.codecs.BufferMessageCodec
import io.vertx.core.eventbus.impl.codecs.ByteArrayMessageCodec
import io.vertx.core.file.OpenOptions
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

        vertx.createHttpServer().requestHandler { router.accept(it) }.listen(8080)

        val eb = vertx.eventBus()
        val fs = vertx.fileSystem()
        eb.consumer<String>("callback") { msg ->
            println("body---" + msg.body())
        }
        eb.consumer<JsonObject>("fileData") { msg ->
            val body = msg.body()
            val string = body.getString("data")
            val fileName = body.getString("fileName")
            println("fileName$fileName")
            val byteArray = string.toByteArray(Charsets.ISO_8859_1)

            // 注意这里文件目录需要存在
           val filePath = "uploads/${System.currentTimeMillis().toString() + "-" + fileName}"
            println(filePath)
            println(System.getProperty("vertx.cwd"))

            fs.open(filePath, OpenOptions()) {
                fs.writeFile(filePath, Buffer.buffer(byteArray)) { it ->
                    if (it.succeeded()) {
                        println("上传文件成功")
                    } else {
                        println("上传文件失败")
                        it.cause().printStackTrace()
                    }
                }
            }
            // 这里找不到路径
            /*fs.createFile(filePath) { it ->
                if (it.succeeded()) {
                    fs.writeFile(filePath, Buffer.buffer(byteArray)) {
                        if (it.succeeded()) {
                            println("上传文件成功")
                        } else {
                            println("上传文件失败")
                            it.cause().printStackTrace()
                        }
                    }
                } else {
                    println("文件创建失败")
                    it.cause().printStackTrace()
                }
            }*/
        }

    }

}