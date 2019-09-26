package com.perkins

import io.vertx.rxjava.core.Vertx
import org.junit.Test
import java.util.concurrent.CountDownLatch

object TestMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val path = System.getProperty("user.dir")
        // 这里获取的路径就是当前jar包的路径
        println(path)
    }
}


class  Temp(){

    @Test
    fun testLet(){
        val a:String? = null
        val b = a?.let {
            it +"123"
        }
        println(b)
    }


    @Test
    fun closeJVMWhenVertx() {
        val vertx = Vertx.vertx()
        val countDownLatch = CountDownLatch(1)


        vertx.rxExecuteBlocking<Int> {
            Thread.sleep(4000)
            println("=====end====")
            it.complete(12)
        }.subscribe {
            println(it)
            vertx.close {
                countDownLatch.countDown()
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            countDownLatch.await()
            println("----runtime end-------")
        }))

        println("exist")
    }
}