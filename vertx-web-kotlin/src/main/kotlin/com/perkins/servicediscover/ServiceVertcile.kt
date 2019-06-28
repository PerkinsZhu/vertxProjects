package com.perkins.servicediscover

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.servicediscovery.types.EventBusService
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import org.slf4j.LoggerFactory


class ServiceVertcile : AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val options = ServiceDiscoveryOptions()
                .setAnnounceAddress("service-announce")
                .setName("my-name")

        val discovery = ServiceDiscovery.create(vertx, options)
//        val record = HttpEndpoint.createRecord("some-rest-api", "localhost", 8080, "/api", JsonObject().put("some-metadata", "some value"))

        val record = EventBusService.createRecord(
                "some-eventbus-service", // The service name
                "address", // the service address,
                "com.perkins.servicediscover.services.MyService", // the service interface as string
                JsonObject().put("some-metadata", "some value")
        )
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