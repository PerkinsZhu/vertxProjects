package com.perkins.jsonvalidate

import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers
import java.lang.NullPointerException
import javax.swing.plaf.synth.SynthScrollBarUI

class JsonParse {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun testUpdateJson() {
        val str = "{\"condition\":{\"\$and\":[{\"event_classify_id\":\"5d8044fd7404c5a1307daa12\",\"context\":{\"\$regex\":\"context\"}},{\"\$and\":[{\"\$or\":[{\"__FIELD__msutfill\":{\"\$regex\":\"123\"}},{\"__FIELD__custNo\":{\"\$regex\":\"qwer\"}},{\"\$and\":[{\"__FIELD__mobile\":\"15888888\"},{\"__FIELD__custNo\":{\"\$regex\":\"00000000000\"}},{\"__FIELD__wqere\":\"8559565232\"},{\"__FIELD__custNo\":\"1221212121212121212\"},{\"__FIELD__custNo\":{\"\$ne\":null,\"\$exists\":true}}]},{\"__FIELD__mobile\":{\"\$in\":[null],\"\$exists\":true}}]},{\"\$and\":[{\"__FIELD__custNo\":{\"\$regex\":\"asdfasdfasdf\"}},{\"__FIELD__custNo\":\"sssdasdfasdf\"},{\"__FIELD__mobile\":{\"\$ne\":null,\"\$exists\":true}}]},{\"\$and\":[{\"__FIELD__custNo\":\"asdfasdfasdfasdf\"},{\"__FIELD__custNo\":\"asdfasdfasdfasdfasdf\"},{\"__FIELD__mobile\":{\"\$ne\":null,\"\$exists\":true}}]}]}]},\"showTable\":true,\"currentPage\":1}\n"
        val json = JsonObject(str).getJsonObject("condition")
        println(json)
        val start = System.currentTimeMillis()
        val list = mutableListOf<Single<*>>()
        getEncryptionField(json, list)
        Observable.from(list).flatMap {
            it.toObservable()
        }.toList().observeOn(Schedulers.immediate()).subscribe {
            println(json)
            print(System.currentTimeMillis() - start)
        }
    }

    // 字段加密
    fun encryptData(context: String, handler: Handler<String>): Single<String> {
        return Single.create<String> { sub ->
                Thread.sleep(3000) //模拟耗时
                logger.info("加密数据：$context")
                val result = "UESDILWJMIEVASDFASDSLIDUFHJASDFSADFASDFLOADHJL"
                handler.handle(result)
                sub.onSuccess(result)
//                sub.onError(NullPointerException())
        }.subscribeOn(Schedulers.computation())
    }

    //模糊查询
    fun getEventId(context: String, handler: Handler<JsonArray>): Single<JsonArray> {
        return Single.create<JsonArray> { sub ->
                logger.info("模糊查询：$context")
                Thread.sleep(5000)//模拟耗时
                // 调用mysql执行模糊查询
                val result = JsonArray().add("id1").add("id2")
                handler.handle(result)
                sub.onSuccess(result)
        }.subscribeOn(Schedulers.io())
    }


    //提取加密数据
    private fun getEncryptionField(condition: JsonObject, list: MutableList<Single<*>>): MutableList<Single<*>> {
        condition.forEach { entry ->
            if (entry.value is JsonArray) {// 如果是数组则循环处理
                (entry.value as JsonArray).forEach {
                    if (it != null) {
                        getEncryptionField(it as JsonObject, list)
                    }
                }
            } else {
                when (val key = entry.key) {
                    "context" -> {
                        val context = entry.value
                        if (context is JsonObject) {
                            val newContext = context.getString("\$regex")
                            if (!newContext.isNullOrBlank()) {
                                list.add(getEventId(newContext, Handler {
                                    condition.remove("context")
                                    condition.put("_id", JsonObject().put("\$in", it))
                                }))
                            }
                        }
                    }
                    else -> {
                        when (key.startsWith("__FIELD__")) {
                            true -> {
                                if (entry.value is String) {
                                    list.add(encryptData(entry.value as String, Handler {
                                        condition.put(entry.key, it)
                                    }))
                                } else {
                                    if (entry.value.toString().contains("\$regex")) {
                                        val regex = entry.value as JsonObject
                                        val context = regex.getString("\$regex")
                                        if (!context.isNullOrBlank()) {
                                            list.add(encryptData(context, Handler {
                                                regex.put("\$regex", it)
                                            }))
                                        }
                                    } else {
                                        getEncryptionField(entry.value as JsonObject, list)
                                    }
                                }
                            }
                            false -> {
                                if (entry.value is JsonObject) {
                                    getEncryptionField(entry.value as JsonObject, list)
                                }
                            }
                        }
                    }
                }
            }
        }
        return list
    }
}