package com.perkins

import org.junit.Test
import sun.security.provider.certpath.Vertex
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer


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
            buffer.appendString("你叫什么名字\r\n","UTF-8")
            i++
        }

        fs.writeFile("uploads/data.txt", buffer, { rs ->
            if (rs.succeeded()) {
                print("===ok===")
            }
        })

    }
}