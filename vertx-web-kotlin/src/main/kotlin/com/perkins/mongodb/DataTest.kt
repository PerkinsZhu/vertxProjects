package com.perkins.mongodb

import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.BulkWriteOptions
import io.vertx.kotlin.ext.mongo.BulkOperation
import io.vertx.kotlin.ext.mongo.FindOptions
import io.vertx.rxjava.core.Vertx
import io.vertx.rxjava.ext.mongo.MongoClient
import org.bson.types.ObjectId
import rx.Single
import io.vertx.ext.mongo.BulkOperation
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Schedules
import rx.Scheduler
import rx.schedulers.Schedulers
import kotlin.streams.toList


object DataTest {
    val logger = LoggerFactory.getLogger(this.javaClass)
    @JvmStatic
    fun main(args: Array<String>) {
        updateData()

    }

    fun updateData() {
        val vertx = Vertx.vertx()
        val config = JsonObject()
        config.put("connection_string", "mongodb://localhost:27017/test") //该配置项会忽略其他所有的配置项
        val client = MongoClient.createShared(vertx, config, "MyPoolName")
        val table = "ddd"
        val query = JsonObject()
//        test1(client, table, query)
//        test2(client, table, query)
//        test5(vertx, client, table, query)
        test6(vertx, client, table, query)

        vertx.close()


    }

    private fun test1(client: MongoClient, table: String, query: JsonObject) {
        var isRunning = true
        val options = FindOptions()
                .setBatchSize(5000)


        client.rxFindWithOptions(table, query, options)

                .map {
                    logger.info("=============")
                    it.forEach { json ->
                        logger.info(json.toString())
                    }
                }.subscribe({
                    isRunning = false
                }, {
                    it.printStackTrace()
                    isRunning = false
                })
        while (isRunning) {
            Thread.sleep(1000)
            logger.info("isRunning")
        }
    }

    private fun test2(client: MongoClient, table: String, query: JsonObject) {
        var isRunning = true
        val options = FindOptions()
                .setBatchSize(5000)

        client.findBatchWithOptions(table, query, options)
                .handler {
                    logger.info(it.toString())
                }
                .endHandler {
                    isRunning = false
                    logger.info("====end====")
                }
        while (isRunning) {
            Thread.sleep(1000)
            logger.info("isRunning")
        }
    }

    private fun test3(vertx: Vertx, client: MongoClient, table: String, query: JsonObject) {
        var isRunning = true
        val limit = 5000
        client.rxCount(table, query).flatMap { count ->
            var pageCount = ((count / limit) + 1).toInt()
            logger.info("count = $count")
            vertx.rxExecuteBlocking<Int> {
                for (i in 0 until pageCount) {
                    val skip = (i * limit)
                    val options = FindOptions()
                            .setLimit(limit)
                            .setSkip(skip)
                            .setBatchSize(1000)
                    var isItemRunning = true
                    client.rxFindWithOptions(table, query, options)
                            .flatMap {
                                val newJson = it.map { json ->
                                    val _id = json.getJsonObject("_id")
                                    val query = JsonObject().put("_id", _id)
                                    val update = JsonObject().put("\$set", JsonObject().put("d2", json.getString("d1")))
                                    BulkOperation.createUpdate(query, update, false, true)
                                }
                                client.rxBulkWrite(table, newJson)
                            }.map {
                                logger.info(it.toJson().toString())
                            }
                            .subscribe({
                                isItemRunning = false
                            }, {
                                it.printStackTrace()
                                isItemRunning = false
                            })

                    while (isItemRunning) {
                        Thread.sleep(1000)
                        logger.info("item is RUNNING")
                    }
                    logger.info("$skip completed")
                }
                logger.info("blocking end")
                it.complete(1)
            }
        }.subscribe({
            logger.info(it.toString())
            isRunning = false
        }, {
            it.printStackTrace()
            isRunning = false
        })

        while (isRunning) {
            Thread.sleep(1000)
            logger.info("isRunning")
        }
        logger.info("===END====")
    }

    private fun test4(vertx: Vertx, client: MongoClient, table: String, query: JsonObject) {
        var isRunning = true
        val limit = 5000
        client.rxCount(table, query).flatMap { count ->
            val pageCount = ((count / limit) + 1).toInt()
            logger.info("count = $count")
            vertx.rxExecuteBlocking<List<Single<Int>>> {
                val list = (0 until pageCount).toList().map { i ->
                    val skip = (i * limit)
                    val options = FindOptions()
                            .setLimit(limit)
                            .setSkip(skip)
                            .setBatchSize(1000)
                    val a = client.rxFindWithOptions(table, query, options)
                            .map {
                                logger.info(it.size.toString())
                                1
                            }
                    a
                }
                logger.info("blocking end")
                it.complete(list)
            }

        }.flatMap {
            Single.zip(it) {
                1
            }
        }.subscribe({
            logger.info("sub-->" + it)
            isRunning = false
        }, {
            it.printStackTrace()
            isRunning = false
        })

        while (isRunning) {
            Thread.sleep(1000)
            logger.info("isRunning")
        }
        logger.info("===END====")
    }


