package com.perkins

import com.amazonaws.services.s3.model.Tag
import com.perkins.awss3.S3Service
import com.perkins.bean.Person
import com.perkins.bean.User
import com.perkins.common.PropertiesUtil
import com.perkins.util.Base64Utils
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.OpenOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.rx.java.RxHelper
import org.apache.commons.codec.digest.DigestUtils
import org.apache.tika.mime.MimeTypes
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.InetAddress
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AppTest {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val vertx = Vertx.vertx()
    val fs = vertx.fileSystem()
    @Test
    fun testInputStream() {
        val file = FileInputStream(File("uploads/0644d274-c987-499b-8286-8d72105b15e1"))

        val fs = vertx.fileSystem()
        val channel = file.channel
        val buffer = Buffer.buffer()
        val byteBuffer = ByteBuffer.allocate(1024)
        while (channel.read(byteBuffer) != -1) {
            byteBuffer.flip()
            buffer.appendByte(byteBuffer.get())
            byteBuffer.clear()
        }
        fs.writeFile("uploads/new", buffer, { rs ->
            if (rs.succeeded()) {
                print("===ok===")
            }
        })
    }


    @Test
    fun testWriteFile() {
        val vertx = Vertx.vertx()
        val fs = vertx.fileSystem()
        val buffer = Buffer.buffer()
        var i = 0
        while (i < 1000) {
            buffer.appendString("你叫什么名字\r\n", "UTF-8")
            i++
        }

        fs.writeFile("uploads/data.txt", buffer, { rs ->
            if (rs.succeeded()) {
                print("===ok===")
            }
        })

    }

    @Test
    fun testRXJava() {
        val vertx = Vertx.vertx()
        val fileSystem = vertx.fileSystem()
        fileSystem.open("uploads/data.txt", OpenOptions()) { result ->
            println("--handle--")
            val file = result.result()
            val observable = RxHelper.toObservable(file)
            observable.forEach { data ->
                println("--observer--")
                System.out.println("Read data: " + data.toString("UTF-8"))
            }
        }
    }

    @Test
    fun testString() {
        val str: String = "   "
        val str1 = "123"
        println(str.isBlank())
        println(str.isEmpty())
        println(str.isNullOrBlank())
        println(str.isNullOrEmpty())
        println(str.isNotBlank())
        val ex = RuntimeException("123456")
        println(ex.message)

        val str3 = "123123.sdfas"
        println(str3.substringBeforeLast("."))
        println(str3.substringAfterLast("."))

    }


    @Test
    fun testLetAndOther() {
        //TODO  测试 run、with、let、also和apply 的用法 （作用域函数）
        var name = "123456"
        val temp = run {
            var name = "jkkjljk"
            println("----->" + name)
            if (false) {
                "nnn" //自动返回结果
            } else {
                100
            }
//            100
        }
        println(temp)
        println("----->$name")

        val obj = "Obj"
        obj.run {
            println(this)
        }
        with(obj) {
            println(this)
        }
        obj.let {
            println(it)
            println(this)
        }
    }

    @Test
    fun testLet() {
        val str: String? = null
        str?.let {
            println(it)
        }
        val map = mutableMapOf<String, String>()
        map.put("aaa", "bbbb")
        map.put("cccc", "dddd")
        println(map)

        val temp = str ?: "aaa"
        println(temp)

    }

    @Test
    fun testCreatFile() {
        val vertx = Vertx.vertx()
        val fs = vertx.fileSystem()
        fs.open("uploads/ssswww.txt", OpenOptions()) {
            if (it.succeeded()) {
                print(it.result())
            } else {
                it.cause().printStackTrace()
            }
        }
    }


    @Test
    fun testWriteFileWithAppend() {
        val vertx = Vertx.vertx()
        val fs = vertx.fileSystem()
        val filePath = "uploads/hello.txt"
        val option = OpenOptions()
        option.isAppend = true
        //TODO 如何追加文件？
        fs.open(filePath, option) { res ->
            if (res.succeeded()) {
                fs.writeFile(filePath, Buffer.buffer("Hello")) { result ->
                    println(result)
                    if (result.succeeded()) {
                        println("File written")
                    } else {
                        System.err.println("Oh oh ..." + result.cause())
                    }
                }
            } else {
                res.cause().printStackTrace()
            }
        }
        Thread.sleep(100000)
    }


    @Test
    fun getProjectPath() {
        System.out.println(System.getProperty("user.dir"))
    }

    @Test
    fun testFileBase64() {
        val data = Base64Utils.encodeFile(File("D:\\zhupingjing\\testFile\\Unicode编码表.png"))
//        println(data)
        val result = Base64Utils.decode(data)
        println(result.size)
//        println(java.lang.String(result, "UTF-8"))
//        Base64Utils.decodeFile(data.toString(), File("D:\\zhupingjing\\testFile\\Unicode编码表new.png"))
    }

    @Test
    fun writeFileByThread() {
        val executor = vertx.createSharedWorkerExecutor("write file")
//        val byteArray = FileInputStream(File("D:\\zhupingjing\\testFile\\Unicode编码表.png"))
        val list = mutableListOf<String>()
        for (i in 0..9) {
            list.add("hello 你好 - $i")
        }

        val filePath = "D:\\zhupingjing\\testFile\\datatest.txt"
        val openOptions = OpenOptions()
//        openOptions.isAppend = true

        //TODO  测试线程开在哪里？ 是仅仅并发处理write操作就可以吗？
        fs.open(filePath, openOptions) {
            if (it.succeeded()) {
                println("打开文件成功")
                var endCount = 0
                val asyncFile = it.result()
                list.forEachIndexed { index, value ->
                    Thread(Runnable {
                        println("处理-- $value")
                        println("java 线程--->" + Thread.currentThread().name)
                        try {
                            if (index % 2 == 0) {
                                Thread.sleep(1000)
                            }
                            val position = (value.toByteArray().size * index)
                            asyncFile.write(Buffer.buffer(value), position.toLong()) {
                                println("file 线程--->" + Thread.currentThread().name)
                                endCount += 1
                                println("---写入结束--$endCount")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }).start()
                }
                println("线程开启结束")
                asyncFile.endHandler {
                    println("关闭文件操作对象")
                    asyncFile.close()
                }

                println("open 线程--->" + Thread.currentThread().name)
                /* while(endCount != 10){
                       这里会把verxt线程阻塞掉，导致所有的handle无法进行处理。，
                   }
                   asyncFile.close()*/
            } else {
                it.cause().printStackTrace()
            }
        }

        Thread.sleep(1000000)
    }


    @Test
    fun testStringNull() {
        val name: String? = "123"
        println((name ?: "asddfgds"))
    }

    @Test
    fun transformBucketName() {
        val realBucketName: String = "kkkl-dev"

//        realBucketName.split("-".toRegex()).forEach{println(it)}

        val result = realBucketName.split("-".toRegex()).map {
            val firstChar = it.first()
            val temp = it.replaceFirst(firstChar, firstChar.toUpperCase())
            println(temp)
            temp
        }.joinToString("")

        println(result)
    }

    @Test
    fun testLocalDateTime() {
        val now = LocalDateTime.now()
        println(now.second)
        println(now.toEpochSecond(ZoneOffset.UTC))
    }

    @Test
    fun testExecutor() {
        val executor = Executors.newScheduledThreadPool(1)

        executor.scheduleAtFixedRate({ println("---") }, 0L, 1L, TimeUnit.SECONDS)

        Thread.sleep(10000)
    }


    @Test
    fun getS3Test() {
        val (bucketName, s3Service) = getS3Server()
        val file = s3Service.getObject(bucketName, "DDDDD.zip")
        if (file != null) {
            println("文件存在")
            println("metaData-->" + file.objectMetadata.contentLength)
        } else {
            println("文件不存在")
        }
    }

    private fun getS3Server(): Pair<String, S3Service> {
        val accessKey = PropertiesUtil.get("accessKey")
        val secretKey = PropertiesUtil.get("secretKey")
        val endpoint = PropertiesUtil.get("endpoint")
        val bucketName = PropertiesUtil.get("bucketName")
        val s3Service = S3Service(accessKey, secretKey, endpoint)
        return Pair(bucketName, s3Service)
    }

    @Test
    fun testAddObjectToS3() {
        val (bucketName, service) = getS3Server()
        val result = service.addObject(bucketName, "testTag0201.png", "D:\\zhupingjing\\testFile\\Unicode编码表.png")
        if (result == null) {
            println("上传失败")
        } else {
            println(result.eTag)
        }
    }

    // 测试文件分段上传
    @Test
    fun testMulitUploadFile() {
        val (bucketName, service) = getS3Server()
        val filePath = "D:\\zhupingjing\\testFile\\sokit-1-3-win32-chs.zip"
        service.mulitUpload(bucketName, "DDDDD.zip", filePath)
    }

    @Test
    fun testMulitUploadFileWithJava() {
        val (bucketName, service) = getS3Server()
        val filePath = "D:\\zhupingjing\\testFile\\Unicode编码表.png"
//        val filePath = "D:\\zhupingjing\\testFile\\sokit-1-3-win32-chs.zip"
//        service.testJavaUpload(bucketName, "sokit-1-3-win32-chs.zip", filePath)
        service.testJavaUpload(bucketName, "Unicode编码表.png", filePath)
    }


    @Test
    fun testMulitUploadFileWithJava2() {
        val (bucketName, service) = getS3Server()

        val list = mutableListOf<String>()
        list.add("D:\\zhupingjing\\testFile\\Unicode编码表.png")
        list.add("D:\\zhupingjing\\testFile\\sokit-1-3-win32-chs.zip")

        list.forEach {
            service.testJavaUpload(bucketName, it.substringAfterLast("\\"), it)
        }
//        val filePath =
//        service.testJavaUpload(bucketName, "sokit-1-3-win32-chs.zip", filePath)
//        service.testJavaUpload(bucketName, "Unicode编码表.png", filePath)
    }

    @Test
    fun testSetTagging() {
        val (bucketName, service) = getS3Server()
        val tagList = ArrayList<Tag>()
        tagList.add(Tag("PROJECT", "app-server"))
        tagList.add(Tag("USER", "root"))
        val key = "testTag0201.png"
        val result = service.setObjectTagging(bucketName, key, tagList)
        println(result)
        println(result.versionId)
        val tagResult = service.getObjectTagging(bucketName, key)
        tagResult.tagSet.forEach {
            println(it.key + "-->" + it.value)
        }
    }


    @Test
    fun testGetMetaData() {
        val (bucketName, service) = getS3Server()
        val list = mutableListOf<String>()
//        list.add("sokit-1-3-win32-chs.zip")
//        list.add("Unicode编码表.png")
        list.add("testTag02.png")
        list.forEach {
            val data = service.getObject(bucketName, it)
            data?.objectMetadata?.userMetadata?.forEach {
                logger.info("upload -->  ${it.key}--->${it.value}")
            }
            println(data?.objectMetadata?.eTag)
            println(data?.taggingCount)
        }
    }


    @Test
    fun testJavaListMultipartUploads() {
        val (bucketName, service) = getS3Server()
        service.testJavaListMultipartUploads(bucketName)
    }

    @Test
    fun abortMultipartUpload() {
        // 终止分段上传的任务
        val (bucketName, service) = getS3Server()
        service.abortMultipartUpload(bucketName, "DDDDD.zip", "ff63a5507bcbb3e6109ed394ef155501")
    }

    @Test
    fun stopAllTask() {
        val (bucketName, service) = getS3Server()
        val list = service.testJavaListMultipartUploads(bucketName)
        list.forEach { upload ->
            try {
                service.abortMultipartUpload(bucketName, upload.key, upload.uploadId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    @Test
    fun testNio() {
        val byteBuffer = ByteBuffer.allocate(16)
        println(byteBuffer.limit())
        println(byteBuffer.position())
        println(byteBuffer.mark())

        byteBuffer.put(9)
        byteBuffer.putInt(8)
        byteBuffer.putInt(7)

        println(byteBuffer.limit())
        println(byteBuffer.position())
        println(byteBuffer.mark())

        byteBuffer.clear()
        println(byteBuffer.limit())
        println(byteBuffer.position())
        println(byteBuffer.mark())

    }

    @Test
    fun testNioReadFile() {
        try {
            val filePath = "D:\\zhupingjing\\testFile\\Studio 3T.zip"
            val inputStream = FileInputStream(File(filePath))
            val buffer = ByteBuffer.allocate(1024 * 1024 * 5)
            val fileChannel = inputStream.channel
            var allSize = 0;
            while (fileChannel.read(buffer) != -1) {
                buffer.flip()
                val array = buffer.array()
                allSize += buffer.limit()

                buffer.clear()
            }
            fileChannel.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Thread.sleep(2000)
    }

    @Test
    fun testCompLength() {
        val temp = "ABC".toByteArray()
        println(temp.size)
        val data = Base64Utils.encode(temp)
        println(data)

        val bsArray = data.toByteArray()
        println(bsArray.size)

        println("你".toByteArray())
        println("你".toByteArray().size)

        println("A".toByteArray())
        println("A".toByteArray().size)

        println('A'.toByte())

        println(5.toByte())
        println(15.toByte().toString())


        /*   temp.forEach {
               println(it.toString())
           }*/
        println(Integer.toBinaryString(5))

        val list = mutableListOf<String>()
    }

    @Test
    fun testMapReference() {
        val map = mutableMapOf<String, List<String>>()
        val list = mutableListOf<String>()
        println(list?.hashCode())
        map.getOrDefault("a", list)
        list.add("a")
        list.add("b")
        list.add("c")
        map.put("a", list)
        val temp = map.get("a")
        println(temp?.hashCode())
        temp?.forEach { println(it) }

    }

    @Test
    fun testCopyFile() {
        val input = FileInputStream(File("D:\\zhupingjing\\testFile\\Unicode编码表.png"))
        saveFileWithBlocking(input, "D:\\zhupingjing\\testFile\\Unicode编码表new.png")
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

    @Test
    fun testMD5() {
        println(DigestUtils.md5Hex("werwesadfsdf"))
    }

    @Test
    fun testFileSystem() {
        val verxt = Vertx.vertx()
        val fs = verxt.fileSystem()
        val inputStream = FileInputStream(File("D:\\zhupingjing\\testFile\\SecureCRT.zip"))

        val option = OpenOptions()


        vertx.fileSystem().lprops("/you/path/to/your/file") { event ->
            event.result()
        }


        fs.open("D:\\zhupingjing\\testFile\\SecureCRT-new.zip", option) {
            val asyncFile = it.result()
            asyncFile.endHandler {
                println("写入结束")
            }


            val blobSize = 1024 * 1024 * 1
            val byteBuffer = ByteArray(blobSize)
            var i = 0
            var count = 0
            println(Thread.currentThread().name + "主线程")



            while ({ i = inputStream.read(byteBuffer); i }() > -1) {
                val buffer = Buffer.buffer(byteBuffer.copyOfRange(0, i))
                asyncFile.write(buffer, (count * blobSize.toLong())) { res ->
                    if (res.succeeded()) {
                        //TODO 怎么通过回调关闭数据流？？
                        //TODO 如何触发endHandle???
                        asyncFile.flush()
                        println(Thread.currentThread().name + "第$count 写入完成")
                    } else {
                        println(Thread.currentThread().name + "第$count 写入失败")
                    }
                }
                count += 1
            }

            println(Thread.currentThread().name + "--while out")
        }

        Thread.sleep(10000000)
    }


    @Test
    fun testGetFileTyope() {
        val allTypes = MimeTypes.getDefaultMimeTypes();

//        val contentType = allTypes.getRegisteredMimeType(".png")
//        println(contentType)

        val path = Paths.get("SecureCRT.zip")
        val temp = Files.probeContentType(path);
        println(temp)
    }

    @Test
    fun testReplaceBlank() {
        val str = "aaa bbb     ccccc"
        println(str.replace(" ", ""))
    }

    @Test
    fun testWhen() {
        val str = "11"
        val result = when (str) {
            "aa" -> "aaaaaa"
            "bb" -> "bbbbb"
            else -> "else"
        }
        println(result)
    }


    @Test
    fun testURLEncode() {
        val fileName = " "
        println(URLEncoder.encode(fileName, "UTF-8"))
        println(URLDecoder.decode("bb%20aa ", "UTF-8"))
    }

    @Test
    fun testReCallCommit() {
        val set = mutableSetOf<String>()
        set.add("12312")
        println(set)
    }

    @Test
    fun testSplit() {
        val s1 = "1,2,3".split(",".toRegex()).toSet()
        val s2 = mutableSetOf("2", "3", "4")
        println(s1.subtract(s2))
        println(s1.union(s2))
        println(s1.minus(s2))

        println(",,asdfasdf,,".trim(','))

    }

    @Test
    fun testConstruct() {
        val a = Person()
        val b = Person("jack", 12)
        println(a)
        println(b)
        println(b)

        val json = JsonObject()
        println(json.getString("a"))

        println("asdfldijwehn232".replace(".".toRegex(), "*"))
    }

    @Test
    fun testShortMd5() {
        println(shortMD5("456sdfasdfasd74wef1asdf"))
        val str = UUID.randomUUID().toString().replace("-".toRegex(), "")
        println(str)
        println(str.substring(5, 29))
    }

    @Test
    fun testFold() {
        val list = (1..3).toList()
        print(list)

        val init = mutableListOf<Int>()
        val result = list.fold(init) { a, b ->
            a.add(b)
            a
        }

        println(result.size)

    }


    fun shortMD5(sourceStr: String): String {
        var result = ""
        try {
            //初始化MessageDigest信息摘要对象,并指定为MD5不分大小写都可以
            val md = MessageDigest.getInstance("MD5")
            md.update(sourceStr.toByteArray())
            //计算信息摘要digest()方法,返回值为字节数组
            val b = md.digest()

            var i: Int
            val buf = StringBuffer("")
            for (offset in b.indices) {
                //将首个元素赋值给i
                i = b[offset].toInt()
                if (i < 0) {
                    i += 256
                }
                if (i < 16) {
                    //前面补0
                    buf.append("0")
                }
                //转换成16进制编码
                buf.append(Integer.toHexString(i))
            }
            result = buf.toString().substring(8, 24)
        } catch (e: NoSuchAlgorithmException) {
            logger.error("get shortMD5 error ", e)
        }

        return result
    }

    @Test
    fun testMap() {
        val user = User("jack", 20)
        val user2 = User("jack", 20)
        val map = mutableMapOf<User, Int>()
        map.put(user, 1)
        println(map.get(user2))
        println(map.get(user))

        val aa = when(2){
            2 -> 23
            else -> 234
        }
        println(aa)

    }

    @Test
    fun testJson(){
        val json = JsonObject().put("a","a")
        val b = JsonObject().put("b","b")
        println(json.mergeIn(b))
        println(json)

    }

    @Test
    fun testGethost(){
        val httpServerOptions = HttpServerOptions()
        println(httpServerOptions.host)

        println(InetAddress.getLocalHost().hostAddress)

    }
    @Test
    fun tempTest(){
        (1 until  1000).toList().forEach { i ->
            println(i)
        }
    }

}