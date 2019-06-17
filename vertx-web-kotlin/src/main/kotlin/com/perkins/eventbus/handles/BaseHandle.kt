package com.perkins.eventbus.handles


import com.amazonaws.services.s3.model.S3Object
import com.perkins.awss3.S3Service
import com.perkins.common.PropertiesUtil
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import net.coobird.thumbnailator.Thumbnails
//import io.vertx.rxjava.core.buffer.Buffer
//import io.vertx.rxjava.core.http.HttpServerResponse
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.devicefarm.model.Run
import software.amazon.ion.system.IonTextWriterBuilder.json
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URLDecoder
import java.net.URLEncoder

class BaseHandle(vertx: Vertx) {
    val fs = vertx.fileSystem()
val workerExecutor = vertx.createSharedWorkerExecutor("workExecutor")
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
        val fileName = request.getParam("fileName")
        val file = s3Service.getObject(bucketName, fileName)

        workerExecutor.executeBlocking<Void>({
            if (file != null) {
                sendS3Object(response, null, null, file)
                response.end()
            } else {
                if (fileName.startsWith(imagePrefix)) {
                    //判断请求的是否是缩略图，如果是缩略图，则判断是否存在，
                    // 如果不存在，则生成缩略图，返回给client，同时上传到S3
                    //TODO 下载原图片，生成缩略图、上传缩略图到S3同时返回缩略图到客户端
                    val sourceFileName = fileName.substringAfter("$imagePrefix-")
                    val sourceFile = s3Service.getObject(bucketName, sourceFileName)
                    if (sourceFile != null) {

                        val metadata = sourceFile.objectMetadata
                        // 注：s3 会默认把userMetadata中的key全换转换为小写，这里取值的时候全部按照小写取值
                        val metaDataOriginName = metadata.userMetadata.getOrDefault("originalname", "data")
                        val metaDataContentType = metadata.userMetadata.getOrDefault("contenttype", "application/octet-stream")
                        val fileSuffix = metaDataOriginName.substringAfterLast(".", "png")

                        //TODO 注意这里的路径 ,尝试怎么找到vertx的默认路径
                        val tempFilePath = projectPath + File.separator + "uploads/$sourceFileName"
                        val tempTargetFilePath = projectPath + File.separator + "uploads/$fileName"

                        saveFileWithBlocking(sourceFile.objectContent, tempFilePath)

                        thumbnailsImage(tempFilePath, tempTargetFilePath)
                        //开线程上传缩略图
                        Thread(Runnable {
                            val fileKey = "$fileName"
                            val userMetadata = mutableMapOf<String, String>()
                            userMetadata["contentType"] = metaDataContentType
                            userMetadata["source"] = URLEncoder.encode("sourceFileName","UTF-8")
                            userMetadata["OriginalName"] = metaDataOriginName
                            val imagePutObjectResult = s3Service.addObject(bucketName, fileKey, tempTargetFilePath, userMetadata)
                            if (imagePutObjectResult == null) {
                                logger.error("upload ThumbnailsImage to  S3 error")
                            }else{
                                logger.info("缩略图上传S3成功")
                            }
                        }).start()
                        // 发送剪切图片
                        sendFileData(response, metaDataContentType, metaDataOriginName, FileInputStream(tempTargetFilePath))
                        response.end()


                        //TODO 调研如何使用vertx的文件写入实现异步操作，如何等待文件全部写完成之后调用end方法？
                        /*fs.open(tempFilePath, OpenOptions()) {
                            if (it.succeeded()) {
                                val asyncFile = it.result()
                                val buffer = Buffer.buffer()
                                val arraySize = 1024 * 1024 * 5
                                val array = ByteArray(arraySize)
                                //TODO 上传图片的时候需要把文件的真是名称设置进去，然后下载的时候再返回给前端
                                //TODO 如何记录该图片是由哪个用户发送给哪个客服的？
                                val dataBuffer = sourceFile.objectContent.buffered()
                                var position = 0

                                //ZPJ 这个endHandler 如何触发？是需要程序主动end来触发吗？
                                asyncFile.endHandler {
                                    logger.info("---文件下载结束----")
                                    println("---文件下载结束----")
                                     asyncFile.close()
                                      //生成剪切图片
                                      thumbnailsImage(tempFilePath,tempTaegetFilePath)
                                      //TODO 开线程上传缩略图
                                      // 发送剪切图片
                                      sendFileData(response, metaDataContentType, metaDataOriginName, FileInputStream(tempTaegetFilePath))
                                      response.end()
                                }
                                println("==== 开始写文件数据")
                                while (dataBuffer.read(array) > -1) {
                                    asyncFile.write(Buffer.buffer(array), (position * arraySize).toLong()) {
                                        if (it.succeeded()) {
                                            logger.info("写第$position 块数据成功")
                                            println("写第$position 块数据成功")
                                            asyncFile.flush()
    //                                        asyncFile.end()
                                        } else {
                                            it.cause().printStackTrace()
                                        }
                                    }
                                    println(asyncFile.hashCode())

                                    position += 1
                                }
                                println("==== 写文件数据结束")
                            } else {
                                logger.error("error")
                            }
                        }*/

                    } else {
                        logger.error("----源文件丢失！！")
                        response.end("can not found file in the S3")
                    }
                } else {
                    logger.error("can not found file($fileName) in the S3")
                    response.end("can not found file in the S3")
                }
            }
        },{})
    }

    private fun thumbnailsImage(tempFilePath: String, tempTaegetFilePath: String?) {
        val uploadThumbWidth = 108
        val uploadThumbHeight = 108
        Thumbnails.of(tempFilePath).size(uploadThumbWidth, uploadThumbHeight).toFile(File(tempTaegetFilePath))
        println("生成缩略图完成")
    }


    private fun sendS3Object(response: HttpServerResponse, contentType: String?, fileName: String?, s3Object: S3Object) {
        val metadata = s3Object.objectMetadata
        // 注：s3 会默认把userMetadata中的key全换转换为小写，这里取值的时候全部按照小写取值
        val metaDataOriginName = metadata.userMetadata.getOrDefault("originalname", "data")
        val metaDataContentType = metadata.userMetadata.getOrDefault("contenttype", "application/octet-stream")

        val returnFileName = fileName ?: metaDataOriginName
        val returnContentType = contentType ?: metaDataContentType
        val inputStream = s3Object.objectContent
        sendFileData(response, returnContentType, returnFileName, inputStream)
    }

    private fun sendFileData(response: HttpServerResponse, contentType: String?, originName: String?, inputStream: InputStream) {
        logger.debug("returnContentType:$contentType,returnFileName :$originName")
        response.putHeader("Pargam", "no-cache")
                .putHeader("Cache-Control", "no-cache")
                .putHeader("Content-Type", contentType)
                .putHeader("Content-Disposition", "attachment;filename=$originName")
        response.isChunked = true
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

    fun saveFileWithBlocking(inputStream: InputStream, targetFilePath: String) {
        val targetFile = createNewFile(targetFilePath)
        val bufSize = 1024 * 1024 * 5
        val buffer = ByteArray(bufSize);
        try {
            var c = -1
            while ({ c = inputStream.read(buffer);c }() > -1) {
                targetFile.appendBytes(buffer.copyOfRange(0, c))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("文件写入结束")
            inputStream.close()
        }
    }

    private fun createNewFile(sourceFilePath: String): File {
        val sourceFile = File(sourceFilePath)
        if (sourceFile.exists()) {
            sourceFile.delete()
        }
        sourceFile.createNewFile()
        return sourceFile
    }
}

/*
fun io.vertx.rxjava.core.http.HttpServerResponse.endFailure(message: String) {
    this.setStatusCode(statusCode).putHeader("Content-Type", "application/json")
            .end(json { obj("success" to false, "message" to message, "code" to statusCode) }.encode())
}*/
