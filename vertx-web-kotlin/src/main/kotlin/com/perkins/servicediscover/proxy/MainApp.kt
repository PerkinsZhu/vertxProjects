package com.perkins.servicediscover.proxy

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.serviceproxy.ProxyHelper

object MainApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        /*
               val eb = vertx.eventBus()
              eb.consumer<String>("database-service-address") {
                   print(it.body())
               }

               val message = JsonObject()
               message.put("collection", "mycollection").put("document", JsonObject().put("name", "tim"))
               val options = DeliveryOptions().addHeader("action", "save")
               eb.send("database-service-address", message, options)
       */

        val service = SomeDatabaseServiceImpl()
        val data = ProxyHelper.registerService(SomeDatabaseService::class.java, vertx, service, "database-service-address")


        val service2 = ProxyHelper.createProxy(SomeDatabaseService::class.java, vertx, "database-service-address")
        service2.save("q2354243", JsonObject(), Handler {
            println("接受到回调结果:${it.result()}")
        })


        /* val service = SomeDatabaseService.createProxy(vertx, "database-service-address")

         // Save some data in the database - this time using the proxy
         service.save("mycollection", JsonObject().put("name", "tim")) { res2 ->
             if (res2.succeeded()) {
                 // done
             }
         }*/
    }
}