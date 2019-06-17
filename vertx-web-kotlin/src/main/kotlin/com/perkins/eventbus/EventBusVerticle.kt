package com.perkins.eventbus

import com.perkins.Runner
import com.perkins.eventbus.handles.BaseHandle
import com.perkins.eventbus.handles.Handle
import com.perkins.eventbus.handles.HandleWithBuffer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.rxjava.core.RxHelper
import org.slf4j.LoggerFactory


fun main(args: Array<String>) {
    Runner.runExample(EventBusVerticle::class.java)
}


class EventBusVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val handler = Handle(vertx)
        val handlerWithBuffer = HandleWithBuffer(vertx)
        val router = Router.router(vertx)
        val opts = BridgeOptions()
                .addOutboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))
                .addInboundPermitted(io.vertx.ext.bridge.PermittedOptions().setAddressRegex(".*"))


        val uploadFile = Handler<RoutingContext> {
            val request = it.request()
            request.params().map { map ->
                logger.info(map.key)
            }
            request.headers().map { map ->
                logger.info(map.key)
            }
            logger.info(request.absoluteURI())

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

        val baseHandler = BaseHandle(vertx)
        router.get("/getFile").handler(baseHandler.getFile)


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


        // rx  部署 verticle
        /*   val baseVerticle = BaseVerticle()
           val rxVertx = io.vertx.rxjava.core.Vertx(vertx);
           RxHelper.deployVerticle(rxVertx, baseVerticle)*/
    }

}