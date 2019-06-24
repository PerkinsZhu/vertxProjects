package com.perkins.kotlintest

import com.perkins.bean.User
import org.junit.Test


class KotlinTest {
    @Test
    fun testBase() {
        val str: String? = null
        str.let {
            println("let --> $it")
        }
        str?.let {
            println("let --> $it")
        }
        val user = User("jack", 10)
        when (user) {
            null -> println()

        }

        with(user) {
            val str = "$age,$name" //可以直接調用對象的方法
            "asdfasdf" // 可以返回不通类型的数值
        }

        user.also {
            println(it.age)
            user.age = 19
            234   // 只会返回相同类型的值
        }.also {
            println(it.age)
            user.age = 23
            "asdf"
        }.also {
            println(it.age)
        }


        var mood = "I am sad"

        run {
            // run代码块中 可以重复定义外部的变量，有一个单独的作用域
            val mood = "I am happy"
            println(mood) // I am happy
        }
        println(mood)  // I am sad
    }

}