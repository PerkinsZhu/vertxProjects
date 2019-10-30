package com.perkins.rx

import io.vertx.core.json.JsonObject
import org.junit.After
import org.junit.Test
import rx.Observable
import rx.Observer
import rx.Single
import rx.Subscriber
import rx.schedulers.Schedulers

import java.util.concurrent.TimeUnit
import rx.functions.Action0
import rx.functions.Action1
import java.util.concurrent.atomic.AtomicInteger


class RXTest {

    @After
    fun after() {
        Thread.sleep(15000)
    }

    fun sleep(i: Long? = null) {
        val b = i ?: 5000L
        Thread.sleep(b)
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

    @Test
    fun testConcat() {
        val list = (1..10).map {
            Single.just(it).toObservable()
        }

        list.reduce { a, b ->
            val temp = Observable.concat(a, b)
            temp
        }.subscribe({
            println(it)
        }, {
            it.printStackTrace()
        })

        val init = Observable.just(mutableListOf<Int>())
        list.fold(init) { a, b ->
            Observable.zip(a, b) { aa, bb ->
                aa.add(bb)
                aa
            }
        }.subscribe({
            println(it)
        }, {
            it.printStackTrace()
        })


        /*Single.concat(Single.just(add(1, 2)), Single.just("jack"), Single.just(mutableListOf<String>()))
                .toList()// 这里会把concat的所有元素合并到list中一次性返回，如果不toList，则会调用三次subscribe函数体
                .subscribe({
                    println(it)
                }, {

                })*/
    }

    @Test
    fun testObserverThread() {
        printThreadName("main -- start ")
        val obj = Observable.just(1).map {
            printThreadName("$it --- a -- start")
            sleep(2000)
            printThreadName("$it --- a -- end")
            it * 10
        }
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .map {
                    printThreadName("$it --- b -- start")
                    sleep(2000)
                    printThreadName("$it --- b -- end")
                    it * 10
                }
                .flatMap {
                    Observable.just(it).map {
                        printThreadName("$it --- c -- start")
                        sleep(2000)
                        printThreadName("$it --- c -- end")
                        it * 10
                    }
                }.flatMap {
                    end(Observable.just(it).map {
                        printThreadName("$it --- cc -- start")
                        sleep(2000)
                        printThreadName("$it --- cc -- end")
                        it * 10
                    })
                    Observable.just(it).map {
                        printThreadName("$it --- ccc -- start")
                        sleep(2000)
                        printThreadName("$it --- ccc -- end")
                        it * 10
                    }
                }.map {
                    printThreadName("$it --- d -- start")
                    sleep(2000)
                    printThreadName("$it --- d -- end")
                    it * 10
                }
        printThreadName("main -- end ")

        end(obj)

        /**
         * 实验结论：
         *      observer 不会阻塞主线程，但是同一个observer上的多个map或者flatmap是阻塞的
         *      即使设置为不同的线程运行也是阻塞的
         */

    }

    fun <T> end(obj: Observable<T>) {
        obj.subscribe({
            println(it)
        }, {
            it.printStackTrace()
        })
    }


    fun printThreadName(name: String) = println("$name-->" + Thread.currentThread().name)

    @Test
    fun testMerge() {
        val a = Observable.just(1, 2, 3, 4)
        val b = Observable.just(5, 6, 7, 8)
        Observable.merge(a, b).subscribe({
            println(it)
        }, {

        })

        a.mergeWith(b).subscribe({
            println("---" + it)
        }, {})
        //TODO map代码块如何并行？

        //TODO JOIN
        /*   a.join(b) { c, d ->

           }*/

    }

    @Test
    fun testRxJava2() {
        val observable = Observable.just("hello")
        Observable.from((1..10))
        Observable.from((1..10).toList().toTypedArray()).map {
            println(it)
            it
        }.subscribe()
        observable.skip(10).take(2).map { s -> println(s) }.doOnCompleted {}


        val temp = observable.subscribe()

        val observable2 = Observable.create(object : Observable.OnSubscribe<String> {
            override fun call(subscriber: Subscriber<in String>) {
                subscriber.onNext("Hello")
                subscriber.onNext("Hi")
//                subscriber.onError(RuntimeException("my error"))
                subscriber.onNext("Aloha")
                subscriber.onCompleted()
                subscriber.onNext("Aloha22")
                subscriber.onNext(456.toString())
                subscriber.onCompleted()
                subscriber.onError(RuntimeException("my error 22"))

            }
        })
        observable2.map {
            println(it)
            it
        }.subscribe({
            println("end ---$it")
        }, {
            it.printStackTrace()
        })


    }

    @Test
    fun testCreate() {
        Observable.create(Observable.OnSubscribe<Int> { observer ->
            try {
                if (!observer.isUnsubscribed) {
                    for (i in 1..4) {
                        observer.onNext(i)
                    }
                    observer.onCompleted()
                }
            } catch (e: Exception) {
                observer.onError(e)
            }
        }).subscribe(object : Subscriber<Int>() {
            override fun onNext(item: Int?) {
                println("Next: " + item!!)
            }

            override fun onError(error: Throwable) {
                System.err.println("Error: " + error.message)
            }

            override fun onCompleted() {
                println("Sequence complete.")
            }
        })

//        test from

        val items = arrayOf(0, 1, 2, 3, 4, 5)
        val myObservable = Observable.from(items)

        myObservable.subscribe(
                { item -> println(item) },
                { error -> println("Error encountered: " + error.message) },
                { println("Sequence complete") }
        )


    }

    @Test
    fun tesetInterface() {
        println(object : OnBind {
            override fun onBindChildViewData(holder: String, itemData: Any, position: Int) {
                println(holder + itemData + position)
            }
        })
    }


    @Test
    fun testZip() {
        /*Single.zip(Single.just(1), Single.just(3)) { a, b ->
            println("$a--$b")
            a + b
        }.subscribe({
            print("zip -->$it")
        }, {

        })*/

        val temp = (1..3).toList().map {
            Single.just(it)
        }
        val init = Observable.just(mutableListOf<Int>())
        val ddd = temp.fold(init) { list, single ->
            Observable.zip(list, single.toObservable()) { a, b ->
                println("zip--${a.size}--$b")
                a.add(b)
                a
            }
        }
        ddd.map {
            println(it.size)
        }.subscribe({

        }, {
            it.printStackTrace()
        })


    }


    @Test
    fun testZip2() {
        val a = AtomicInteger(0)
        val data = Single.just((1..10).toList()).map {
            Thread.sleep(1000)
            a.getAndIncrement()
            it.map { it * 10 }
        }.map {
            val tipData = JsonObject().put("a", a.get())
            Pair(tipData, it)
        }

        val tipData = JsonObject().put("a", a.get())
        val result = Single.zip(Single.just(tipData), data) { a, b ->
            println(a)
            Pair(a, b)
        }

        result.map {
            println(it.first)
            println(it.second.first)
            println(it.second.second)
        }.subscribe()


    }

    @Test
    fun testOnError() {
        Single.just(0).map {
            100 / it
        }.onErrorReturn {
            println("----onerror----")
            it.printStackTrace()
            1000
        }.subscribe({
            println(it)
        }, {
            println("--sub---")
            it.printStackTrace()
        })

    }

    @Test
    fun testBlock() {
        val data = Single.just((1 until 100).toList()).map {
            Thread.sleep(2000)
            println(it)
            it.map { it * 10 }
        }.toBlocking().value()
        println(data)
    }

    @Test
    fun testException() {
        Single.just(10).map {
            10 / 0
        }.doOnError {
            println("========")
            it.printStackTrace()
            throw java.lang.RuntimeException("111111111")
        }.map {
            print(it)
            it
        }.subscribe({}, {
            println("----")
            it.printStackTrace()
        })
    }

    @Test
    fun testIntDefault() {
//        var a:Int
//        showInt(12)
        var list: List<Long> = listOf()
//        val temp =
        println(list + 5)

    }

    fun showInt(a: Int) {
        println(a)
    }

    @Test
    fun testZip5() {
        val list = (0 until 10).toList().map {
            Single.just(it).map {
                println("map-$it---"+Thread.currentThread().name)
                it
            }
        }

        val s1 = Single.just(1).map {
            println("map1---"+Thread.currentThread().name)
            2
        }
        val s2 = Single.just(1).observeOn(Schedulers.newThread()).map {
            println("map2---"+Thread.currentThread().name)
            2
        }

  /*      Single.zip(s1,s2){ i: Int, i1: Int ->
            println("zip--" + Thread.currentThread().name)
        }.subscribe {
            println("sub--" + Thread.currentThread().name)
        }*/

        Single.zip(list){
            it.forEach{ any ->
                println(any.javaClass)
                println(any)
            }
        }.subscribe {
            println("sub--" + Thread.currentThread().name)
        }
        Thread.sleep(1000)
    }


    @Test
    fun testThread044(){
       Single.just(1).map{
            println(Thread.currentThread().name)
            it
        }.observeOn(Schedulers.computation()).map {
            println(Thread.currentThread().name)
            it
        }.observeOn(Schedulers.io()).map {
            println(Thread.currentThread().name)
            it
        }.observeOn(Schedulers.newThread()).map {
//           Schedulers.reset()
           println(Thread.currentThread().name)
           it
       }.subscribe{
            println(Thread.currentThread().name)
        }
        Thread.sleep(1000)
    }

}

interface OnBind {

    fun onBindChildViewData(holder: String, itemData: Any, position: Int)

}