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
        val str = "{\"\$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"UserImportMode\",\"description\":\"商城导入坐席/商铺时请求参数校验\",\"type\":\"object\",\"properties\":{\"nonce\":{\"type\":\"string\",\"minLength\":10},\"timestamp\":{\"type\":\"number\"},\"token\":{\"type\":\"string\",\"minLength\":10},\"data\":{\"type\":\"object\",\"properties\":{\"platform\":{\"type\":\"integer\"},\"list\":{\"type\":\"array\",\"minItems\":1,\"items\":{\"type\":\"object\",\"required\":[\"account_id\",\"leave_start_time\",\"leave_end_time\",\"platform\"],\"properties\":{\"account_id\":{\"type\":\"string\",\"minLength\":1},\"agent_id\":{\"type\":\"string\"},\"agent_name\":{\"type\":\"string\"},\"leave_start_time\":{\"type\":\"integer\"},\"leave_end_time\":{\"type\":\"integer\"},\"platform\":{\"type\":\"integer\"}}}}},\"required\":[\"platform\",\"list\"]}},\"required\":[\"nonce\",\"timestamp\",\"token\",\"data\"]}"
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
        println(list1+ list2)

        println(list1.joinToString(",") { "?" })
    }

    @Test
    fun testMap(){
        val map = mutableMapOf<String,Any>()
        println(map.getOrDefault("a",100))
        println(map.getOrElse("a",{100}))
//        println(map.getValue("a"))
        val a:String ?= null
        map["111"] = a!!
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

    @Test
    fun testRandom(){
        val str = Math.random().toChar()
        println(str)
    }
}
