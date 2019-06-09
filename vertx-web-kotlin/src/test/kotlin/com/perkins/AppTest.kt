package com.perkins

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

    @Test
    fun testInputStream() {
        val file = FileInputStream(File("uploads/0644d274-c987-499b-8286-8d72105b15e1"))
        val vertx = Vertx.vertx()
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
        with(obj){
            println(this)
        }
        obj.let {
            println(it)
            println(this)
        }
    }

    @Test
    fun  testLet(){
        val str :String? = null
        str?.let {
            println(it)
        }
        val map = mutableMapOf<String,String>()
        map.put("aaa","bbbb")
        map.put("cccc","dddd")
        println(map)

        val temp = str ?: "aaa"
        println(temp)

    }

    @Test
    fun testCreatFile(){
        val vertx = Vertx.vertx()
        val fs = vertx.fileSystem()
        fs.open("uploads/ssswww.txt",OpenOptions()){
            if(it.succeeded()){
                print(it.result())
            }else{
                it.cause().printStackTrace()
            }
        }
    }

}