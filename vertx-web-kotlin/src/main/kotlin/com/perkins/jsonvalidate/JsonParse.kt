package com.perkins.jsonvalidate

import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rx.Observable
import rx.Single
import rx.Single.just
import rx.schedulers.Schedulers

class JsonParse {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun testUpdateJson() {
        val str = "{\"condition\":{\"\$and\":[{\"event_classify_id\":\"5d8044fd7404c5a1307daa12\",\"context\":{\"\$regex\":\"号码是\"},\"publish_date\":{\"\$gte\":1570464000000,\"\$lte\":1572537599000},\"handle_status\":3,\"nodes\":{\"\$elemMatch\":{\"handle_date\":{\"\$gte\":1569859200000,\"\$lte\":1572537599000}}}},{\"\$or\":[{\"__FIELD__mobile\":\"15888888888\"},{\"\$and\":[{\"__FIELD__mobile\":{\"\$ne\":null,\"\$exists\":true}},{\"__FIELD__mobile\":{\"\$in\":[null],\"\$exists\":true}},{\"__FIELD__wqere\":\"asdfd\"}]}]}]},\"showTable\":true,\"currentPage\":1}\n"
        val json = JsonObject(str).getJsonObject("condition")
        val dataMap = mutableMapOf<String, Any>()

        val list = mutableListOf<Observable<JsonArray>>()

        Single.just(getEncryptionField(json, dataMap)).map { map ->
            // 这里虽然用subscribeOn切换了线程，但这里也就是串行的
            map.map { item ->
                when (item.key == "context") {
                    true -> {
                        getEventId(item.value as String).map {
                            JsonObject().put(item.key, it)
                        }
                    }
                    false -> {
                        encryptData(item.value as String).map {
                            JsonObject().put(item.key, it)
                        }
                    }
                }
            }
        }.flatMap { singleList ->
            singleList.fold(just(JsonObject())) { a, b ->
                Single.zip(a, b) { c, d ->
                    c.mergeIn(d)
                }
            }.map {
                val data = replaceData(json, it)
                logger.info(data.toString())
            }
        }.subscribe()
        Thread.sleep(4000)
    }

    // 字段加密
    fun encryptData(context: String): Single<String> {
        return Single.create<String> { sub ->
            Thread(Runnable {
                Thread.sleep(3000) //模拟耗时
                logger.info("加密数据：$context")
                sub.onSuccess("加密密文")
            }).start()
        }
    }

    //模糊查询
    fun getEventId(context: String): Single<JsonArray> {
        logger.info("模糊查询：$context")
        return Single.create<JsonArray> { sub ->
            Thread(Runnable {
                Thread.sleep(2000)//模拟耗时
                // 调用mysql执行模糊查询
                val handler = Handler<JsonArray> {
                    logger.info("获取到模糊搜索结果")
                    sub.onSuccess(it)
                }
                handler.handle(JsonArray().add("id1").add("id2"))
            }).start()
        }
    }


    //提取加密数据
    private fun getEncryptionField(condition: JsonObject, map: MutableMap<String, Any>): MutableMap<String, Any> {
        condition.forEach { entry ->
            if (entry.value is JsonArray) {// 如果是数组则循环处理
                (entry.value as JsonArray).forEach {
                    if (it != null) {
                        getEncryptionField(it as JsonObject, map)
                    }
                }
            } else {
                when (val key = entry.key) {
                    "context" -> {
                        val context = entry.value
                        if (context is JsonObject) {
                            val context = context.getString("\$regex")
                            if (!context.isNullOrBlank()) {
                                map[entry.key] = context
                            }

                        }
                    }
                    else -> {
                        when (key.startsWith("__FIELD__")) {
                            true -> {
                                if (entry.value is String) {
                                    map[entry.key] = entry.value
                                } else {
                                    getEncryptionField(entry.value as JsonObject, map)
                                }
                            }
                            false -> {
                                if (entry.value is JsonObject) {
                                    getEncryptionField(entry.value as JsonObject, map)
                                }
                            }
                        }
                    }
                }
            }
        }
        return map
    }

    //数据替换
    private fun replaceData(condition: JsonObject, data: JsonObject): Any {
        val result = JsonObject()
        condition.map { entry ->
            if (entry.value is JsonArray) {// 如果是数组则循环处理
                val list = (entry.value as JsonArray).map {
                    it?.let { item ->
                        replaceData(item as JsonObject, data)
                    }
                }
                JsonObject().put(entry.key, list)
            } else {
                when (val key = entry.key) {
                    "context" -> {
                        val context = entry.value
                        if (context is JsonObject && !context.getString("\$regex").isNullOrBlank()) {
                            JsonObject().put("_id", JsonObject().put("\$in", data.getValue("context")))
                        } else {
                            entry
                        }
                    }
                    else -> {
                        when (key.startsWith("__FIELD__")) {
                            true -> {
                                if (entry.value is String) {
                                    val context = data.getValue(entry.key)
                                    JsonObject().put(entry.key, context)
                                } else {
                                    JsonObject().put(entry.key, replaceData(entry.value as JsonObject, data))
                                }
                            }
                            false -> {
                                if (entry.value is JsonObject) {
                                    JsonObject().put(entry.key, replaceData(entry.value as JsonObject, data))
                                } else {
                                    entry
                                }
                            }
                        }
                    }
                }
            }
        }.forEach {
            if (it is JsonObject) {
                result.mergeIn(it)
            } else {
                val item = it as Map.Entry<String, Any>
                result.put(item.key, item.value)
            }
        }
        return result
    }
}