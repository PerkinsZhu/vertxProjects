package com.perkins.eventbus

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import org.junit.Test
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.rxjava.core.eventbus.EventBus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.Buffer


class App {


    val vertx = Vertx.vertx();
    val executor = vertx.createSharedWorkerExecutor("test")
    @Test
    fun testClusterEventBus() {
        val options = VertxOptions()
        Vertx.clusteredVertx(options) { res ->
            if (res.succeeded()) {
                val vertx = res.result()
                val eventBus = vertx.eventBus()
                println("We now have a clustered event bus: $eventBus")
            } else {
                System.out.println("Failed: " + res.cause())
            }
        }
    }

    @Test
    fun testSendBuffer() {
        val vertx = Vertx.vertx();
        val eb = vertx.eventBus();
        eb.consumer<ByteArray>("buffer") {
            val body = it.body()
            body.iterator().forEach { println(it) }

            it.reply("ok")
        }


        try {
            while (true) {
                val array = ByteArray(5)
                array.set(0, 1)
                array.set(3, 2)
                eb.send("buffer", array) { res: AsyncResult<Message<ByteArray>> ->
                    if (res.succeeded()) {
                        println("success")
                    } else {
                        res.cause().printStackTrace()
                    }
                }
                Thread.sleep(1000)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Test
    fun testSendString() {
        val vertx = Vertx.vertx();
        val eb = vertx.eventBus();
        eb.consumer<String>("file") { msg ->
            val body = msg.body()
            println(msg.body())
            println(msg.body())
            println(msg.headers())
        }

        try {
            while (true) {
                eb.send("file", "i am  file")
                Thread.sleep(1000)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    @Test
    fun testSendFile() {
        /**
         * 数据分块传送如何实现顺序错乱/
         *
         */
        val eb = vertx.eventBus();
        eb.consumer<ByteArray>("buffer") {
            val headers = it.headers()
            println(headers)
            val body = it.body()
            body.iterator().forEach { println(it) }
            val out = FileOutputStream(File("uploads/busfile.txt"))
            out.write(body)
            out.close()
            it.reply("ok")
        }
        val options = DeliveryOptions()
        options.addHeader("fileName", "fileName.txt")

        try {
            val array = ByteArray(1024)
            val file = FileInputStream(File("uploads/temp.txt"))
            var index = -1
            while ({ index = file.read(array);index }() != -1) {
                val data = array.copyOfRange(0, index)

                eb.send("buffer", data, options) { res: AsyncResult<Message<ByteArray>> ->
                    if (res.succeeded()) {
                        println("success")
                    } else {
                        res.cause().printStackTrace()
                    }
                }

                Thread.sleep(1000)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    @Test
    fun testSendOrder() {
        val vertx = Vertx.vertx();
        val eb = vertx.eventBus();
        eb.consumer<String>("order") {
            println("receive-------->" + it.body())
            it.reply(it.body())
        }



        for (j in 1..3) {
            executor.executeBlocking<Void>({ futrue ->
                for (i in 1..100) {
                    eb.send("order", "data0$j----$i") { res: AsyncResult<Message<String>> ->
                        if (res.failed()) {
                            res.cause().printStackTrace()
                        } else {
                            println("callback-------->" + res.result().body())
                        }
                    }
                }
            }, {})
        }
        while (true) {
            Thread.sleep(1000000000)
        }

    }

    @Test
    fun testEventBusClient() {

        val eb = vertx.eventBus()

        eb.consumer<String>("order") {
            println("receive-------->" + it.body())
            it.reply(it.body())
        }

        eb.send("order", "data01----1") { res: AsyncResult<Message<String>> ->
            if (res.failed()) {
                res.cause().printStackTrace()
            } else {
                println("callback-------->" + res.result().body())
            }
        }
    }


    @Test
    fun testEventBusServer() {
        val eb = vertx.eventBus()
        val server = vertx.createHttpServer()

        server.requestHandler {
            it.bodyHandler{
                body ->
                println(body.toString())
            }
            it.response().end("00000000")
        }

        eb.consumer<String>("order") {
            println("receive-------->" + it.body())
            it.reply(it.body())
        }





        server.listen(8080){
            res ->
            run {
                if (res.succeeded()) {
                    println("Server is now listening!");
                } else {
                    println("Failed to bind!");
                }
            }
        }


        while (true){
            Thread.sleep(1000000000)
        }
    }
}