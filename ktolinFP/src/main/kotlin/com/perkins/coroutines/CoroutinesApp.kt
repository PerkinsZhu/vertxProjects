package com.perkins.coroutines

import arrow.fx.extensions.io.concurrent.sleep
import com.perkins.AbstractApp
import kotlinx.coroutines.*
import org.junit.Test
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.math.log
import kotlin.time.Duration

class CoroutinesApp : AbstractApp() {
    private fun log(data: String) = logger.info("-------$data")
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

        thread {
            logger.info("-----i am thread")
        }.start()

        Thread.sleep(2000)

    }


    @Test
    fun main() {
        /*(0 until 10).toList().parallelStream().forEach {
            GlobalScope.launch {
                // 在后台启动一个新的协程并继续
                delay(1000L)
                logger.info("---$it")
            }
        }*/
        (0 until 10).toList().forEach {
            log("---$it")
//            GlobalScope.launch(Dispatchers.Unconfined) {
            GlobalScope.launch {
                // 协程内部是顺序串行的，协程之间如果不是在同一个线程中，则回事异步的，在同一个线程中则是同步的
                // 在后台启动一个新的协程并继续
//                delay(1000L)
                (0 until 10).forEach { item ->
                    log("$it---$item")
                }
                log("=======$it")
            }
        }
        println("Hello,") // 主线程中的代码会立即执行
        runBlocking {
            // 但是这个表达式阻塞了主线程
            delay(2000L)  // ……我们延迟 2 秒来保证 JVM 的存活
        }
    }

    @Test
    fun testAppScop() {
        runBlocking {
            GlobalScope.launch {
                log("main launch")
                repeat(1000) { i ->
                    log("I'm sleeping $i ...")
                    delay(500L) //这里的delay会导致线程切换
                }
            }
            delay(5300L) // 在延迟后退出
        }
    }

    @Test
    fun testManyCoroutines() = runBlocking {
        repeat(100_000) {
            launch {
                delay(1000L)
                log(it.toString())
            }
        }
    }

    @Test
    fun testCancel() {
        runBlocking {
            val job = launch {
                repeat(1000) { i ->
                    println("job: I'm sleeping $i ...")
                    delay(500L)
                }
            }
            delay(1300L) // 延迟一段时间
            log("main: I'm tired of waiting!")
            job.cancel() // 取消该作业
            log("im cancel")
            delay(1000)
            log("im join")
            job.join() // 等待作业执行结束
            println("main: Now I can quit.")
        }
    }

    @Test
    fun coroutineContextTest() = runBlocking<Unit> {
        launch {
            // 运行在父协程的上下文中，即 runBlocking 主协程
            println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Unconfined) {
            // 不受限的——将工作在主线程中
            println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Default) {
            // 将会获取默认调度器
            println("Default               : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(newSingleThreadContext("MyOwnThread")) {
            // 将使它获得一个新的线程
            println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
        }
    }

    @Test
    fun testDebug() = runBlocking<Unit> {
        val a = async {
            log("I'm computing a piece of the answer")
            6
        }
        val b = async {
            log("I'm computing another piece of the answer")
            7
        }
        log("The answer is ${a.await() * b.await()}")
    }


    @Test
    fun switchContext() {
        newSingleThreadContext("Ctx1").use { ctx1 ->
            newSingleThreadContext("Ctx2").use { ctx2 ->
                runBlocking(ctx1) {
                    log("Started in ctx1")
                    withContext(ctx2) {
                        log("Working in ctx2")
                    }
                    log("Back to ctx1")
                }
            }
        }
    }

    @Test
    fun testThread() {
        runBlocking {
            log("i am run blocking ")
//            GlobalScope.launch { // 会在新的线程中执行
//            launch { //和blocking线程一致，代码会阻塞串行执行
//            this.launch {//和blocking线程一致，代码会阻塞串行执行
//            val  a = this.async<Int> {//和blocking线程一致，代码会阻塞串行执行 ,该方法会有返回值
            async(Dispatchers.Default) {
                //和blocking线程一致，代码会阻塞串行执行
                repeat(10) { i ->
                    log("---$i")
                    delay(1000) // delay会导致线程切换
                    log("---$i--END")
                }
            }
            log("end blocking ")
            delay(10000)
        }
    }


    @Test
    fun testAsync2() {
        GlobalScope.launch {
            val data = this.async {
                (0 until 100).fold(10) { a, b -> a + b }
            }
            val result = data.await()
            log("result :$result")
        }
        Thread.sleep(2000)
    }

    @Test
    fun testCoroutineThread() {
        runBlocking {
            repeat(100) {
                Thread {
                    log("---$it")
                }.start()
            }
        }
    }
}


object AAA : AbstractApp() {

    @JvmStatic
    fun main(args: Array<String>) {
        Thread {
            logger.info("---")
        }.start()

        Thread.sleep(2000)
    }

}