package com.perkins.vertxs

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.WorkerExecutor
import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import com.sun.corba.se.spi.presentation.rmi.StubAdapter.request
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*


class RouteVerticle : AbstractVerticle() {
    lateinit var ioPool: WorkerExecutor // 先定义变量，延时初始化
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun start(startFuture: Future<Void>?) {

        ioPool = vertx.createSharedWorkerExecutor("my-worker-pool", 10);
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        // 取消默认的handle，默认的handle会自动把文件存储在磁盘中，一旦读取上传的文件流后，后面就无法再取出数据了
        // 该handle 默认对所有的请求生效
        //router.route().handler(BodyHandler.create())
        router.get("/hello").handler {
            it.response().end("hello")
        }

        router.post("/form").handler { ctx ->
            ctx.response().putHeader("Content-Type", "text/plain")

            ctx.response().isChunked = true

            for (f in ctx.fileUploads()) {
                println(f.name())
                println(f.contentType())
                println(f.contentTransferEncoding())
                println(f.uploadedFileName())
                ctx.response().write("Filename: " + f.fileName())
                ctx.response().write("\n")
                ctx.response().write("Size: " + f.size())
            }
            ctx.response().end()
        }


        // 无法读取出chunk数据
//        router.post("/form2").handler(form2Handle())
        router.post("/form2").handler {
            val request = it.request()
            request.setExpectMultipart(true);
            request.uploadHandler { upload ->
                upload.handler { chunk ->
                    // 每个chunk在8192byte ,大概8K
                    println("size -->" + chunk.length())
                    //TODO 如何直接从数据流中读取数据，而不用写入磁盘？
                    //TODO 如何实现分块上传？
                    /**
                     * 1、先从磁盘中读取文件分块上传到S3
                     * 再删除磁盘文件
                     *
                     * 2、 做个缓存，当接受的数据量大于5M的时候提交一次
                     */
//                    multipartUpload("buckName","key","path")
//                    println(chunk.toString("UTF-8"))
                }
                println("uploadSize--->" + upload.size())

            }
            it.response().end("save over")
        }


        router.get("/download").handler {
            logger.info("接受到下载文件请求")
            //从本地磁盘读取文件返回
            it.response().sendFile("uploads/0644d274-c987-499b-8286-8d72105b15e1")
        }


        router.get("/downloadFromUrl").handler {
            logger.info("接受到下载文件请求")
            //从网络中读取文件
            it.response().sendFile("uploads/0644d274-c987-499b-8286-8d72105b15e1")
        }

        router.get("/downloadFromStream").handler { request ->
            logger.info("接受到下载文件请求")
            //从数据流中下载文件
            val fs = vertx.fileSystem()
            val buffer = fs.readFileBlocking("uploads/0644d274-c987-499b-8286-8d72105b15e1")

            val request = request.request()
            val response = request.response()
            response.setChunked(true)
            response.write(buffer)
            response.end()
//            request.response().sendFile("uploads/0644d274-c987-499b-8286-8d72105b15e1")
        }

        // 超出2G的文件下载无法终止
        router.get("/downloadFromBuffer").handler { context ->
            logger.info("接受到下载文件请求")
            val id = context.queryParams().get("id")
            println("id=$id")
            val file = FileInputStream(File("file-uploads/${id}"))
            val channel = file.channel
            val byteBuffer = ByteBuffer.allocate(1024 * 1024 * 5)
            val response = context.response()
            //必须先设置头信息再发送数据
            response.putHeader("Content-Type", "application/octet-stream;charset=utf-8")
            response.putHeader("Content-Disposition", "attachment;filename=data.mp4")
            response.setChunked(true)
            while (channel.read(byteBuffer) != -1) {
                println("正在写数据.....")
                byteBuffer.flip()
                response.write(Buffer.buffer(byteBuffer.slice().array()))
                byteBuffer.clear()
            }
            response.end()
        }
        //使用work pool实现
        router.get("/downloadFromBuffer2").handler(ioHandler)


        server.requestHandler(router::accept).listen(8080)
    }


    /**
     *
     * TODO 当并发数较多的时候会发生异常
     *
     * java.lang.IllegalStateException: Response is closed
    at io.vertx.core.http.impl.HttpServerResponseImpl.checkValid(HttpServerResponseImpl.java:547)
    at io.vertx.core.http.impl.HttpServerResponseImpl.write(HttpServerResponseImpl.java:581)
    at io.vertx.core.http.impl.HttpServerResponseImpl.write(HttpServerResponseImpl.java:286)
    at io.vertx.core.http.impl.HttpServerResponseImpl.write(HttpServerResponseImpl.java:53)
     */
    val ioHandler = Handler<RoutingContext> { context ->
        logger.info("接受到下载文件请求")
        // 开启线程池处理数据写操作
        ioPool.executeBlocking<RoutingContext>({ future ->
            logger.info("当前处理的请求-->" + context)
            // 调用一些需要耗费显著执行时间返回结果的阻塞式API
            val id = context.queryParams().get("id")
            println("id=$id")
            val file = FileInputStream(File("file-uploads/${id}"))
            val channel = file.channel
            val byteBuffer = ByteBuffer.allocate(1024 * 1024 * 5)
            val response = context.response()
            //必须先设置头信息再发送数据
            response.putHeader("Content-Type", "application/octet-stream;charset=utf-8")
            response.putHeader("Content-Disposition", "attachment;filename=data.mp4")
            response.setChunked(true)
            while (channel.read(byteBuffer) != -1) {
                byteBuffer.flip()
                response.write(Buffer.buffer(byteBuffer.slice().array()))
                byteBuffer.clear()
            }
            // 数据写入 response中之后结束future
            future.complete(context)
        }, { res ->
            logger.info("数据读取结束")
            //future结束之后，判断是否成功，如果执行成功，则关闭response
            if (res.succeeded()) {
                res.result().response().end()
            } else {
                logger.error("读取文件失败：", res.cause())
            }

        })
    }

