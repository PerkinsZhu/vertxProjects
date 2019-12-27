package com.perkins.arrow

import org.junit.Test

class BaseTest {

    @Test
    fun sayHello(): Unit = println("Hello World")

    fun sayGoodBye(): Unit = println("Good bye World!")

 /*   @Test
    fun greet(): IO<Unit> = IO.fx {
        val pureHello = effect { sayHello() }
        val pureGoodBye = effect { sayGoodBye() }
    }*/
}