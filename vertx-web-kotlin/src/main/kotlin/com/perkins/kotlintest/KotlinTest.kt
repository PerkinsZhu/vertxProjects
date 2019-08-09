package com.perkins.kotlintest

import com.perkins.bean.User
import io.vertx.core.json.JsonObject
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


    @Test
    fun testAdd() {
        var a = 0;
        (1 until 10).toList().forEach {
            println(++a)
        }
    }

    @Test
    fun showJson() {
        val str = "{\"\$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"UserUpdateMode\",\"description\":\"坐席名称修改接口\",\"type\":\"object\",\"properties\":{\"nonce\":{\"type\":\"string\",\"minLength\":10},\"timestamp\":{\"type\":\"number\"},\"token\":{\"type\":\"string\",\"minLength\":10},\"data\":{\"type\":\"object\",\"properties\":{\"platform\":{\"type\":\"integer\"},\"account_id\":{\"type\":\"string\",\"minLength\":1},\"account_name\":{\"type\":\"string\",\"minLength\":1},\"mass_id\":{\"type\":\"string\",\"minLength\":1}}},\"required\":[\"platform\",\"account_id\",\"account_name\",\"mass_id\"]},\"required\":[\"nonce\",\"timestamp\",\"token\",\"data\"]}"
        println(JsonObject(str))
    }

    @Test
    fun testFilter(){
        val list = (1 until 10 ).toList()
        list.parallelStream().filter { it % 2 == 0 }.forEach { println(it) }
    }


    @Test
    fun testUnion(){
        val list1 = mutableListOf<Int>(1,2,3,4)
        val list2= mutableListOf<Int>(1,2,5,6)
        println(list1.union(list2))
        println(list1.subtract(list2))
        println(list1.intersect(list2))
        println(list1.minus(list2))
    }

    @Test
    fun testMap(){
        val map = mutableMapOf<String,Any>()
        println(map.getOrDefault("a",100))
        println(map.getOrElse("a",{100}))
//        println(map.getValue("a"))
    }
    @Test
    fun testFund(){
        val list = mutableListOf(1,2,3,4)
        println(list.filter{ it == 6 }.map {  })
    }
    @Test
    fun testEQ(){
        val a = "123"
        val b = "123"
        println(a == b )
        println(a.equals(b))
    }

}
