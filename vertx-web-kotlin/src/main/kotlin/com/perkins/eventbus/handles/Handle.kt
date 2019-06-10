package com.perkins.eventbus.handles

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.file.OpenOptions
import io.vertx.core.json.JsonObject

class Handle(vertx: Vertx) {

    val fs = vertx.fileSystem()

    val fileData =  Handler<Message<JsonObject>> { msg ->
        val body = msg.body()
        val string = body.getString("data")
        val fileName = body.getString("fileName")

        println("fileName$fileName")
        val byteArray = string.toByteArray(Charsets.ISO_8859_1)
        // 注意这里文件目录需要存在
        val filePath = "uploads/${System.currentTimeMillis().toString() + "-" + fileName}"
        println(filePath)
        println(System.getProperty("vertx.cwd"))

        val options =OpenOptions()
        fs.open(filePath, options) {
            fs.writeFile(filePath, Buffer.buffer(byteArray)) { it ->
                if (it.succeeded()) {
                    println("上传文件成功")
                } else {
                    println("上传文件失败")
                    it.cause().printStackTrace()
                }
            }
        }
    }
    val mulitUpload =  Handler<Message<JsonObject>> { msg ->
        //TODO 实现文件的分快上传
        /**
         * 思路：
         *      前端先调用接口获取分块文件ID，后端把Id和文件名做缓存
         *      前端分批次传送数据，附带id
         *      后端根据iD映射到文件名称，把数据追加到文件后面
         */
        val body = msg.body()
        println(body)
        val array = body.getJsonArray("data")
        val fileName = body.getString("fileName")

        val byteArray = ByteArray(array.size())
        println("fileName$fileName")
        val list = array.list
        list.forEachIndexed { index, value ->
            run {
                val byte = value as Integer
                byteArray.set(index, byte.toByte())
            }
        }
        println(byteArray)



        println(byteArray)

//        val byteArray:Array<Byte> = array.list.toTypedArray<Byte>()
        // 注意这里文件目录需要存在
        val filePath = "uploads/${System.currentTimeMillis().toString() + "-" + fileName}"
        println(filePath)

        val options =OpenOptions()
        fs.open(filePath, options) {
                fs.writeFile(filePath, Buffer.buffer(byteArray)) { it ->
                if (it.succeeded()) {
                    println("上传文件成功")
                } else {
                    println("上传文件失败")
                    it.cause().printStackTrace()
                }
            }
        }


    }


}