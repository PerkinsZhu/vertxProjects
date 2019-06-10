package com.perkins.eventbus.handles

import com.perkins.util.Base64Utils
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.file.AsyncFile
import io.vertx.core.file.OpenOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import java.io.File
import java.io.FileOutputStream

class Handle(vertx: Vertx) {
    val logger = LoggerFactory.getLogger(this.javaClass)

    val fs = vertx.fileSystem()
    //上传完成之后清除缓存数据
    // 超时之后清除缓存
    val fileIdMap = mutableMapOf<String, JsonObject>()
    val fileIdToFile = mutableMapOf<String, AsyncFile>()

    val fileData = Handler<Message<JsonObject>> { msg ->
        val body = msg.body()
        val string = body.getString("data")
        val fileName = body.getString("fileName")

        println("fileName$fileName")
        val byteArray = string.toByteArray(Charsets.ISO_8859_1)
        // 注意这里文件目录需要存在
        val filePath = "uploads/${System.currentTimeMillis().toString() + "-" + fileName}"
        println(filePath)
        println(System.getProperty("vertx.cwd"))

        val options = OpenOptions()
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


    // 获取分块上传的文件ID
    val startMulitUpload = Handler<Message<JsonObject>> { msg ->
        val body = msg.body()
        val fileName = body.getString("fileName")
        val blobCount = body.getInteger("blobCount")
        val fileId = System.nanoTime().toString() + "-" + fileName
        val meatData = JsonObject()
        meatData.put("fileName", fileName)
        meatData.put("blobCount", blobCount)
        fileIdMap.put(fileId, meatData)
        val options = OpenOptions()
        options.setAppend(true)
        val asyFile = fs.openBlocking("uploads/$fileId", options)
        asyFile.exceptionHandler {
            logger.info("文件操作异常", it)
        }
        fileIdToFile.put(fileId, asyFile)
        msg.reply(fileId)
    }
    val mulitUpload = Handler<Message<JsonObject>> { msg ->
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
        val fileId = body.getString("fileId")
        val fileName = fileIdMap.getOrDefault(fileId, System.nanoTime().toString())
        val byteArray = ByteArray(array.size())
        println("fileName-->$fileName")
        val list = array.list
        list.forEachIndexed { index, value ->
            run {
                val byte = value as Integer
                byteArray.set(index, byte.toByte())
            }
        }
        val filePath = "uploads/${System.currentTimeMillis().toString() + "-" + fileName}"
        println(filePath)

        val options = OpenOptions()
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

    var stringBuffer = Buffer.buffer()

    val mulitUploadWithBase64 = Handler<Message<String>> { msg ->
        val body = msg.body()
        val dataBody = body.substringAfterLast(",")

        val header = msg.headers()
        val fileId = header["fileId"]
        val position = header["position"].toLong()
        val currentBlogCount = header["currentBlogCount"]

        val fileMetaData = fileIdMap.get(fileId)
        val asyncFile = fileIdToFile[fileId]

        if (fileMetaData == null) {
            throw  RuntimeException("未找到文件的metadata数据")
        } else {
            val fileName = fileMetaData.getString("fileName")
            val blobCount = fileMetaData.getInteger("blobCount")
            logger.info("fileName:$fileName,blobCount:$blobCount,currentBlogCount:$currentBlogCount")

            val byteArray = Base64Utils.decode(dataBody)

            logger.info("asyncFile$asyncFile")
            asyncFile?.let {
                logger.info("=============开始写入第$currentBlogCount 块数据=============$position")
                //TODO 这里写文件数据会覆盖掉
                it.write(Buffer.buffer(byteArray))
                /*asyFile.write(Buffer.buffer(byteArray),position){}*/
                it.flush()
                logger.info("=============第$currentBlogCount 块数据写入结束=============")
                if (currentBlogCount == blobCount.toString()) {
                    logger.info("文件传输结束，关闭数据流")
                    asyncFile.close()
                    fileIdToFile.remove(fileId)
                    fileIdMap.remove(fileId)
                }
            }

            msg.reply("$currentBlogCount upload success")
        }
    }

    val projectPath = System.getProperty("user.dir")

    fun writeDataToFile(filePath: String, data: ByteArray) {
        val fileInputStream = FileOutputStream(File(projectPath + File.separator + filePath))
        fileInputStream.write(data)
        fileInputStream.close()

    }
}