    private fun test5(vertx: Vertx, client: MongoClient, table: String, query: JsonObject) {
        var isRunning = true
        val limit = 5000
        client.rxCount(table, query).flatMap { count ->

            var pageCount = ((50000 / limit) + 1).toInt()
            logger.info("count = $count")
            vertx.rxExecuteBlocking<List<Single<Int>>> {
                val aa = (0 until pageCount).map { i ->
                    val skip = (i * limit)
                    val options = FindOptions()
                            .setLimit(limit)
                            .setSkip(skip)
                            .setBatchSize(500)
                    client.rxFindWithOptions(table, query, options)
                            .flatMap {
                                logger.info("updating……$skip")
                                val newJson = it.parallelStream().map { json ->
                                    val _id = json.getJsonObject("_id")
                                    val query = JsonObject().put("_id", _id)
                                    val update = JsonObject().put("\$set", JsonObject().put("d5", json.getString("d1")))
                                    BulkOperation.createUpdate(query, update, false, true)
                                }.toList()
                                client.rxBulkWrite(table, newJson).map {
                                    logger.info(it.toJson().toString())
                                    logger.info("updating completed ……$skip")
                                    1
                                }
                            }
                }
                it.complete(aa)
            }
        }.flatMap {
            Single.zip(it) {
                logger.info("共完成任务数量：" + it.size)
                "end"
            }
        }.subscribe({
            isRunning = false
        }, {
            it.printStackTrace()
            isRunning = false
        })

        while (isRunning) {
            Thread.sleep(1000)
            logger.info("isRunning")
        }
        logger.info("===END====")
    }

    private fun test6(vertx: Vertx, client: MongoClient, table: String, query: JsonObject) {
        var isRunning = true
        val limit = 5000
        client.rxCount(table, query).flatMap { count ->

            var pageCount = ((50000 / limit) + 1).toInt()
            logger.info("count = $count")
            vertx.rxExecuteBlocking<List<Single<Int>>> {
                val aa = (0 until pageCount).map { i ->
                    val skip = (i * limit)
                    val options = FindOptions()
                            .setLimit(limit)
                            .setSkip(skip)
                            .setBatchSize(500)
                    client.rxFindWithOptions(table, query, options)
                            .flatMap { list ->
                                logger.info("updating……$skip")
                                logger.info("1 thread${Thread.currentThread().name}")
                                Single.just(1)
//                                        .observeOn(Schedulers.computation())
                                        .map{
                                    logger.info("2 thread${Thread.currentThread().name}")
                                    list.parallelStream().map { json ->
                                        val _id = json.getJsonObject("_id")
                                        val query = JsonObject().put("_id", _id)
                                        val update = JsonObject().put("\$set", JsonObject().put("d4", json.getString("d1")))
                                        BulkOperation.createUpdate(query, update, false, true)
                                    }.toList()
                                }
//                                        .observeOn(Schedulers.io())
                                        .flatMap { newJson ->
                                    logger.info("3 thread${Thread.currentThread().name}")
                                    client.rxBulkWrite(table, newJson).map {
                                        logger.info(it.toJson().toString())
                                        logger.info("updating completed ……$skip")
                                        1
                                    }
                                }
                            }
                }
                it.complete(aa)
            }
        }.flatMap {
            Single.zip(it) {
                logger.info("共完成任务数量：" + it.size)
                "end"
            }
        }.subscribe({
            isRunning = false
        }, {
            it.printStackTrace()
            isRunning = false
        })

        while (isRunning) {
            Thread.sleep(1000)
            logger.info("isRunning")
        }
        logger.info("===END====")
    }

    private fun test7(vertx: Vertx, client: MongoClient, table: String, query: JsonObject) {
        var isRunning = true
        val limit = 5000
        client.rxCount(table, query).flatMap { count ->

            var pageCount = ((50000 / limit) + 1).toInt()
            logger.info("count = $count")
            vertx.rxExecuteBlocking<List<Single<Int>>> {
                val aa = (0 until pageCount).map { i ->
                    val skip = (i * limit)
                    val options = FindOptions()
                            .setLimit(limit)
                            .setSkip(skip)
                            .setBatchSize(500)
                    client.rxFindWithOptions(table, query, options)
                            .flatMap { list ->
                                Thread.sleep(2000) //睡眠兩秒，等待此輪任务执行结束，腾出缓存
                                logger.info("updating……$skip")
                                logger.info("1 thread${Thread.currentThread().name}")
                                Single.just(1)
//                                        .observeOn(Schedulers.computation())
                                        .map{
                                            logger.info("2 thread${Thread.currentThread().name}")
                                            list.parallelStream().map { json ->
                                                val _id = json.getJsonObject("_id")
                                                val query = JsonObject().put("_id", _id)
                                                val update = JsonObject().put("\$set", JsonObject().put("d4", json.getString("d1")))
                                                BulkOperation.createUpdate(query, update, false, true)
                                            }.toList()
                                        }
//                                        .observeOn(Schedulers.io())
                                        .flatMap { newJson ->
                                            logger.info("3 thread${Thread.currentThread().name}")
                                            client.rxBulkWrite(table, newJson).map {
                                                logger.info(it.toJson().toString())
                                                logger.info("updating completed ……$skip")
                                                1
                                            }
                                        }
                            }
                }
                it.complete(aa)
            }
        }.flatMap {
            Single.zip(it) {
                logger.info("共完成任务数量：" + it.size)
                "end"
            }
        }.subscribe({
            isRunning = false
        }, {
            it.printStackTrace()
            isRunning = false
        })

        while (isRunning) {
            Thread.sleep(1000)
            logger.info("isRunning")
        }
        logger.info("===END====")
    }
}