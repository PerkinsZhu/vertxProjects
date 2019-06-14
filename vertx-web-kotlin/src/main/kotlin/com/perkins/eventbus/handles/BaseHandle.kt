package com.perkins.eventbus.handles

import com.amazonaws.services.s3.model.S3Object
import com.perkins.awss3.S3Service
import com.perkins.common.PropertiesUtil
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.OpenOptions
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
//import io.vertx.rxjava.core.buffer.Buffer
//import io.vertx.rxjava.core.http.HttpServerResponse
import org.slf4j.LoggerFactory

class BaseHandle(vertx: Vertx) {
    val fs = vertx.fileSystem()

    val imagePrefix = "thumbnail"
    val logger = LoggerFactory.getLogger(this.javaClass)
    val accessKey = PropertiesUtil.get("accessKey")
    val secretKey = PropertiesUtil.get("secretKey")
    val endpoint = PropertiesUtil.get("endpoint")
    val bucketName = PropertiesUtil.get("bucketName")
    val s3Service = S3Service(accessKey, secretKey, endpoint)

    val getFile = Handler<RoutingContext> {
        // 返回用户请求的数据文件
        val request = it.request()
        val response = it.response()
        val fileName = request.params().get("fileName")
        val file = s3Service.getObject(bucketName, fileName)
        if (file != null) {
            sendS3Object(response, null, null, file)
            response.end()
        } else {
            if (fileName.startsWith(imagePrefix)) {
                //判断请求的是否是缩略图，如果是缩略图，则判断是否存在，
                // 如果不存在，则生成缩略图，返回给client，同时上传到S3
                //TODO 下载原图片，生成缩略图、上传缩略图到S3同时返回缩略图到客户端
                val sourceFileName = fileName.substringAfter(imagePrefix + "-")
                val sourceFile = s3Service.getObject(bucketName, sourceFileName)
                if (sourceFile != null) {
                    fs.open("uploads/$sourceFileName", OpenOptions()) {
                        if (it.succeeded()) {
                            val asyncFile = it.result()
                            val buffer = Buffer.buffer()
                            val arraySize = 1024 * 1024 * 5
                            val array = ByteArray(arraySize)
                            //TODO 上传图片的时候需要把文件的真是名称设置进去，然后下载的时候再返回给前端
                            //TODO 如何记录该图片是由哪个用户发送给哪个客服的？
                            val dataBuffer = sourceFile.objectContent.buffered()
                            var position = 0
                            while (dataBuffer.read(array) > -1) {
                                asyncFile.write(Buffer.buffer(array), (position * arraySize).toLong()) {
                                    if (it.succeeded()) {
                                        logger.info("写第$position 块数据成功")
                                        asyncFile.flush()
                                    } else {
                                        it.cause().printStackTrace()
                                    }
                                }
                                asyncFile.endHandler {
                                    asyncFile.close()
                                }
                                position += 1
                            }
                        } else {
                            logger.error("error")
                        }
                    }

                    //TODO 注意这里，文件的inputStream只能读取一次
                    sendS3Object(response, null, null, sourceFile)
                    response.end()
                } else {
                    logger.error("----源文件丢失！！")
                }
            } else {
                logger.error("can not found file($fileName) in the S3")
                response.end("can not found file in the S3")
            }
        }
    }


    private fun sendS3Object(response: HttpServerResponse, contentType: String?, fileName: String?, s3Object: S3Object) {
        val metadata = s3Object.objectMetadata
        // 注：s3 会默认把userMetadata中的key全换转换为小写，这里取值的时候全部按照小写取值
        val metaDataOriginName = metadata.userMetadata.getOrDefault("originalname", "data")
        val metaDataContentType = metadata.userMetadata.getOrDefault("contenttype", "application/octet-stream")

        val returnFileName = fileName ?: metaDataOriginName
        val returnContentType = contentType ?: metaDataContentType
        logger.debug("returnContentType:$returnContentType,returnFileName :$returnFileName")
        response.putHeader("Pargam", "no-cache")
                .putHeader("Cache-Control", "no-cache")
                .putHeader("Content-Type", returnContentType)
                .putHeader("Content-Disposition", "attachment;filename=$returnFileName")
        response.isChunked = true
        val inputStream = s3Object.objectContent
        try {
            val buffer = Buffer.buffer(inputStream.readBytes())
            response.write(buffer)
        } catch (ex: Exception) {
            logger.error("write s3Object to response error", ex)
        } finally {
            logger.info("file write end")
            inputStream.close()
        }
    }
}