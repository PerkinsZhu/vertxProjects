package com.perkins.sign

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.impl.JsonObjectBsonAdapter
import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.time.ZoneOffset

object SignAppAuto {
    @JvmStatic
    fun main(args: Array<String>) {

        val nonce = RandomStringUtils.randomAlphabetic(10).toString()
        val timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        val token = RandomStringUtils.randomAlphabetic(20).toString()
        try {

            val list = (1 until  20).toList().map { i ->
                JsonObject().put("mass_id", "zpj-auto-$i")
                        .put("mass_name", "zpj-auto-$i")
                        .put("account_id", "zpj-auto-id-$i")
                        .put("real_name", "zpj-auto-name-$i")
            }
            val data = JsonArray(list)
            SignUtil().createSign(nonce, timestamp, token, data)
        } catch (e: Exception) {
            println("json format error")
            e.printStackTrace()
        }
    }
}