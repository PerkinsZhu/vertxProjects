package com.perkins.mongodb

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import org.junit.Test

class APPTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
        }
    }

    @Test
    fun testMongodb() {
        val vertx = Vertx.vertx()
        val config = JsonObject()
   /*     config.put("host", "127.0.0.1")
        config.put("port", 27017)
        config.put("db_name", "test")*/
        config.put("connection_string", "mongodb://localhost:27017/test") //该配置项会忽略其他所有的配置项

        val client = MongoClient.createShared(vertx, config, "MyPoolName")

        val document = JsonObject().put("title", "The Hobbit")

        client.save("books", document) { res ->

            if (res.succeeded()) {

                val id = res.result()
                println("Saved book with id $id")

            } else {
                res.cause().printStackTrace()
            }

        }

        Thread.sleep(5000)
    }


}