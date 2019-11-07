package com.perkins.arrow

import arrow.Kind
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.fx.extensions.io.unsafeRun.unsafeRun
import arrow.fx.typeclasses.Concurrent
import arrow.fx.typeclasses.UnsafeRun
import arrow.unsafe
import com.perkins.AbstractApp
import kotlinx.coroutines.*
import org.junit.Test

class ArrowApp : AbstractApp() {
    @Test
    fun testBase() {
        println("-----")
    }

    suspend fun sayHello(): Unit =
            println("Hello World")

    suspend fun sayGoodBye(): Unit =
            println("Good bye World!")

    @Test
    fun FPTest() {
        println(greet())
        println(greet2())
        Thread.sleep(2000)

    }


    fun greet(): IO<Unit> =
            IO.fx {
                val pureHello = effect { sayHello() }
                val pureGoodBye = effect { sayGoodBye() }
            }

    fun greet2(): IO<Unit> =
            IO.fx {
                !effect { sayHello() }
                !effect { sayGoodBye() }
            }

    @Test
    fun main() { // The edge of our world
        unsafe { runBlocking { greet2() } }
//        Thread.sleep(2000)
        println(greet3()) //greet is a pure IO program
    }

    fun sayInIO(s: String): IO<Unit> =
            IO { println(s) }

    fun greet3(): IO<Unit> =
            IO.fx {
                sayInIO("Hello World").bind()
            }

    val contextA = newSingleThreadContext("A")

    suspend fun printThreadName(): Unit =
            println(Thread.currentThread().name)

    val program = IO.fx {
        continueOn(contextA)
        !effect { printThreadName() }
        continueOn(dispatchers().default())
        !effect { printThreadName() }
    }

    @Test
    fun main2() { // The edge of our world
        unsafe { runBlocking { program } }
        Thread.sleep(2000)
    }

    suspend fun threadName(): String = Thread.currentThread().name
    val program3 = IO.fx {
        val fiberA = !effect {
            logger.info("start fiber A")
            Thread.sleep(5000)
            threadName()
        }.map {
            23
        }.fork(dispatchers().default())
        val fiberB = !effect { threadName() }.fork(dispatchers().default())
        val threadA = !fiberA.join()
        val threadB = !fiberB.join()
        !effect { logger.info(threadA.toString()) }
        !effect { logger.info(threadB) }
    }

    @Test
    fun main3() { // The edge of our world
        unsafe { runBlocking { program3 } }
    }


    data class ThreadInfo(
            val threadA: String,
            val threadB: String
    )

    val program4 = IO.fx {
        val (threadA: String, threadB: String) =
                !dispatchers().default().parMapN(
                        effect { threadName() },
                        effect { threadName() },
                        ::ThreadInfo
                )
        !effect { println(threadA) }
        !effect { println(threadB) }
    }

    @Test
    fun main4() { // The edge of our world
        unsafe { runBlocking { program4 } }
    }


    suspend fun threadName(i: Int): String =
            "$i on ${Thread.currentThread().name}"

    val program5 = IO.fx {
        val result: List<String> = !listOf(1, 2, 3).parTraverse { i -> effect { threadName(i) } }
        val result2: List<String> = listOf(1, 2, 3).parTraverse { i -> effect { threadName(i) } }.bind()

        !effect { result.forEach { logger.info(it) } }

    }

    @Test
    fun main5() { // The edge of our world
        unsafe { runBlocking { program5 } }
    }


    val program6 = IO.fx {
        val result: List<String> = !listOf(
                effect { threadName() },
                effect { threadName() },
                effect { threadName() }
        ).parSequence()
        !effect { println(result) }
    }

    @Test
    fun main6() { // The edge of our world
        unsafe { runBlocking { program6 } }
    }

    suspend fun program() = GlobalScope.async { printThreadName() }

    @Test
    fun main7() {
        runBlocking { program().await() }
    }

    suspend fun program7() = GlobalScope.async(start = CoroutineStart.LAZY) { printThreadName() }

    @Test
    fun main8() {
        runBlocking { program7().await() }
    }

    /* a side effect */
    val const = 1

    suspend fun sideEffect(): Int {
        println(Thread.currentThread().name)
        return const
    }

    /* for all `F` that provide an `Fx` extension define a program function */
    fun <F> Concurrent<F>.program(): Kind<F, Int> = fx.concurrent { !effect { sideEffect() } }

    /* for all `F` that provide an `UnsafeRun` extension define a main function */
    fun <F> UnsafeRun<F>.main(fx: Concurrent<F>): Int = unsafe { runBlocking { fx.program() } }

    /* Run program in the IO monad */
    @Test
    fun main9() {
        IO.unsafeRun().main(IO.concurrent())
    }

}