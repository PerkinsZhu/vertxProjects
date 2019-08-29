package com.perkins.rx

import io.vertx.core.AsyncResult
import io.vertx.core.Verticle
import io.vertx.core.file.OpenOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.rx.java.RxHelper
import io.vertx.rxjava.core.Vertx
import org.junit.After
import org.junit.Test
import rx.Observable
import rx.Scheduler
import rx.Single
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.collections.LinkedHashSet
import kotlin.streams.toList


class RxTest {
    val vertx = Vertx.vertx()
    @Test
    fun testRxJava() {
        val observable = RxHelper.observableHandler<Long>()
        observable
                .subscribe { id ->
                    println(id)
                    id
                }
        vertx.setTimer(1000, observable.toHandler())
    }

    @After
    fun sleep() {
//        Thread.sleep(10000)
    }

    val logger = LoggerFactory.getLogger(this.javaClass)

    @Test
    fun testHttpServer() {

        val observable = RxHelper.observableFuture<AsyncResult<io.vertx.rxjava.core.http.HttpServer>>()
        observable.subscribe(
                { server ->
                    logger.info("success")
                },
                { failure ->
                    logger.error("fail", failure)
                }
        )
        vertx.createHttpServer(HttpServerOptions().setPort(1234).setHost("localhost"))
//                .listen(observable.toHandler())
    }


    @Test
    fun testFS() {
        val fs = vertx.fileSystem()
        fs.open("H:\\test\\data.txt", OpenOptions()) {
            it.result().toObservable().forEach {
                println(it.toString())
            }
        }
    }

    @Test
    fun testDelove() {
        val verticle = TestVerticle()
        io.vertx.rxjava.core.RxHelper.deployVerticle(vertx, verticle).subscribe(
                {
                    println("success ->$it")
                },
                {
                    println("success -> error")
                    it.printStackTrace()
                }
        )
    }

    @Test
    fun testHttpServer2() {
        val server = vertx.createHttpServer()
        server.requestHandler {
            print("----")
            it.response().end("ok")
        }

        val single = server
                .connectionHandler {}
                .connectionHandler {}
                .rxListen(1234, "localhost")

        single.subscribe({
            println("ok")
        }, {
            println("error")
            it.printStackTrace()
        })
    }

    @Test
    fun testScheduler() {
        val scheduler = RxHelper.scheduler(vertx.delegate);
        val timer = Observable.interval(100, 100, TimeUnit.MILLISECONDS, scheduler);
        timer
//                .startWith(100)
                .subscribe({
                    println(it)
                }, {
                    it.printStackTrace()
                })
        val scheduler2 = RxHelper.blockingScheduler(vertx.delegate);
        val timer2 = Observable.interval(100, 100, TimeUnit.MILLISECONDS, scheduler2);
        timer2.subscribe({
            println("2--->" + it)
        }, {
            it.printStackTrace()
        })
    }

    @Test
    fun zipTest() {
        val sing01 = Single.just(100).map {
            Thread.sleep(3000)
            it * 10
        }
        val sing02 = Single.just(200).map {
            it * 10
        }

        Single.zip(sing02, sing01) { a, b ->
            //            println(a)
//            println(b)
            Pair(a, b)
        }.subscribe({
            print(it)
        }, {

        })
    }

    @Test
    fun testStream() {
        val list = (1 until 10).toList().stream()
                .map { i ->
                    19 / (5 - i)
                }
                .collect(Collectors.toList())
        println(list)
    }

    @Test
    fun testParStream() {
        val list = (1 until 10).toList().parallelStream().map { i ->
            println(Thread.currentThread().name)
            i * 10
        }.toList()
        println(list)
        /*(1 until 10 ).toList().parallelStream().forEach { i->
            println(Thread.currentThread().name)
            i * 10
        }*/
        /* (0 until 3000).toList().forEach {
             testConcurrent()
         }*/

    }

    fun testConcurrent() {
        val map = mutableMapOf<String, MutableList<String>>()
        val list = mutableSetOf<Int>()
        val listre = Collections.synchronizedCollection(list)
        (0 until 1000).toList().parallelStream().forEach { i ->
            val key = (i % 10).toString()
//            synchronized(list){
            listre.add(i)
//            }
            synchronized(map) {
                val list = map.getOrDefault(key, mutableListOf())
                list.add("$i")
                map[key] = list
            }
        }
        map.forEach { (t, u) ->
            if (u.size != 100) {
                println("$t ----> ${u.size}")
            }
        }
        println(list.size)
    }

    @Test
    fun testSetMap() {
        val map = mutableMapOf<Int, Int>()
        (0 until 100).toList().forEach {
            map[it % 10] = it
        }
        println(map)

    }

    @Test
    fun testRXThread() {
        (1 until 10).toList().parallelStream().forEach { i ->
            println("$i===>" + Thread.currentThread().name)
            Single.just(i).map {
                println("$i--" + Thread.currentThread().name)
                it
            }.flatMap {
                Single.just(5).map {
                    println("$i--" + Thread.currentThread().name)
                    it
                }
            }.subscribe()
        }
    }

    @Test
    fun tsetJoin() {
        val str = (1 until 10).toList().joinToString(",") {
            "(${it})"
        }
        println(str)
    }

    @Test
    fun testConcurretnt() {
        val s1 = Single.just(1).map {
            Thread.sleep(1000)
            println("-------${Thread.currentThread().name}")
            1
        }.subscribeOn(Schedulers.newThread())
        val s2 = Single.just(1).map {
            Thread.sleep(2000)
            println("========${Thread.currentThread().name}")
            2
        }.subscribeOn(Schedulers.newThread())
        val start = System.currentTimeMillis()
        Single.zip(s2, s1) { a, b ->
            println((System.currentTimeMillis() -start))
            println(a + b)
            println("${Thread.currentThread().name}")
        }.subscribe()

    Thread.sleep(3000)
    }

}