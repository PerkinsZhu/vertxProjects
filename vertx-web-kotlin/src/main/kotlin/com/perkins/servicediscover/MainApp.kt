package com.perkins.servicediscover

import io.vertx.core.Vertx
import org.slf4j.LoggerFactory


class MainApp {

    companion object {
        val logger = LoggerFactory.getLogger(this.javaClass)
        @JvmStatic
        fun main(args: Array<String>) {
            val vertx = Vertx.vertx()
            vertx.deployVerticle(ServiceVertcile::class.java!!.name) {
                if (it.succeeded()) {
                    logger.info("service deploy success")
                } else {
                    logger.error("service deploy failed", it.cause())
                }
            }


        }
    }

/*
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
                    val service = reference.getAs(MyService::class.java)
//                    val service = reference.getAs(MyService::class.java)
                    service.show("123123")
//                    val reference2 = discovery.getReferenceWithConfiguration(result, conf)
*/
/*                    val client = reference.getAs(HttpClient::class.java)
                    client.get("/api") { response ->
                        response.bodyHandler {
                            print(it.toString("UFT-8"))
                        }.endHandler {
                            logger.info("---end--")
                            ServiceDiscovery.releaseServiceObject(discovery, client)
                        }

                    }*//*


                    reference.release();
                } else {
                    logger.info("the lookup succeeded, but no matching service")
                }
            } else {
                logger.error("lookup failed", ar.cause())
            }
        })
    }
*/


}