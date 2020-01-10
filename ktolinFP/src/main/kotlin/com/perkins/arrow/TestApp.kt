package com.perkins.arrow

import arrow.core.*
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.IO.Companion.effect
import arrow.fx.Promise
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.async.async
import arrow.fx.extensions.io.concurrent.dispatchers
import arrow.fx.extensions.io.concurrent.parMapN
import arrow.fx.extensions.io.monad.flatMap
import arrow.fx.extensions.io.monadDefer.monadDefer
import arrow.fx.fix
import arrow.fx.typeclasses.milliseconds
import arrow.unsafe
import com.perkins.arrow.ArrowApp.ThreadInfo
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.slf4j.LoggerFactory

class TestApp {
    val logger = LoggerFactory.getLogger(this.javaClass)

    @Test
    fun testAAA() {
        val a = IO { 0 }.flatMap { IO { it * 2 } }.map { it + 1 }
        println(a.unsafeRunSync())
        val b = IO.effect { 10 }.attempt().unsafeRunSync()
        println(b.getOrElse { 0 })
        val c = b.rightIfNull { 45 }
        println(c)
        val aa = IO<Int> { throw RuntimeException() }.attempt()
        println(aa)
        val aaa = IO<Int> { throw RuntimeException("Boom!") }.runAsync { result ->
            result.fold({ IO { println("Error") } }, { IO { println(it.toString()) } })
        }
        println(aaa.unsafeRunSync())

        val cc = IO.async<Int> { 10 }.attempt().unsafeRunTimed(100.milliseconds)
        println(cc)
        val ccc = IO.just(1).unsafeRunSync()
        println(ccc)

        val d = IO.monadDefer().defer { IO.just(1) }.unsafeRunSync()
        println(d)

        val t = Tuple2(1, 2)
        t.a
    }

    suspend fun sayHello(): Unit =
            println("Hello World")

    suspend fun sayGoodBye(): Unit =
            println("Good bye World!")

    fun greet(): IO<Unit> =
            IO.fx {
                val pureHello = !effect { sayHello() }
                val pureGoodBye = !effect { sayGoodBye() }
            }

    @Test
    fun testGreet() {
        println(greet())
    }


    @Test
    fun testAsync() {
        val c = IO.async { callback: (Either<Throwable, Int>) -> Unit ->
            callback(1.right())
        }.fix().attempt().unsafeRunSync()

        println(c)

        val d = IO.async { callback: (Either<Throwable, Int>) -> Unit ->
            callback(RuntimeException().left())
        }.fix().attempt().unsafeRunSync()

        println(d)

    }

    private fun println(a: Any) {
        logger.info(a.toString())
    }

    @Test
    fun testPromise() {
        val promise: IO<Promise<ForIO, Int>> = Promise.uncancelable<ForIO, Int>(IO.async()).fix()
        println(promise.unsafeRunSync().get())
        val result = Promise.uncancelable<ForIO, Int>(IO.async()).flatMap { p ->
            p.complete(10)
            p.get()
        }.unsafeRunSync()
        println(result)
    }

    @Test
    fun testEffect() {
        effect { println("-----") }.unsafeRunSync()

        val a = IO.fx {
            !effect { println("=====") }
            !effect { println("*********") }
        }

        val b = unsafe { runBlocking { a } }
        println(b.unsafeRunSync())
    }

    @Test
    fun testRepeat() {
        val seq = generateSequence(0) { it + 1 }.k()
        seq.take(10).forEach {
            println(it)
        }
    }

    @Test
    fun testAsync2() {
        val program = IO.fx {
            val fiberA = !effect { "aaa" }.fork(dispatchers().default())
            val fiberB = !effect { "bb" }.fork(dispatchers().io())
            val threadA = !fiberA.join()
            val threadB = !fiberB.join()
            !effect { println(threadA) }
            !effect { println(threadB) }
        }
        program.unsafeRunSync()

        val aa = IO.fx {
            val (threadA: String, threadB: String) =
                    !dispatchers().default().parMapN(
                            effect { "aa" },
                            effect { "bbb" },
                            ::ThreadInfo
                    )
            !effect { println(threadA) }
            !effect { println(threadB) }
        }
        aa.unsafeRunSync()
    }

}