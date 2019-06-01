package com.perkins

import com.perkins.vertxs.CommonVerticle
import com.perkins.vertxs.RouteVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.logging.LoggerFactory


class MainVerticle : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val commonVertx = CommonVerticle()
        val routeVerticle = RouteVerticle()
        //TODO 当同时部署两个verticale的时候，第二个无法找到route，但是上传文件却可以成功的。
//        vertx.deployVerticle(commonVertx) { completionHandler(it) }
        vertx.deployVerticle(routeVerticle) { completionHandler(it) }

    }

    fun completionHandler(it: AsyncResult<String>) {
        if (it.succeeded()) {
            logger.info("Deployment id is: " + it.result());
        } else {
            logger.error("Deployment failed!");
        }
    }
}