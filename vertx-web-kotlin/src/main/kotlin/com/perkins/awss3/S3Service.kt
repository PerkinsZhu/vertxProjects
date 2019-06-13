package com.perkins.awss3

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.*
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer

class S3Service constructor(accessKey: String, secretKey: String, endpoint: String) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    }

    private var amazonS3: AmazonS3

    init {
        if (accessKey.isNullOrBlank() || secretKey.isNullOrBlank() || endpoint.isNullOrBlank()) {
            logger.error("S3 config missing")
            throw  RuntimeException("S3 config missing")
        } else {
            val sessionCredentials = BasicSessionCredentials(accessKey, secretKey, "")
            amazonS3 = AmazonS3ClientBuilder.standard()
                    .withPathStyleAccessEnabled(true)
                    .disableChunkedEncoding()
                    .withCredentials(AWSStaticCredentialsProvider(sessionCredentials))
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, Regions.CN_NORTH_1.name))
                    .build()
        }
    }

    /**
     * 查询bucketName下的所有object
     */
    fun listFiles(bucketName: String): JsonArray {
        val jsonArray = JsonArray()
        try {
            val objectListing = amazonS3.listObjects(ListObjectsRequest().withBucketName(bucketName))
            objectListing.objectSummaries.map { objectSummary ->
                val item = JsonObject()
                item.put("key", objectSummary.key)
                item.put("bucketName", objectSummary.bucketName)
                item.put("size", objectSummary.size)
                item.put("lastModified", objectSummary.lastModified.toString())
                jsonArray.add(item)
            }
        } catch (ex: Exception) {
            logger.error("query  file list fail", ex)
        }
        return jsonArray
    }

    /**
     * 查询所有的Bucket
     */
    fun getBucketList(): JsonArray {
        val jsonArray = JsonArray()
        try {
            amazonS3.listBuckets().map { bucket ->
                val item = JsonObject()
                item.put("name", bucket.name)
                item.put("creationDate", bucket.creationDate.toString())
                item.put("owner", bucket.owner.displayName)
                jsonArray.add(item)
            }
        } catch (ex: Exception) {
            logger.error("query  bucketList fail", ex)
        }
        return jsonArray
    }

    /**
     * 在指定的bucketName下面添加object
     * 上传失败返回null
     */
    fun addObject(bucketName: String, key: String, filePath: String, userMetadata: Map<String, String>? = null): PutObjectResult? {
        logger.info("addObject to S3, bucketName:$bucketName,fileKey:$key,filePath:$filePath")
        return try {
            val putObjectRequest = PutObjectRequest(bucketName, key, File(filePath))
            userMetadata?.let {
                val metadata = ObjectMetadata()
                metadata.userMetadata = it
                putObjectRequest.withMetadata(metadata)
            }
            amazonS3.putObject(putObjectRequest)
        } catch (e: Exception) {
            logger.error("upload file[$key] to S3 fail!", e)
            null
        }
    }

    /**
     * 删除object
     * 返回状态无意义
     */
    fun deleteObject(bucketName: String, key: String): Boolean {
        amazonS3.deleteObject(bucketName, key)
        return true
    }

    /**
     * 获取bucket下面指定的s3Object
     */
    fun getObject(bucketName: String, key: String): S3Object? {
        logger.debug("getObject ($bucketName.$key) in S3")
        return try {
            val request = GetObjectRequest(bucketName, key)
            amazonS3.getObject(request)
        } catch (e: Exception) {
            logger.error("get S3Object error", e)
            null
        }
    }

    fun initiateMultipartUpload(bucketName: String, key: String): InitiateMultipartUploadResult? {
        // 这里支持metadata
        val request = InitiateMultipartUploadRequest(bucketName, key)
        return amazonS3.initiateMultipartUpload(request)
    }

    fun uploadPart(bucketName: String, key: String, uploadId: String, partNum: Int, inputStream: InputStream, partSize: Long): UploadPartResult? {
        logger.info("第$partNum,大小:$partSize")

        val uploadPartRequest = UploadPartRequest()
                .withUploadId(uploadId)
                .withPartNumber(partNum)
                .withBucketName(bucketName)
                .withKey(key)
                .withInputStream(inputStream)
                .withPartSize(partSize)
        return amazonS3.uploadPart(uploadPartRequest)
    }

    fun uploadPart(bucketName: String, key: String, uploadId: String, partNum: Int, file: File, partSize: Long): UploadPartResult? {
        val uploadPartRequest = UploadPartRequest()
                .withUploadId(uploadId)
                .withPartNumber(partNum)
                .withBucketName(bucketName)
                .withKey(key)
                .withPartSize(partSize)
                .withFile(file)
                .withFileOffset(partSize)
        return amazonS3.uploadPart(uploadPartRequest)
    }


    fun completeMultipartUpload(bucketName: String, key: String, uploadId: String, eTagList: List<PartETag>): CompleteMultipartUploadResult? {
        val complete = CompleteMultipartUploadRequest()
                .withUploadId(uploadId)
                .withBucketName(bucketName)
                .withKey(key)
                .withPartETags(eTagList) //注意，完成的时候必须要带eTagList
        return amazonS3.completeMultipartUpload(complete)
    }

    /**
     * 分块上传文件到S3
     */
    fun mulitUpload(bucketName: String, key: String, filePath: String) {
        val initResult = initiateMultipartUpload(bucketName, key)
        if (initResult != null) {
            val uploadId = initResult.uploadId
            try {
                val list = mulitUploadWithInputStream(filePath,bucketName, key, uploadId)

//                val list = mulitUploadWithFile(filePath, bucketName, key, uploadId)
                val completeMultipartUploadResult = completeMultipartUpload(bucketName, key, uploadId, list)
                if (completeMultipartUploadResult != null) {
                    logger.info("文件分块上传完成")
                } else {
                    logger.info("文件分块上传失败")
                }
            } catch (e: Exception) {
                logger.error("分库上传文件失败，终止上传！", e)
                amazonS3.abortMultipartUpload(AbortMultipartUploadRequest(bucketName, key, uploadId))
            }
        } else {
            logger.error("初始化分块上传文件失败")
        }
    }

    fun abortMultipartUpload(bucketName: String, key: String, uploadId: String) {
        amazonS3.abortMultipartUpload(AbortMultipartUploadRequest(bucketName, key, uploadId))
    }

    private fun mulitUploadWithInputStream(filePath: String, bucketName: String, key: String, uploadId: String): List<PartETag> {
        val inputStream = FileInputStream(File(filePath))
//        val buffer = ByteBuffer.allocate(1024 * 1024 * 5)
        //https://docs.amazonaws.cn/AmazonS3/latest/dev/qfacts.html
        // 分块上传，官方文档限制每段大小在5M以上，最后一段可以小于5M，实际测试在1M以上都是可以的，但是小于1M就会失败
        val buffer = ByteBuffer.allocate(1024 * 1024)
        val fileChannel = inputStream.channel
        var partNum = 1;
        val list = mutableListOf<PartETag>()
        while (fileChannel.read(buffer) != -1) {
            buffer.flip()
            val uploadPartResult = uploadPart(bucketName, key, uploadId
                    , partNum
                    , ByteArrayInputStream(buffer.array())
                    , buffer.limit().toLong())

            if (uploadPartResult == null) {
                logger.error("第$partNum 块文件上传失败")
            } else {
                uploadPartResult?.let {
                    list.add(it.partETag)
                }
                logger.error("第$partNum 块文件上传成功")
            }
            partNum += 1
            buffer.clear()
        }
        inputStream.close()
        return list;
    }

    /**
     * 从磁盘中读取整个文件进行分块上传
     */
    private fun mulitUploadWithFile(filePath: String, bucketName: String, key: String, uploadId: String): MutableList<PartETag> {
        val file = File(filePath)
        val fileSize = file.length()
        var blobSize = 1024 * 1024 * 5L
        var filePosition = 0L
        var partNum = 1;
        val partETags = mutableListOf<PartETag>()

        while (filePosition < fileSize) {
            blobSize = Math.min(blobSize, fileSize - filePosition)
            val uploadRequest = UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(key)
                    .withUploadId(uploadId)
                    .withPartNumber(partNum)
                    .withFileOffset(filePosition)
                    .withFile(file)
                    .withPartSize(blobSize);

            val uploadPartResult = amazonS3.uploadPart(uploadRequest)
            if (uploadPartResult != null) {
                logger.info("第$partNum 块数据上传成功")
                partETags.add(uploadPartResult.partETag)
            } else {
                logger.info("第$partNum 块数据上传成功")
                throw  java.lang.RuntimeException("第$partNum 块数据上传成功")
            }

            partNum += 1
            filePosition += blobSize
        }
        return partETags
    }


    /**
     * 调用java客户端测试分块上传
     */
    fun testJavaUpload(bucketName: String, key: String, filePath: String) {
        UploadObjectMPULowLevelAPI.testUploadFile(amazonS3, key, bucketName, filePath)
    }

    // 调用java客户端测试
    fun testJavaListMultipartUploads(bucketName: String) {
        UploadObjectMPULowLevelAPI.listMultipartUploads(amazonS3,bucketName)
    }

}