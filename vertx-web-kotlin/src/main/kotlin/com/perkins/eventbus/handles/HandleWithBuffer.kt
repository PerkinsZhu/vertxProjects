package com.perkins.eventbus.handles

import com.amazonaws.services.s3.model.PartETag
import com.amazonaws.services.s3.model.UploadPartResult
import com.perkins.awss3.S3Service
import com.perkins.common.PropertiesUtil
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import org.apache.http.util.ByteArrayBuffer
import org.apache.tika.mime.MimeType
import org.apache.tika.mime.MimeTypes
import org.bson.types.ObjectId
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HandleWithBuffer(vertx: Vertx) {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val executor = vertx.createSharedWorkerExecutor("myWorker")
    val fs = vertx.fileSystem()
    // 上传完成之后清除缓存数据
    // 超时之后清除缓存
    //TODO 实现并发存储  需要前端实现并发传输进行测试
    private val fileIdMap = mutableMapOf<String, JsonObject>()
    private val fileIdToUploadResultMap = mutableMapOf<String, Pair<String, ByteArrayBuffer>>()
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
        executor.scheduleAtFixedRate({
            logger.info("clean cache data")
            val expirationTime = System.nanoTime() - timeOut
            val cleanKeys = fileIdMap.filter { entry ->
                val value = entry.value
                val createTime = value.getLong("createTime")
                expirationTime > createTime
            }.map { it.key }
            cleanKeys.forEach { key ->
                fileIdMap.remove(key)
                fileIdToUploadResultMap.remove(key)
                fileIdToPartETagMap.remove(key)
            }
            logger.info("当前缓存数量:fileIdMap:${fileIdMap.size},fileIdToUploadResultMap:${fileIdToUploadResultMap.size},fileIdToPartETagMap:${fileIdToPartETagMap.size}")
        }, 1L, 1L, TimeUnit.MINUTES)
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


    val startMulitUploadToS3 = Handler<Message<JsonObject>> { msg ->
        val body = msg.body()
        val fileName = body.getString("fileName")
        val blobCount = body.getInteger("blobCount")
        val contentSize = body.getLong("contentSize")
        var contentType = body.getString("contentType", "")
        if (contentType.isNullOrBlank()) {
            val path = Paths.get(fileName)
            contentType = Files.probeContentType(path)

        }

        //生成唯一文件名 格式为  随机串-${bucketName}-s3
        val fileId = ObjectId.get().toString() + "-" + fileName.substringBeforeLast(".") + "-im+test-s3." + fileName.substringAfterLast(".")

        //缓存当前文件基本信息
        val meatData = JsonObject()
        meatData.put("fileName", fileName)
        meatData.put("blobCount", blobCount)
        meatData.put("contentSize", contentSize)
        meatData.put("createTime", System.nanoTime())
        meatData.put("contentType", contentType)
        fileIdMap.put(fileId, meatData)
        fileIdToPartETagMap.put(fileId, mutableListOf())


        val userMetadata = mutableMapOf<String, String>()
        userMetadata["contentType"] = contentType
        userMetadata["OriginalName"] = URLEncoder.encode(fileName, "UTF-8")
        //初始化S3分块上传逻辑
        val tempResult = s3Service.initiateMultipartUpload(bucketName, fileId, userMetadata)
        tempResult?.let {
            logger.info("开始上传文件:$fileId,fileName:$fileName")
            val byteBuffer = ByteArrayBuffer(1024 * 1024 * 5)
            fileIdToUploadResultMap.put(fileId, Pair(it.uploadId, byteBuffer))
        }

        //TODO 如何记录该图片是由哪个用户发送给哪个客服的？
        // 图片数据库应该只存储图片信息，至于图片是由哪个用户发给哪个用户的，应该由聊天记录逻辑中处理

//        数据库存储的时候，如果是图片则需要同时存储源文件名称和缩略图文件名称，此时缩略图是不存在的，只有在首次下载的时候才
//        生成缩略图

        //回复客户端 文件Id(新文件名)
        val data = JsonObject()
        data.put("fileId", fileId)
        data.put("thumb", "thumbnail-$fileId")
        data.put("_id", "数据库_id")

        val result = getResult(data, 0)
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

            logger.debug("fileName:$fileName,blobCount:$blobCount,currentBlogCount:$currentBlogNum")
            val byteArray = dataBody.toByteArray(Charsets.ISO_8859_1)
            initResult?.let {
                logger.debug("=============共 $blobCount 块数据，开始写入第$currentBlogNum 块数据=============")
                val uploadId = it.first
                val byteBuffer = it.second

                val bufferSize = byteBuffer.length()
                val dataSize = byteArray.size
                var remain = 0;
                val cpat = byteBuffer.capacity()
                if (bufferSize + dataSize > cpat) {  // 判断缓存是否装满，如果装满了就发送，没装满不发送。S3分段上传的时候貌似只支持1024的整数倍
                    remain = cpat - bufferSize //本次接收到但是未发送的数据
                    byteBuffer.append(byteArray, 0, remain)
                    println("$bufferSize-- $dataSize--$remain")
                } else {
                    // 由于后端做buffer缓存前段数据，如果前段并发不按照顺序发送数据，则后端就需要缓存所有的数据，
                    // 等待前段全部发送成功之后再
                    //提交S3,这样就等同于前端先上传文件到后端，后端再统一发送，这样就不是实时发送数据到S3。
                    byteBuffer.append(byteArray, 0, byteArray.size)
                }

                val list = fileIdToPartETagMap[fileId] ?: mutableListOf()
                var isSend = false
                val sendedCount = list.size
                if (remain > 0) { // 本次可以发送且没有发送完
                    val partResult = sendToS3(byteBuffer, fileId, uploadId, sendedCount + 1)
                    partResult?.let { res ->
                        list.add(res.partETag)
                    }

                    byteBuffer.clear()
                    byteBuffer.append(byteArray, remain, (byteArray.size - remain)) // 把未发送的数据存储下来
                    isSend = true
                }



                logger.debug("=============第$currentBlogNum 块数据写入结束=============")
                if (currentBlogNum == blobCount.toString()) {
                    if (!isSend) {
                        val partResult = sendToS3(byteBuffer, fileId, uploadId, sendedCount + 1)
                        partResult?.let { res ->
                            list.add(res.partETag)
                        }
                        fileIdToPartETagMap.put(fileId, list)
                        byteBuffer.clear()
                    }

                    logger.debug("最后一块数据写入完成，文件传输结束，关闭数据流")

                    //结束S3上传逻辑
                    val complets = s3Service.completeMultipartUpload(bucketName, fileId, uploadId, list)
                    if (complets != null) {
                        //TODO 数据库中保存该文件记录


                        logger.info("文件上传结束!")
                    } else {
                        logger.error("文件上传S3 结束失败")
                    }
                    byteBuffer.clear()
                    fileIdToPartETagMap.remove(fileId)
                    fileIdToUploadResultMap.remove(fileId)
                    fileIdMap.remove(fileId)
                }
            }
        }

        val data = JsonObject()
        val result = getResult(data, 0, "第$currentBlogNum 块文件上传成功")
        msg.reply(result)
    }

    private fun sendToS3(byteBuffer: ByteArrayBuffer, fileId: String, uploadId: String, sendCount: Int): UploadPartResult? {
        val sendByteArray = byteBuffer.toByteArray() ?: ByteArray(0)
        logger.info("sendToS3------>$sendCount--->${sendByteArray.size}")
        return s3Service.uploadPart(bucketName, fileId,
                uploadId,
                sendCount,
                ByteArrayInputStream(sendByteArray),
                sendByteArray.size.toLong())
    }

}
