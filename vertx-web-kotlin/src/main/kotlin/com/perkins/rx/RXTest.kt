package com.perkins.rx

import org.junit.After
import org.junit.Test
import rx.Observable
import rx.Scheduler
import rx.Single
import rx.schedulers.Schedulers
import scala.concurrent.Future
import java.lang.RuntimeException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RXTest {

    @After
    fun sleep() {
        Thread.sleep(5000)
    }

    @Test
    fun testRX() {
        val data = Observable.just("123")
        data.flatMap {
            println("asdafs")
            Observable.just("45646")
        }

        data.map {
            println(it)
            22342
        }.subscribe {
            // 不订阅则不会触发 map代码块
            println(it)
        }


        println("end")

    }

    @Test
    fun testSingle() {
        val a = Single.just("11")
        val b = Single.just("22")
        a.toObservable().concatWith(b.toObservable()).toList().subscribe({
            println(it)
        }, {
            it.printStackTrace()
        })

        a.concatWith(b).subscribe({
            println(it)
        }, {
            it.printStackTrace()
        })
        a.concatWith(b).toList().subscribe({
            println(it)
        }, {
            it.printStackTrace()
        })

        val list = (1..10).map {
            val key = it % 3
            Single.just(Pair("aa$key", it))
        }

        val init: Single<MutableList<Pair<String, Int>>> = Single.just(mutableListOf())
        list.fold(init) { acc, single ->
            val temp = acc.flatMap { list ->
                single.map {
                    list.add(it)
                    list
                }
            }
            temp
        }.subscribe({
            println(it)

        }, {
            it.printStackTrace()
        })

        val map = mutableMapOf<String, MutableSet<Int>>()
        val initMap: Single<MutableMap<String, MutableSet<Int>>> = Single.just(map)

        list.fold(initMap) { acc, single ->
            val temp = acc.flatMap { map ->
                single.map {
                    println(it.first)
                    val set = map.getOrDefault(it.first, mutableSetOf<Int>())
                    set.add(it.second)
                    map.put(it.first, set)
                    map
                }
            }
            temp
        }.subscribe({
            println(it)

        }, {
            it.printStackTrace()
        })

        /*list.reduce{acc,single ->
            println("----")
            val temp =  single.toObservable().concatWith(acc.toObservable()).toSingle()
            temp
        }.subscribe({
            println(it)

        },{
            it.printStackTrace()
        })*/


    }


    @Test
    fun testRxJava() {
        val single = Single.just(add(1, 2))
        // compose支持类型的转换
        single.compose {
            it.map {
                it.toString()
            }
        }.map {

        }

        Single.concat(Single.just(add(1, 2)), Single.just("jack"), Single.just(mutableListOf<String>()))
                .toList()// 这里会把concat的所有元素合并到list中一次性返回，如果不toList，则会调用三次subscribe函数体
                .subscribe({
                    println(it)
                }, {

                })

        Single.create<Int> {
            it.onSuccess(add(1, 3))
        }.subscribeOn(Schedulers.newThread()) // 可以指定运行的线程
                .subscribe({
                    //create创建的 必须订阅，否则不会支持方法体
                    println("create--->$it")
                }, {})


        Single.just(1).flatMapObservable {
            Observable.just(1, 2, 3, "asdf")
            Observable.just(1, 2, 3, "asdf").toList() // 注意 toList 的作用
        }.subscribe({
            println("flatMapObservable -->$it")

        }, {})
        // Single.from() //TODO


        Single.zip(Single.just(1), Single.just(3)) { a, b ->
            a + b
        }.subscribe({
            print("zip -->$it")
        }, {})


        Single.just(addWitError(1, 2))
                .timeout(100, TimeUnit.MILLISECONDS)
                .onErrorReturn {
                    it.printStackTrace()
                    100
                }.subscribe({
                    println("error -->$it")
                }, {})

/*
        val executor = Executors.newSingleThreadExecutor()
        Schedulers.computation();// 计算线程
        Schedulers.from(executor);// 自定义
        Schedulers.immediate();// 当前线程
        Schedulers.io();// io线程
        Schedulers.newThread();// 创建新线程
        Schedulers.trampoline();// 当前线程队列执行
*/


    }

    private fun addWitError(i: Int, i1: Int): Int {
        throw  RuntimeException("")
        return i + i1
    }

    fun add(a: Int, b: Int): Int {
        println("add")
        return a + b
    }

}