package com.perkins

import com.perkins.util.Base64Utils
import org.junit.Test
import sun.security.provider.certpath.Vertex
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import jdk.nashorn.internal.objects.NativeArray.forEach
import io.vertx.core.file.AsyncFile
import io.vertx.core.file.OpenOptions
import io.vertx.rx.java.RxHelper


class AppTest {
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
                        println("java 线程--->"+Thread.currentThread().name)
                        try {
                            if (index % 2 == 0) {
                                Thread.sleep(1000)
                            }
                            val position = (value.toByteArray().size * index)
                            asyncFile.write(Buffer.buffer(value), position.toLong()) {
                                println("file 线程--->"+Thread.currentThread().name)
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

                println("open 线程--->"+Thread.currentThread().name)
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
    fun  testStringNull(){
        val name :String? = "123"
        println((name ?: "asddfgds"))
    }
}