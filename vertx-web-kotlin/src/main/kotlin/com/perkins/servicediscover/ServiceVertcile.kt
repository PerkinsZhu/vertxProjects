package com.perkins.servicediscover

import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.AbstractVerticle
import io.vertx.rxjava.core.http.HttpClient
import io.vertx.rxjava.servicediscovery.ServiceDiscovery
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint
import io.vertx.servicediscovery.Record
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import org.slf4j.LoggerFactory


class ServiceVertcile : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val options = ServiceDiscoveryOptions()
                .setAnnounceAddress("service-announce")
                .setName("my-name")

        val discovery = ServiceDiscovery.create(vertx, options)
        val record = HttpEndpoint.createRecord("some-rest-api", "localhost", 8080, "/api", JsonObject().put("some-metadata", "some value"))

        discovery.publish(record) { ar ->
            if (ar.succeeded()) {
                val publishedRecord = ar.result()
                logger.info("服务发布成功-->" + publishedRecord.name)
            } else {
                logger.error("服务发布失败", ar.cause())
            }
        }
//        discovery.close()
    }

}