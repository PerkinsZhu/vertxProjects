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
import org.bson.types.ObjectId
import java.io.File
import java.io.FileOutputStream

class Handle(vertx: Vertx) {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val executor = vertx.createSharedWorkerExecutor("myWorker")
    val fs = vertx.fileSystem()
    // TODO 上传完成之后清除缓存数据
    //TODO 超时之后清除缓存
    //TODO 实现并发存储
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


    // 开始进行分块上传，获取分块上传的文件ID
    val startMulitUpload = Handler<Message<JsonObject>> { msg ->
        val body = msg.body()
        val fileName = body.getString("fileName")
        val blobCount = body.getInteger("blobCount")
        val fileId = ObjectId.get().toString() + "-" + fileName //生成唯一文件名

        //缓存当前文件基本信息
        val meatData = JsonObject()
        meatData.put("fileName", fileName)
        meatData.put("blobCount", blobCount)
        fileIdMap.put(fileId, meatData)

        //缓存当前文件的asyFile对象
        val options = OpenOptions()
        val asyFile = fs.openBlocking("uploads/$fileId", options)
        asyFile.exceptionHandler {
            logger.info("文件操作异常", it)
        }
        fileIdToFile.put(fileId, asyFile)

        //回复客户端 文件Id(新文件名)
        val data = JsonObject()
        data.put("fileId", fileId)
        val result = getResult(data, 0)
        msg.reply(result)
    }

    private fun getResult(data: JsonObject, code: Int, msg: String? = null): JsonObject {
        val result = JsonObject()
        result.put("code", code)
        if (msg.isNullOrBlank()) {
            val message = when (code) {
                0 -> "处理成功"
                else -> "处理失败"
            }
            result.put("msg", message)
        } else {
            result.put("msg", msg)
        }
        result.put("data", data)
        return result
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

    var allDataBuffer = Buffer.buffer()

    //接收分块文件数据。二进制数据按照base64进行编码
    val mulitUploadWithBase64 = Handler<Message<String>> { msg ->
        val dataBody = msg.body()

        val header = msg.headers()
        val fileId = header["fileId"]
        val position = header["position"].toLong()

        val currentBlogCount = header["currentBlogCount"]

        val fileMetaData = fileIdMap.get(fileId)
        val asyncFile = fileIdToFile[fileId]

        if (fileMetaData == null || asyncFile == null) {
            logger.error("未找到文件的metadata数据或AsyncFile对象")
            throw  RuntimeException("文件初始化异常")
        } else {
            val fileName = fileMetaData.getString("fileName")
            val blobCount = fileMetaData.getInteger("blobCount")

            logger.debug("fileName:$fileName,blobCount:$blobCount,currentBlogCount:$currentBlogCount")
            val byteArray = Base64Utils.decode(dataBody)

            asyncFile?.let {
                logger.debug("=============共 $blobCount 块数据，开始写入第$currentBlogCount 块数据=============")
                it.write(Buffer.buffer(byteArray))
                it.flush()
                logger.debug("=============第$currentBlogCount 块数据写入结束=============")
                if (currentBlogCount == blobCount.toString()) {
                    logger.debug("最后一块数据写入完成，文件传输结束，关闭数据流")
                    asyncFile.close()
                    //清空缓存
                    fileIdToFile.remove(fileId)
                    fileIdMap.remove(fileId)
                }
            }
        }

        val data = JsonObject()
        val result = getResult(data, 0, "第$currentBlogCount 块文件上传成功")
        msg.reply(result)
    }

    // 用来测试寸，一次性存储所有的数据
    private fun saveToFileOnce(fileId: String?) {
        // 数据接收完成之后一次性写入一个新的文件
        val openOptions = OpenOptions()
        fs.open("uploads/new-$fileId", openOptions) {
            if (it.succeeded()) {
                val asyFile = it.result()
                val byteArray = Base64Utils.decode(allDataBuffer.toString())
                asyFile.write(Buffer.buffer(byteArray), 0) {
                    //TODO 如何关闭文件流
                    asyFile.close()
                }
            }
        }
    }
}

val projectPath = System.getProperty("user.dir") + "/vertx-web-kotlin"

fun writeDataToFile(filePath: String, data: ByteArray) {
    val file = File(projectPath + File.separator + filePath)
    println(file.absolutePath)
    if (!file.exists()) {
        file.createNewFile()
    }
    val fileInputStream = FileOutputStream(file, true)
    fileInputStream.write(data)
    fileInputStream.close()

}
