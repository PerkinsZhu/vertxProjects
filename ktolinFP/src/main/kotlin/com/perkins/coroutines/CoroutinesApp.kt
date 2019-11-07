package com.perkins.coroutines

import com.perkins.AbstractApp
import kotlinx.coroutines.*
import org.junit.Test

class CoroutinesApp : AbstractApp() {

    //协程测试
    @Test
    fun coroutineDemo1() {
        println("test func coroutineDemo1")
        GlobalScope.launch {
            delay(2000)
            println("coroutine finish")
        }

        println("coroutine start")
        Thread.sleep(3000)
        println("coroutine end")
    }

    @Test
    fun coroutinesDemo2() {

        logger.info("AA" + "协程初始化开始，时间: " + System.currentTimeMillis())
        // 官方提供的四种线程
        Dispatchers.Default // 不指定时使用的默认线程
        Dispatchers.IO
        Dispatchers.Main // 依赖android
        Dispatchers.Unconfined // 没指定，就是在当前线程
        // 自定义协程线程池
        val singleThreadContext = newSingleThreadContext("Sing-AA")
        val fiexedThreadContext = newFixedThreadPoolContext(5, "Fixed-AA")

        CoroutineStart.DEFAULT //默认启动模式
        CoroutineStart.LAZY //懒加载模式，你需要它的时候，再调用启动，看这个例子
        CoroutineStart.ATOMIC
        CoroutineStart.UNDISPATCHED
        GlobalScope.launch(singleThreadContext, CoroutineStart.ATOMIC) {
            logger.info("=========singleThreadContext=========")
        }
        var job: Job = GlobalScope.launch(start = CoroutineStart.LAZY) {
            logger.info(("AA----协程开始运行，时间: " + System.currentTimeMillis()))
        }

        Thread.sleep(1000L)
        // 手动启动协程
        job.start()
/*
        job.start() - 启动协程，除了 lazy 模式，协程都不需要手动启动
        job.join() - 等待协程执行完毕
        job.cancel() - 取消一个协程
        job.cancelAndJoin() - 等待协程执行完毕然后再取消
*/

        GlobalScope.launch(Dispatchers.Unconfined) {
            logger.info("AA" + "协程初始化完成，时间: " + System.currentTimeMillis())
            for (i in 1..3) {
                logger.info("AA" + "协程任务1打印第$i 次，时间: " + System.currentTimeMillis())
            }
            delay(500)
            for (i in 1..3) {
                logger.info("AA" + "协程任务2打印第$i 次，时间: " + System.currentTimeMillis())
            }
        }

        logger.info("AA" + "主线程 sleep ，时间: " + System.currentTimeMillis())
        Thread.sleep(1000)
        logger.info("AA" + "主线程运行，时间: " + System.currentTimeMillis())

        for (i in 1..3) {
            logger.info("AA" + "主线程打印第$i 次，时间: " + System.currentTimeMillis())
        }
    }

    @Test
    fun testAsync() {
        GlobalScope.launch(Dispatchers.Unconfined) {
            val deferred = GlobalScope.async {
                delay(1000L)
                logger.info("AA--This is async ")
                return@async "taonce"
            }

            logger.info("AA--协程 other start")
            val result = deferred.await()
            logger.info("AA--async result is $result")
            logger.info("AA--协程 other end ")
        }

        logger.info("AA--主线程位于协程之后的代码执行，时间:  ${System.currentTimeMillis()}")
        Thread.sleep(2200)
    }

    @Test
    fun testBlocking() {
        // blocking 可以阻塞当前线程
        runBlocking {
            // 阻塞1s
            println("string，，，，，，")
            delay(3000L)
            println("This is a coroutines ")
        }
        // 阻塞2s
        Thread.sleep(2000L)
        println("main end ")
    }

    suspend fun getToken(): String {
        delay(300)
        logger.info("AA----getToken 开始执行，时间:  ${System.currentTimeMillis()}")
        return "ask"
    }

    suspend fun getResponse(token: String): String {
        delay(100)
        logger.info("AA----getResponse 开始执行，时间:  ${System.currentTimeMillis()}")
        return "response"
    }

    fun setText(response: String) {
        logger.info("AA----setText 执行，时间:  ${System.currentTimeMillis()}")
    }

    @Test
    fun testSup() {
        // 运行代码
        GlobalScope.launch(Dispatchers.Unconfined) {
            logger.info("AA----协程 开始执行，时间:  ${System.currentTimeMillis()}")
            val token = getToken()
            val response = getResponse(token)
            setText(response)
        }
        Thread.sleep(2000)
    }

}