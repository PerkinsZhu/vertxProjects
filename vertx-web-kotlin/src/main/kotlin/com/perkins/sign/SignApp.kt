package com.perkins.sign

import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.time.ZoneOffset

object SignApp {
    @JvmStatic
    fun main(args: Array<String>) {
//        val path = System.getProperty("user.dir") + File.separator+"data.json"
        val path = "D:\\myProjects\\vertxProjects\\vertx-web-kotlin\\src\\main\\kotlin\\com\\perkins\\sign\\data.json"
        val dataStr = FileReader(File(path)).readText()
        val nonce = RandomStringUtils.randomAlphabetic(10).toString()
        val timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        val token = RandomStringUtils.randomAlphabetic(20).toString()
        try {
            val data = if (dataStr.startsWith("[")) {
                io.vertx.core.json.JsonArray(dataStr)
            } else {
                io.vertx.core.json.JsonObject(dataStr)
            }
            SignUtil().createSign(nonce, timestamp, token, data)
        } catch (e: Exception) {
            println("json format error")
            e.printStackTrace()
        }
    }
    // 第五次提交
}