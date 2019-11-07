package com.perkins.arrow

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.unsafe
import com.perkins.AbstractApp
import kotlinx.coroutines.newSingleThreadContext
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

    fun main2() { // The edge of our world
        unsafe { runBlocking { program } }
    }


}