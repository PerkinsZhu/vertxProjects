package com.perkins.mongodb

import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient
import org.junit.Test
import org.slf4j.LoggerFactory
import rx.Observable
import rx.Single
import rx.Subscriber
import rx.schedulers.Schedulers
import java.util.concurrent.CountDownLatch

class APPTest {
    val logger = LoggerFactory.getLogger(this.javaClass)

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

    @Test
    fun testRxClient() {
        val countDownLatch = CountDownLatch(1)
        val table = "event"
        val vertx = io.vertx.rxjava.core.Vertx.vertx()
        val config = JsonObject().put("connection_string", "mongodb://username:password@ip:port/dbName") //该配置项会忽略其他所有的配置项
        val client = io.vertx.rxjava.ext.mongo.MongoClient.createShared(vertx, config, "MyPoolName")
        val query = JsonObject()
        logger.info("--a----")
//        client.rxCount(table, JsonObject()).map {

//            logger.info("count:$it")
            Observable.range(0, 5).flatMap {
                logger.info("--start--$it")
                val option = FindOptions().setLimit(1000)
                client.rxFindWithOptions(table, query, option).map { list ->
                    logger.info("---$it end ")
                    list.size
                }.toObservable()
            }.map { size ->
                logger.info("----> size :${size}")
                size
            }.subscribe(MySubscriber())

        /*}.subscribe {
            countDownLatch.countDown()
        }*/
        countDownLatch.await()
        logger.info("====end=====")
    }


}

class MySubscriber : Subscriber<Any>() {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun onNext(t: Any) {
        logger.info("on next")
        request(1)
    }

    override fun onCompleted() {
        logger.info("on completed")
    }

    override fun onError(e: Throwable?) {
        logger.info("on error")
    }

    override fun onStart() {
        logger.info("on start")
        request(1)
    }

}