    lateinit var s3: S3Client

    /**
     * 分块上传文件
     * TODO 在workPool 中执行该操作
     */
    fun multipartUpload(bucketName: String, key: String, path: String) {
        val createMultipartUploadRequest = CreateMultipartUploadRequest.builder().bucket(bucketName).key(key).build()
        val response = s3.createMultipartUpload(createMultipartUploadRequest)
        val uploadId = response.uploadId()

        val file = FileInputStream(File(path))
        val channel = file.channel
        val byteBuffer = ByteBuffer.allocate(1024 * 1024 * 5)
        var i = 1
        var list: MutableList<CompletedPart> = ArrayList()
        while (channel.read(byteBuffer) != -1) {
            byteBuffer.flip()
            val uploadPartRequest1 = UploadPartRequest.builder().bucket(bucketName).key(key).uploadId(uploadId).partNumber(i).build()
            val etag = s3.uploadPart(uploadPartRequest1, RequestBody.fromByteBuffer(byteBuffer)).eTag()
            val part = CompletedPart.builder().partNumber(i).eTag(etag).build()
            list.add(part)
            byteBuffer.clear()
        }

        val completedMultipartUpload = CompletedMultipartUpload.builder().parts(list).build()
        val completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder().bucket(bucketName).key(key).uploadId(uploadId)
                .multipartUpload(completedMultipartUpload).build()
        s3.completeMultipartUpload(completeMultipartUploadRequest)
    }

//     通过workPool执行上传工作
    fun multipartUploadAsync() = Handler<RoutingContext> { context ->
        val bucketName = ""
        val key = ""
        val path = ""
        ioPool.executeBlocking<RoutingContext>({ future ->
            val createMultipartUploadRequest = CreateMultipartUploadRequest.builder().bucket(bucketName).key(key).build()
            val response = s3.createMultipartUpload(createMultipartUploadRequest)
            val uploadId = response.uploadId()

            val file = FileInputStream(File(path))
            val channel = file.channel
            val byteBuffer = ByteBuffer.allocate(1024 * 1024 * 5)
            var i = 1
            var list: MutableList<CompletedPart> = ArrayList()
            while (channel.read(byteBuffer) != -1) {
                byteBuffer.flip()
                val uploadPartRequest1 = UploadPartRequest.builder().bucket(bucketName).key(key).uploadId(uploadId).partNumber(i).build()
                val etag = s3.uploadPart(uploadPartRequest1, RequestBody.fromByteBuffer(byteBuffer)).eTag()
                val part = CompletedPart.builder().partNumber(i).eTag(etag).build()
                list.add(part)
                byteBuffer.clear()
            }
            val completedMultipartUpload = CompletedMultipartUpload.builder().parts(list).build()
            val completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder().bucket(bucketName).key(key).uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload).build()
            s3.completeMultipartUpload(completeMultipartUploadRequest)

            future.complete(context)
        }, { res ->
            logger.info("数据读取结束")
            //future结束之后，判断是否成功，如果执行成功，则关闭response
            if (res.succeeded()) {
                res.result().response().end()
            } else {
                logger.error("读取文件失败：", res.cause())
            }

        })
    }


    fun form2Handle()= Handler<RoutingContext> { context ->
        ioPool.executeBlocking<RoutingContext>({ future ->
            val request = context.request()
            request.setExpectMultipart(true);
            request.uploadHandler { upload ->
                upload.handler { chunk ->
                    // 每个chunk在8192byte ,大概8K
                    println("size -->" + chunk.length())
                    //TODO 如何直接从数据流中读取数据，而不用写入磁盘？
                    //TODO 如何实现分块上传？
                    /**
                     * 先从磁盘中读取文件分块上传到S3
                     * 再删除磁盘文件
                     */
//                    multipartUpload("buckName","key","path")
//                    println(chunk.toString("UTF-8"))
                }
                println("uploadSize--->"+upload.size())

            }
            future.complete(context)
        }, { res ->
            logger.info("数据读取结束")
            //future结束之后，判断是否成功，如果执行成功，则关闭response
            if (res.succeeded()) {
                res.result().response().end("save over")
            } else {
                logger.error("读取文件失败：", res.cause())
            }

        })
    }
}