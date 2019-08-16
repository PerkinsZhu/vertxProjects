package com.perkins

import org.junit.Test

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
}