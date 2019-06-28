package com.perkins.servicediscover

import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.Vertx
import io.vertx.rxjava.core.http.HttpClient
import io.vertx.rxjava.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import org.junit.After
import org.junit.Test
import org.slf4j.LoggerFactory
import rx.Single
import rx.schedulers.Schedulers


class MainApp {
    val logger = LoggerFactory.getLogger(this.javaClass)
    @After
    fun sleep() {
        Thread.sleep(100000)
    }

    @Test
    fun testService() {
        val vertx = Vertx.vertx()
        Single.just(vertx).map {
            it.deployVerticle(ServiceVertcile::class.java!!.name) {
                if (it.succeeded()) {
                    logger.info("service deploy success")
                } else {
                    logger.error("service deploy failed", it.cause())
                }
            }
        }.observeOn(Schedulers.newThread())
                .map {
                    Thread.sleep(1000)
                    consumerService(vertx)
                }
                .doAfterTerminate { }
                .subscribe()


    }

    private fun consumerService(vertx: Vertx?) {
        val options = ServiceDiscoveryOptions()
                .setAnnounceAddress("service-announce")
                .setName("my-name")
        val discovery = ServiceDiscovery.create(vertx, options)

        discovery.getRecord({ r -> true }, { ar ->
            if (ar.succeeded()) {
                if (ar.result() != null) {
                    val result = ar.result()
                    logger.info("record-->${result.name}")
                    val conf = JsonObject()
                    val reference = discovery.getReference(result)
//                    val reference2 = discovery.getReferenceWithConfiguration(result, conf)
                    val client = reference.getAs(HttpClient::class.java)
                    client.get("/api") { response ->
                        response.bodyHandler {
                            print(it.toString("UFT-8"))
                        }.endHandler {
                            logger.info("---end--")
                            ServiceDiscovery.releaseServiceObject(discovery, client)
                        }

                    }

                    reference.release();
                } else {
                    logger.info("the lookup succeeded, but no matching service")
                }
            } else {
                logger.info("lookup failed")
            }
        })
    }

}