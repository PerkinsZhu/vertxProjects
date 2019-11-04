package com.perkins.mongodb

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.ext.mongo.FindOptions
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rx.schedulers.Schedulers
import java.util.concurrent.CountDownLatch

class PressureTest {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    val client = MongClientUtil.getRxClient()
    private val countDownLatch = CountDownLatch(1)

    @Test
    fun doTask() {

        val table = "event"
        val query = JsonObject("{\"\$and\":[{\"view_platform\":{\"\$in\":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63]}},{\"event_classify_id\":\"5d8044fd7404c5a1307daa12\"}]}")
        client.rxCount(table, query).observeOn(Schedulers.io()).flatMap {
            logger.info("count:$it")
            val option = FindOptions()
            option.batchSize = 10000
            client.rxFindWithOptions(table, query,option).map { list ->
                logger.info("data size:${list.size}")
                1
            }
        }.subscribe {
            countDownLatch.countDown()
        }



        countDownLatch.await()
        logger.info("====end====")
    }


}
