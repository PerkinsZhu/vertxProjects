package com.perkins

object TestMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val path = System.getProperty("user.dir")
        // 这里获取的路径就是当前jar包的路径
        println(path)
    }
}