package com.perkins.eventbus.handles

import com.amazonaws.services.s3.model.InitiateMultipartUploadResult
import com.amazonaws.services.s3.model.PartETag
import com.perkins.awss3.S3Service
import com.perkins.common.PropertiesUtil
import com.perkins.util.Base64Utils
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.file.AsyncFile
import io.vertx.core.file.OpenOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.BulkOperation
import net.coobird.thumbnailator.Thumbnails
import org.apache.commons.codec.digest.Md5Crypt
import org.apache.http.util.ByteArrayBuffer
import org.bson.types.ObjectId
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Handle(vertx: Vertx) {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val executor = vertx.createSharedWorkerExecutor("myWorker")
    val fs = vertx.fileSystem()
    // 上传完成之后清除缓存数据
    // 超时之后清除缓存
    //TODO 实现并发存储  需要前端实现并发传输进行测试
    private val fileIdMap = mutableMapOf<String, JsonObject>()
    private val fileIdToFile = mutableMapOf<String, AsyncFile>()
    private val fileIdToUploadResultMap = mutableMapOf<String, InitiateMultipartUploadResult>()
    private val fileIdToPartETagMap = mutableMapOf<String, MutableList<PartETag>>()

    val accessKey = PropertiesUtil.get("accessKey")
    val secretKey = PropertiesUtil.get("secretKey")
    val endpoint = PropertiesUtil.get("endpoint")
    val bucketName = PropertiesUtil.get("bucketName")
    val s3Service = S3Service(accessKey, secretKey, endpoint)

    init {
        val executor = Executors.newScheduledThreadPool(1)
        // 缓存有效期设置为1分钟，定时器执行间隔1分钟，因此，实际上缓存存活时间在1-2分钟之间
        val timeOut = 1000000000 * 60 * 1L
        /*executor.scheduleAtFixedRate({
            logger.info("clean cache data")
            val expirationTime = System.nanoTime() - timeOut
            val cleanKeys = fileIdMap.filter { entry ->
                val value = entry.value
                val createTime = value.getLong("createTime")
                expirationTime > createTime
            }.map { it.key }
            cleanKeys.forEach { key ->
                fileIdMap.remove(key)
                fileIdToFile[key]?.let {
                    it.close()
                }
                fileIdToFile.remove(key)
            }
            logger.info("当前缓存数量:fileIdToFile:${fileIdToFile.size},fileIdMap:${fileIdMap.size}")
        }, 1L, 1L, TimeUnit.MINUTES)*/
    }

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
        val contentSize = body.getLong("contentSize")
        val contentType = body.getString("contentType", "application/octet-stream")


        val fileId = ObjectId.get().toString() + "-" + fileName //生成唯一文件名

        //缓存当前文件基本信息
        val meatData = JsonObject()
        meatData.put("fileName", fileName)
        meatData.put("blobCount", blobCount)
        meatData.put("contentSize", contentSize)
        meatData.put("createTime", System.nanoTime())
        meatData.put("contentType", contentType)
        fileIdMap.put(fileId, meatData)

        //缓存当前文件的asyFile对象
        val options = OpenOptions()
        // 这里需要阻塞方式打开文件，不然有可能会导致数据块提交上来，但是文件还没有打开
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
        /**
         * 思路：
         *      前端先调用接口获取分块文件ID，后端把Id和文件名做缓存
         *      前端分批次传送数据，附带id
         *      后端根据iD映射到文件名称，把数据追加到文件后面
         */
        val body = msg.body()
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

        val currentBlogNum = header["currentBlogNum"]

        val fileMetaData = fileIdMap.get(fileId)
        val asyncFile = fileIdToFile[fileId]

        if (fileMetaData == null || asyncFile == null) {
            logger.error("未找到文件的metadata数据或AsyncFile对象")
            throw  RuntimeException("文件初始化异常")
        } else {
            val fileName = fileMetaData.getString("fileName")
            val contentType = fileMetaData.getString("contentType")
            val blobCount = fileMetaData.getInteger("blobCount")
            val contentSize = fileMetaData.getLong("contentSize")

            logger.debug("fileName:$fileName,blobCount:$blobCount,currentBlogCount:$currentBlogNum")
            val byteArray = Base64Utils.decode(dataBody)

            asyncFile?.let {
                logger.debug("=============共 $blobCount 块数据，开始写入第$currentBlogNum 块数据=============")
                // 循环按照顺序写入
                // it.write(Buffer.buffer(byteArray))
                // 指定写入位置，可以实现并发上传
                val position = ((currentBlogNum.toInt() - 1) * contentSize)
                it.write(Buffer.buffer(byteArray), position) {
                    if (it.succeeded()) {
                        logger.info("第 $currentBlogNum 块数据写入 $position 成功")
                    } else {
                        logger.error("第 $currentBlogNum 块数据写入 $position 失败")
                    }
                }
                it.flush()
                logger.debug("=============第$currentBlogNum 块数据写入结束=============")
                if (currentBlogNum == blobCount.toString()) {
                    logger.debug("最后一块数据写入完成，文件传输结束，关闭数据流")
                    asyncFile.close()
                    //清空缓存
                    fileIdToFile.remove(fileId)
                    fileIdMap.remove(fileId)
//                    val filePath = "D:\\myProjects\\vertxProjects\\vertx-web-kotlin\\src\\main\\kotlin\\com\\perkins\\eventbus\\uploads\\$fileId"
//                    sendFileToS3(filePath, contentType, fileName)
                }
            }
        }

        val data = JsonObject()
        val result = getResult(data, 0, "第$currentBlogNum 块文件上传成功")
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


    // 从本地磁盘读取数据传输到S3
    fun sendFileToS3(filePath: String, contentType: String, originaFileName: String) {
        val uploadedFileName = filePath
        val path = Base64Utils.encode(filePath.substringAfterLast("\\").toByteArray())
        val pathWithObjectSuffix = "$path-${transformBucketName(bucketName) + "-test"}"
        val userMetadata = mutableMapOf<String, String>()
        userMetadata["contentType"] = contentType
        userMetadata["OriginalName"] = URLEncoder.encode(originaFileName, "UTF-8")
        val putObjectResult = s3Service.addObject(bucketName, pathWithObjectSuffix, uploadedFileName, userMetadata)
        if (putObjectResult != null) {
            logger.info("文件上传成功")
        }
        /*
        val newFileName = ""
        val thumbPath = "thumbnail-$path-$newFileName"
        Thumbnails.of("").size(1111, 222).toFile(File("$thumbPath"))
        */
    }

    fun transformBucketName(realBucketName: String): String {
        return realBucketName.replace("-".toRegex(), "+")
    }


    val startMulitUploadToS3 = Handler<Message<JsonObject>> { msg ->
        val body = msg.body()
        val fileName = body.getString("fileName")
        val blobCount = body.getInteger("blobCount")
        val contentSize = body.getLong("contentSize")
        val contentType = body.getString("contentType", "application/octet-stream")


        val fileId = ObjectId.get().toString() + "-" + fileName //生成唯一文件名

        //缓存当前文件基本信息
        val meatData = JsonObject()
        meatData.put("fileName", fileName)
        meatData.put("blobCount", blobCount)
        meatData.put("contentSize", contentSize)
        meatData.put("createTime", System.nanoTime())
        meatData.put("contentType", contentType)
        fileIdMap.put(fileId, meatData)

        //初始化S3分块上传逻辑
        val tempResult = s3Service.initiateMultipartUpload(bucketName, fileId)
        tempResult?.let {
            logger.info("开始上传文件:$fileId,fileName:$fileName")
            fileIdToUploadResultMap.put(fileId, it)
        }

        //回复客户端 文件Id(新文件名)
        val data = JsonObject()
        data.put("fileId", fileId)
        val result = getResult(data, 0)
        msg.reply(result)
    }

    // 按照base64编码上传数据，直接把数据转发S3,不再实例化到磁盘
    val mulitUploadWithBase64AndSendToS3 = Handler<Message<String>> { msg ->
        val dataBody = msg.body()

        val header = msg.headers()
        val fileId = header["fileId"]

        val currentBlogNum = header["currentBlogNum"]

        val fileMetaData = fileIdMap.get(fileId)
        val initResult = fileIdToUploadResultMap[fileId]

        if (fileMetaData == null || initResult == null) {
            logger.error("未找到文件的metadata数据或AsyncFile对象")
            throw  RuntimeException("文件初始化异常")
        } else {
            val fileName = fileMetaData.getString("fileName")
            val contentType = fileMetaData.getString("contentType")
            val blobCount = fileMetaData.getInteger("blobCount")
            val contentSize = fileMetaData.getLong("contentSize")

            logger.info("fileName:$fileName,blobCount:$blobCount,currentBlogCount:$currentBlogNum")
            val byteArray = Base64Utils.decode(dataBody)

            initResult?.let {
                logger.debug("=============共 $blobCount 块数据，开始写入第$currentBlogNum 块数据=============")

                val uploadId = it.uploadId
                val partResult = s3Service.uploadPart(bucketName,
                        fileId,
                        uploadId,
                        currentBlogNum.toInt(),
                        ByteArrayInputStream(byteArray),
                        byteArray.size.toLong())

                val list = fileIdToPartETagMap.getOrDefault(fileId, mutableListOf<PartETag>())
                partResult?.let { res ->
                    list.add(res.partETag)
                }
                fileIdToPartETagMap[fileId] = list

                logger.debug("=============第$currentBlogNum 块数据写入结束=============")
                if (currentBlogNum == blobCount.toString()) {
                    logger.debug("最后一块数据写入完成，文件传输结束，关闭数据流")

                    //结束S3上传逻辑
                    val complets = s3Service.completeMultipartUpload(bucketName, fileId, uploadId, list)
                    if (complets != null) {
                        logger.info("文件上传结束!")
                    } else {
                        logger.error("文件上传S3 结束失败")
                    }
                    fileIdToPartETagMap.remove(fileId)
                    fileIdToUploadResultMap.remove(fileId)
                    //清空缓存
                    fileIdToFile.remove(fileId)
                    fileIdMap.remove(fileId)
                }
            }
        }

        val data = JsonObject()
        val result = getResult(data, 0, "第$currentBlogNum 块文件上传成功")
        msg.reply(result)
    }


    // 按照二进制字符串上传分块数据
    val mulitUploadWithByteStringAndSendToS3 = Handler<Message<String>> { msg ->
        val dataBody = msg.body()

        val header = msg.headers()
        val fileId = header["fileId"]

        val currentBlogNum = header["currentBlogNum"]

        val fileMetaData = fileIdMap.get(fileId)
        val initResult = fileIdToUploadResultMap[fileId]

        if (fileMetaData == null || initResult == null) {
            logger.error("未找到文件的metadata数据或AsyncFile对象")
            throw  RuntimeException("文件初始化异常")
        } else {
            val fileName = fileMetaData.getString("fileName")
            val contentType = fileMetaData.getString("contentType")
            val blobCount = fileMetaData.getInteger("blobCount")
            val contentSize = fileMetaData.getLong("contentSize")

            logger.info("fileName:$fileName,blobCount:$blobCount,currentBlogCount:$currentBlogNum")
            val byteArray = dataBody.toByteArray(Charsets.ISO_8859_1)
            initResult?.let {
                logger.debug("=============共 $blobCount 块数据，开始写入第$currentBlogNum 块数据=============")
                val uploadId = it.uploadId
                val partResult = s3Service.uploadPart(bucketName, fileId,
                        uploadId,
                        currentBlogNum.toInt(),
                        ByteArrayInputStream(byteArray),
                        byteArray.size.toLong())

                val list = fileIdToPartETagMap.getOrDefault(fileId, mutableListOf())
                partResult?.let { res ->
                    list.add(res.partETag)
                }
                fileIdToPartETagMap.put(fileId, list)


                logger.debug("=============第$currentBlogNum 块数据写入结束=============")
                if (currentBlogNum == blobCount.toString()) {
                    logger.debug("最后一块数据写入完成，文件传输结束，关闭数据流")

                    //结束S3上传逻辑
                    val complets = s3Service.completeMultipartUpload(bucketName, fileId, uploadId, list)
                    if (complets != null) {
                        logger.info("文件上传结束!")
                    } else {
                        logger.error("文件上传S3 结束失败")
                    }
                    fileIdToPartETagMap.remove(fileId)
                    fileIdToUploadResultMap.remove(fileId)
                    //清空缓存
                    fileIdToFile.remove(fileId)
                    fileIdMap.remove(fileId)
                }
            }
        }

        val data = JsonObject()
        val result = getResult(data, 0, "第$currentBlogNum 块文件上传成功")
        msg.reply(result)
    }
}

val projectTempPath = System.getProperty("user.dir") + File.separator + "temp" + File.separator

fun writeDataToFile(filePath: String, data: ByteArray) {
    val file = File(projectTempPath  + File.separator + filePath)
    println(file.absolutePath)
    if (!file.exists()) {
        file.createNewFile()
    }
    val fileInputStream = FileOutputStream(file, true)
    fileInputStream.write(data)
    fileInputStream.close()

}
