package com.perkins.vertxs

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.UpdateOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler


class MongoVerticle : AbstractVerticle() {
    private val collectionName = "books"
    override fun start() {
        val config = JsonObject().put("db_name", "test")
                .put("host", "localhost")
                .put("port", 27017)
        var client = MongoClient.createShared(vertx, config)

        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.get("/list").handler {
            val query = JsonObject()
            client.find(collectionName, query) { res -> res.map { item -> item.map { json -> println(json) } } }
            it.response().end("hello")
        }


        router.get("/add").handler {
            val document = JsonObject().put("title", "The Hobbit")
            client.save(collectionName, document) { res ->
                if (res.succeeded()) {
                    val id = res.result()
                    println("Saved book with id $id")
                } else {
                    res.cause().printStackTrace()
                }

            }
            it.response().end("hello")
        }

        router.route("/update").handler {
            val query = JsonObject().put("title", "The Hobbit")
            val update = JsonObject().put("\$set", JsonObject().put("author", "J. R. R. Tolkien"))
            val options = UpdateOptions().setMulti(true).setUpsert(true)
            client.updateCollectionWithOptions("books", query, update, options) { res ->
                if (res.succeeded()) {
                    println("Book updated !")
                } else {
                    res.cause().printStackTrace()
                }
            }
            it.response().end("hello")
        }
        server.requestHandler(router::accept).listen(8080)
    }
}