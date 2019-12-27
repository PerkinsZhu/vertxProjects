package com.perkins.rx

import arrow.core.Left
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import org.junit.Test
import org.slf4j.LoggerFactory

class RXKtolin {
    val logger = LoggerFactory.getLogger(this.javaClass)
    @Test
    fun tesSimple() {
        val list = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
        list.toObservable() // extension function for Iterables
                .filter { it.length >= 5 }
                .subscribeBy(  // named arguments for lambda Subscribers
                        onNext = { println(it) },
                        onError = { it.printStackTrace() },
                        onComplete = { println("Done!") }
                )

    }


    @Test
    fun testBackpressure() {
        //runBackpressure()
        testConcurrent()
        Thread.sleep(10000)
    }

    private fun testConcurrent() {
        Flowable.range(1, 10)
                .flatMap { v ->
                    //在Flowable中开Flowable 可以实现多线程并发处理
                    Flowable.just(v)
                            .subscribeOn(Schedulers.computation())
                            .map {
                                logger.info("$it")
                                it
                            }
                            .map { w ->
                                Thread.sleep(1000)
                                w * w
                            }
                }
                .blockingSubscribe { println(it) }
    }

    //可以实现背压执行
    private fun runBackpressure() {
        Flowable.range(1, 10).map {
            println(it)
            it
        }.map {
            Thread.sleep(1000)
            it
        }.map {
            println("---$it")
            it
        }.toList().subscribe { t1, t2 ->
            println(t1)
        }
    }

}