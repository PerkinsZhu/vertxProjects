package com.perkins

import com.perkins.eventbus.EventBusVerticle
import com.perkins.vertxs.CommonVerticle
import com.perkins.vertxs.MongoVerticle
import com.perkins.vertxs.RXVerticle
import com.perkins.vertxs.RouteVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.DeploymentOptions




class MainVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start(startFuture: Future<Void>) {
        val commonVertx = CommonVerticle()
        val routeVerticle = RouteVerticle()
        val mongoVerticle = MongoVerticle()
        val rxVerticle = RXVerticle()
        val options = DeploymentOptions()
                .setWorker(true) // 指定 verticle按照 work verticle 在Worker Pool  中执行
                .setInstances(1) //设置运行 verticle的数量，适用于多核处理器
        //TODO 当同时部署两个verticale的时候，第二个无法找到route，但是上传文件却可以成功的。
//        vertx.deployVerticle(commonVertx) { completionHandler(it) }

//        ========================eventbus =========
        val eventBusVerticle = EventBusVerticle()

        vertx.deployVerticle(commonVertx, options) { completionHandler(it, startFuture) }

    }

    fun completionHandler(it: AsyncResult<String>,startFuture:Future<Void>) {
        if (it.succeeded()) {
            logger.info("Deployment id is: " + it.result());
            startFuture.complete()
        } else {
            logger.error("Deployment failed!\r\t" ,it.cause());
            startFuture.fail(it.cause())
        }
    }

}