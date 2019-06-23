package com.perkins.rx

import io.vertx.core.AsyncResult
import io.vertx.core.Verticle
import io.vertx.core.file.OpenOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.rx.java.RxHelper
import io.vertx.rxjava.core.Vertx
import org.junit.After
import org.junit.Test
import rx.Observable
import java.util.concurrent.TimeUnit


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
        Thread.sleep(10000)
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
}