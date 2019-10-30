package com.perkins.lifecycle

import com.perkins.lifecycle.ww.BBB
import rx.Single

object LifeCycleTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val list = (0 until 10).toList().map {
            Single.just(BBB("$it"))
        }
        Single.zip(list) {
            it.map {
                val item = it as BBB
                item
            }
        }.map {
            println(list.size)
            2
        }.map {
            println(it)
        }.subscribe {
            println("====end==")
        }
        Thread.sleep(2000)

    }
}

