package com.perkins.eventbus.handles

import com.amazonaws.services.s3.model.PartETag
import com.amazonaws.services.s3.model.UploadPartResult
import com.perkins.awss3.S3Service
import com.perkins.common.PropertiesUtil
import com.perkins.handlers.AbstractHandle
import com.perkins.handlers.MongodbHandle
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.json.json
import org.apache.http.util.ByteArrayBuffer
import org.apache.tika.mime.MimeType
import org.apache.tika.mime.MimeTypes
import org.bson.types.ObjectId
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HandleWithBuffer(vertx: Vertx) : AbstractHandle() {

    val flieColllection = "upload-files"
    val timeOut = 1000L * 60
    val executor = vertx.createSharedWorkerExecutor("myWorker")
    val fs = vertx.fileSystem()

    //该缓存在上传完成之后和上传失败之后需要进行清除
    private val fileIdMap = ConcurrentHashMap<String, JsonObject>()
    private val fileIdToUploadResultMap = ConcurrentHashMap<String, Pair<String, ByteArrayBuffer>>()
    private val fileIdToPartETagMap = ConcurrentHashMap<String, MutableList<PartETag>>()

    val accessKey = PropertiesUtil.get("accessKey")
    val secretKey = PropertiesUtil.get("secretKey")
    val endpoint = PropertiesUtil.get("endpoint")
    val bucketName = PropertiesUtil.get("bucketName")
    val s3Service = S3Service(accessKey, secretKey, endpoint)

    val imagePrefix = "thumbnail"


    // 图片数据库应该只存储图片信息，至于图片是由哪个用户发给哪个用户的，应该由聊天记录逻辑中处理
//        数据库存储的时候，如果是图片则需要同时存储源文件名称和缩略图文件名称，此时缩略图是不存在的，只有在首次下载的时候才
//        生成缩略图

    val startMulitUploadToS3 = Handler<Message<JsonObject>> { msg ->
        val body = msg.body()
        val fileName = body.getString("fileName")
        val blobCount = body.getInteger("blobCount")
        val contentSize = body.getLong("contentSize")
        var contentType = body.getString("contentType", "")
        if (contentType.isNullOrBlank()) {
            val path = Paths.get(fileName)
            contentType = Files.probeContentType(path)
            if (contentType.isNullOrBlank()) {
                contentType = "application/octet-stream"
            }
        }

        //生成唯一文件名 格式为  随机串-${bucketName}-s3 。上传到S3时以该Id命名
        val fileId = ObjectId.get().toString() + "-" + fileName.substringBeforeLast(".") + "-im+test-s3." + fileName.substringAfterLast(".")

        val userMetadata = mutableMapOf<String, String>()
        userMetadata["contentType"] = contentType
        userMetadata["OriginalName"] = URLEncoder.encode(fileName, "UTF-8")
        //初始化S3分块上传逻辑
        val tempResult = s3Service.initiateMultipartUpload(bucketName, fileId, userMetadata)

        var result: JsonObject

        if (tempResult == null) {
            result = getResult(JsonObject(), 1)
        } else {
            val uploadId = tempResult.uploadId
            val byteBuffer = ByteArrayBuffer(1024 * 1024 * 1)
            fileIdToUploadResultMap.put(fileId, Pair(uploadId, byteBuffer))

            //缓存当前文件基本信息
            val _id = ObjectId.get().toHexString()
            val meatData = JsonObject()
            meatData.put("fileName", fileName)
            meatData.put("blobCount", blobCount)
            meatData.put("contentSize", contentSize)
            meatData.put("createTime", System.nanoTime())
            meatData.put("contentType", contentType)
            meatData.put("_id", _id) // 保存数据库的_id
            fileIdMap.put(fileId, meatData)
            fileIdToPartETagMap.put(fileId, mutableListOf())

            // 文件信息存储数据库
            val document = JsonObject()
                    .put("_id", _id)
                    .put("fileName", fileId)
                    .put("contentType", contentType)
                    .put("originName", fileName)
                    .put("path", fileId)
                    .put("suffix", fileName.substringAfterLast("."))
                    .put("created_at", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                    .put("created_by", 12) // 应该是记录所有者Id
            if (contentType.startsWith("image/")) {
                document.put("thumb", "$imagePrefix-$fileId")
            }

            client.save(flieColllection, document) { res ->
                if (res.succeeded()) {
                    val id = res.result()

                    logger.info("Saved $flieColllection with id $id")
                } else {
                    logger.error("存储数据库文件失败", res.cause())
                }
            }

            // 一分钟之后如果没有处理结束则终止上传任务
            vertx.setTimer(timeOut) {
                logger.info("开始处理清理任务[$fileId]")
                fileIdToUploadResultMap[fileId]?.let {
                    val uploadId = it.first
                    cleanFailData(fileId, uploadId, _id)
                }
                fileIdMap.remove(fileId)
                fileIdToUploadResultMap.remove(fileId)
                fileIdToPartETagMap.remove(fileId)
                logger.debug("当前缓存数量:fileIdMap:${fileIdMap.size},fileIdToUploadResultMap:${fileIdToUploadResultMap.size},fileIdToPartETagMap:${fileIdToPartETagMap.size}")
            }

            //回复client 文件Id(新文件名)
            val data = JsonObject()
            data.put("fileId", fileId)
            data.put("thumb", "thumbnail-$fileId")
            result = getResult(data, 0)
        }

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
            val blobCount = fileMetaData.getInteger("blobCount")

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
                        logger.info("文件上传结束!")
                    } else {
                        val _id = fileMetaData.getString("_id")
                        cleanFailData(fileId, uploadId, _id)
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

    //分块上传失败，终止分块上传任务，清除数据库数据
    private fun cleanFailData(fileId: String, uploadId: String, _id: String) {
        try {//终止分块上传逻辑
            s3Service.abortMultipartUpload(bucketName, fileId, uploadId)
        } catch (e: Exception) {
            logger.error("终止分块上传任务失败,key:$fileId,uploadId:$uploadId", e)
        }
        //删除数据库记录
        val query = JsonObject().put("_id", _id)
        client.findOneAndDelete(flieColllection, query, MongodbHandle.baseHandle("S3文件上传失败，删除数据库记录[$_id]"))
    }

    //发送分块数据到S3
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
