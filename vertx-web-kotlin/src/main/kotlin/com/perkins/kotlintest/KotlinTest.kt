package com.perkins.kotlintest

import com.alibaba.fastjson.JSONObject
import com.ctrip.framework.apollo.ConfigService
import com.hazelcast.client.impl.protocol.codec.AtomicReferenceIsNullCodec
import com.perkins.bean.User
import com.perkins.common.PropertiesUtil
import com.perkins.mongodb.MongClientUtil
import com.perkins.util.DESUtils
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.mongo.FindOptions
import io.vertx.rxjava.core.Vertx
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.binary.Base64
import org.junit.Test
import org.slf4j.LoggerFactory
import org.springframework.util.DigestUtils
import rx.Observable
import rx.Single
import rx.plugins.RxJavaCompletableExecutionHook
import rx.schedulers.Schedulers
import sun.misc.BASE64Encoder
import java.io.File
//import sun.misc.BASE64Encoder
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.streams.toList


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
        val str = "{\"\$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"encryptDataAddSchema\",\"description\":\"添加加密事件\",\"type\":\"object\",\"properties\":{\"nonce\":{\"type\":\"string\",\"minLength\":10},\"timestamp\":{\"type\":\"number\"},\"token\":{\"type\":\"string\",\"minLength\":10},\"data\":{\"type\":\"object\",\"properties\":{\"event_id\":{\"type\":\"string\",\"minLength\":1},\"context\":{\"type\":\"string\",\"minLength\":1}},\"required\":[\"event_id\",\"context\"]}},\"required\":[\"nonce\",\"timestamp\",\"token\",\"data\"]}"
        println(JsonObject(str))
    }

    @Test
    fun testFilter() {
        val list = (1 until 10).toList()
        list.parallelStream().filter { it % 2 == 0 }.forEach { println(it) }
    }


    @Test
    fun testUnion() {
        val list1 = mutableListOf<Int>(1, 2, 3, 4)
        val list2 = mutableListOf<Int>(1, 2, 5, 6)
        println(list1.union(list2))
        println(list1.subtract(list2))
        println(list1.intersect(list2))
        println(list1.minus(list2))
        println(list1 + list2)

        println(list1.joinToString(",") { "?" })
    }

    @Test
    fun testMap() {
        val map = mutableMapOf<String, Any>()
        println(map.getOrDefault("a", 100))
        println(map.getOrElse("a", { 100 }))
//        println(map.getTextValue("a"))
        val a: String? = null
        map["111"] = a!!

    }

    @Test
    fun testFund() {
        val list = mutableListOf(1, 2, 3, 4)
        println(list.filter { it == 6 }.map { })
    }

    @Test
    fun testEQ() {
        val a = "123"
        val b = "123"
        println(a == b)
        println(a.equals(b))
    }

    @Test
    fun testRandom() {
        val str = Math.random().toChar()
        println(str)
    }

    @Test
    fun createSalt() { // 创建随机盐值
        val byteArray = ByteArray(16)
        SecureRandom().nextBytes(byteArray)
        /* val salt = BASE64Encoder().encode(byteArray)
         println(salt)*/
    }

    @Test
    fun getStr() {
//        val pattern = Pattern.compile("(1(([35][0-9])|(47)|8[0126789]))(\\s|-)*\\d{4}(\\s|-)*\\d{4}");
        val pattern = Pattern.compile("\\(*0\\d{2,3}\\)*-*\\d{7,8}")
        val matcher = pattern.matcher("我的电话号码是：147 83600246100，请给我大地1583600246100啊阿虎请给我大地158 3600 246100啊阿虎" +
                "请给我大地1583600 246100啊阿虎" +
                "请给我大地158-3600-246100啊阿虎" +
                "请给我大地158-3600246100啊阿虎" +
                "请给我大地1583600-246100啊阿虎" +
                "021-66522555" +
                "02166522555" +
                "(021)66522555" +
                "(021)-66522555");
        while (matcher.find()) {
            println(matcher.group())
            println(matcher.groupCount())
        }
    }

    @Test
    fun getRegex() {
//        val pattern = Pattern.compile("\\(*0\\d{2,3}\\)*-*\\d{7,8}")
        val id_18 = "([1-6][1-9]|50)\\d{4}(18|19|20)\\d{2}((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]"
        val id_15 = "([1-6][1-9]|50)\\d{4}\\d{2}((0[1-9])|10|11|12)(([0-2][1-9])|10|20|30|31)\\d{3}"
        val phone = "(^\\d{15}\$)|(^\\d{18}\$)|(^\\d{17}(\\d|X|x)\$)"
//        val mail = "^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+\$"
//        val mail = "[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}"
        val mail = "[a-z0-9A-Z]+[-|a-z0-9A-Z._]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-z]{2,}"

        val str1 =
        //                "我的电话号码是：412325182303135417 sss" +
//                "440204197911113613谁家" +
//                "522528199008133616哈哈哈" +
                "32031177070600" +
                        "是3255133@qq.com" +
                        "是skuher@qq.com.cn" +
                        "是sku-her@qq.com.cn" +
                        "是skuh.er@qq.com.cn" +
                        "是12-we45564@qq.com" +
                        "是kw.we-1245564@qq.com" +
                        "是jd-l1245564@qq.com.cn" +
                        "是:3255133@qq.com"
        val str = "我的电话号码是：412325182303135417 sss 440204197911113613谁家 522528199008133616哈哈哈 32031177070600122撒的发生 320311770706002AAAA 320311770706002333 320311770706001BBBB 3203117707060012222 我的邮箱是：1245564@qq.com 我的邮箱是：12-we45564@qq.com 我的邮箱是：jd-l1245564@qq.com.cn 我的邮箱是：kw.we-1245564@qq.com"
        val pattern = Pattern.compile(mail)
        val matcher = pattern.matcher(str)


        while (matcher.find()) {
            println(matcher.group())
            println(matcher.groupCount())
        }
    }

    @Test
    fun testReplace() {
        val test = "kw.we-1245564@qq.com"
        val str = "我的邮箱是：1245564@qq.com 我的邮箱是：12-we45564@qq.com 我的邮箱是：jd-l1245564@qq.com.cn 我的邮箱是：kw.we-1245564@qq.com"
        println(str.replace(test, ""))

    }

    @Test
    fun testDesUtil() {
        val password = PropertiesUtil.get("despassword")
        val context = "oRv+QkvNNL8="
        val originData = String(DESUtils.decrypt(Base64.decodeBase64(context), password))
        println(originData)
    }

    @Test
    fun testLet() {
        val a: String? = null
        val aa = a.let {
            it + "aaa"
        }
        print(aa)
    }

    @Test
    fun testJsonObject() {
        val data = JsonObject().put("AA", "11")
        val str = data.getString("AA")
        if (!str.isNullOrBlank()) {
            println(str)
        }
    }


    @Test
    fun testParStream() {
        val list = (1 until 10).toList()
        list.parallelStream().forEach {
            Thread.sleep(1000)
            println(it)
        }
        println("==end")

        val map = mutableMapOf<String, String>()
        map.put("aa", "ddd")
        val a: String? = null
        println(map.get(a))

    }


    @Test
    fun testCombin() {
        val l1 = mutableListOf(1, 2, 3)
        val l2 = mutableListOf(14, 5, 6)
        println(l1 + l2)

        val l3 = (0 until 103).toList()
        val count = l3.size
        for (i in 0 until count step 10) {
            val end = if ((i + 10) < count) (i + 10) else {
                count
            }
            val temp = l3.subList(i, end)
            println(i)
            println(temp)
        }

    }


    @Test
    fun testPall() {
        val list = (0 until 10).toList().parallelStream().map {
            println(Thread.currentThread().name)
            Thread.sleep((it % 3) * 1000L)
            it * 10
        }
        println(list.toList())

    }


    @Test
    fun testTime() {
        val time = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
//        val time1= LocalDateTime.now().
        println(time)
        println(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))
        println(LocalDateTime.now().toInstant(ZoneOffset.UTC).toString())
        println(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toString())
        println(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli())
        println(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)).toEpochMilli())
    }


    @Test
    fun testFuture() {
        val count = AtomicInteger(0)
        (0 until 100).toList().parallelStream().map {
            val data = Single.just(it).map {
                Thread.sleep(1000)
                it
            }.subscribe({
                println("$it, end")
                count.getAndIncrement()
            }, {
                count.getAndIncrement()
                it.printStackTrace()
            })
            data
        }.toList()

        while (count.get() < 100) {
            println("waiting……")
            Thread.sleep(1000)
        }
        println("stop……")
    }


    @Test
    fun testMD5() {
//  val id = "zpj-urge-03-1"
//  val id = "user_id-02"
        val id = "UR0000000000021"
        val res = DigestUtils.md5DigestAsHex(id.toByteArray()).substring(8, 24)
        println(res)
    }


    @Test
    fun testASCALL() {
        val list = mutableListOf("1", "2", "3", "a", "b", "c", "你", "我", "他")
        list.forEach {
            it.toByteArray().forEach {
                print(it.toString())
            }
            println()
        }

//        val byte = Byte(1)
        "12".toByteArray().forEach {
            print(it.toString())
        }


    }

    @Test
    fun testBase64() {
/*
        val str = "abc,ABD,123,你我他"
        val base64 = Base64()
        str.forEach {
            print(it.toByte())
//        base64.encodeAsString()
        }
*/

        val data = "ASDFASDFASDFA\n" +
                "http://127.0.0.1/app.html?platform=crm&account=zpj-kf-02&ip=127.0.0.1:8081&developer=0"
        println(String(Base64(true).encode(data.toByteArray()), Charset.forName("UTF-8")))
        println(String(Base64(false).encode(data.toByteArray())))
        println(String(Base64.encodeBase64(data.toByteArray()), Charset.forName("UTF-8")))
    }

    @Test
    fun testRegexBase64() {
        val data = "/NRtHMP970zhnmz3dfv9glyce1n手动阀m/ETI0VVCyvVC8onnWEjgmZwBzxkJCgeMWsOYg+qEJsM3tRjVS+mKchlUkLs8C7shGhKKWLvhcY9+dUhd1ufSp6f2ONRNKZ/Jmfl0417KyrlpJooBsZLPIw0K6uzqyboU+2tsBydS8DNS4rUPmPaPNqvjbzriD77WeKOyHkVwHZ4HVbBllLCmI++VpOgYnoPusFgPiLxWAJEH/On/0DSup+btCnE6FVMD9IS9ia5fzs9tqlJQy91L1Xmp1JNy59Dzr3xg4I4vdKRfhuNC0NaOMoJLQDkstiOfGYtBOdJOx+c5EtnXTKDulmUo3Rn8PWe3/7eK6+ygaEi2NrtswvUTGc2ciq2o5/yVo0xoZj3RjqLwB7YpN/+Sxw1w7LSV838fwi3DB4ji0G/xBxKtqOf8laNMaGY90Y6i8Ae2aC0b738p8R/KNSeuWtpjSCFUxRaXcpFu"
        val regex = Regex("^[a-zA-Z0-9/+]*={0,2}\$")
        val d = regex.matches(data)
        print(d)
    }

    @Test
    fun testWhen() {
        val a = RuntimeException("123")
        when (a) {
            is RuntimeException -> {
                println("=====")
            }
            else -> println("----")
        }
    }


    @Test
    fun testCompose() {
        val f1 = Future.future<Int>()
        val f2 = Future.future<Int>().map {
            println("====")
            2
        }
        f1.complete(2)
        f2.complete(10)

        CompositeFuture.all(f1, f2).setHandler {
            println(it.succeeded())
            println(it.result())
        }

        Thread.sleep(4000)
    }


    @Test
    fun testRxJava() {
        Observable.from(0 until 100).map {
            println(it)
            it
        }.toList().map {
            println(it)
        }

                .subscribe()
    }


    @Test
    fun testFutureMap() {
        val future = Future.future<Int>()
        future.map {
            println("---$it")
            2
        }

        /*
future.setHandler{
    println("---handler${it.result()}")
}*/
        future.complete(100)
        future.map {
            println("=====$it")
//            10
        }
        future.map {
            println("===asd==$it")
            20
        }
        Thread.sleep(2000)
//        print(future.result())
//        print(future.result())
    }

    @Test
    fun testHandleToRxJava() {

        val rx = Observable.unsafeCreate<Int> { subscriber ->
            val handler = Handler<Int> {
                println("handle--->" + it)
                subscriber.onNext(100)
                subscriber.onCompleted()
            }
            handler.handle(10)
        }.flatMap {
            println("map1--->" + it)
            Observable.just(200)
        }
        rx.map {
            println("map2--->" + it)
            300
        }
        rx.subscribe({
            println("end -->$it")
        }, {
            it.printStackTrace()
        })

        Thread.sleep(1000)
    }

    @Test
    fun testUTcTime() {
        val nowTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        println(nowTime)
        println(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)).toEpochMilli())

        println(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli())

        println(LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
        val now = LocalDateTime.now(ZoneOffset.UTC)
        println(now.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        val cal = Calendar.getInstance();
        println(cal.getTimeInMillis())
        println(System.currentTimeMillis())

    }

    val logger = LoggerFactory.getLogger(this.javaClass)
    @Test
    fun testObserver() {
        val client = MongClientUtil.getRxClient()
        val test = (0 until 10).map { idx ->
            val op = FindOptions()
            op.limit = 10
            println("=========")
            client.rxFindWithOptions("event", JsonObject(), op).map {
                println(idx);
                println(it.size);

            }.toObservable()
        }
        Observable.concat(test).toList().subscribe {
            println("======end===")
        }
        try {
            Thread.sleep(50000)
        } catch (e: Exception) {

        }

    }


    @Test
    fun testSlice() {
        val a = mutableListOf(1, 2, 3, 4)
        println(a.slice((0 until 4)))
    }


    @Test
    fun testJson1() {
        println(json { obj("a" to 1) })
        println(json { obj("a" to 1, "b" to 1) })
        println(json { obj("a" to 1, "b" to 1) })
    }


    fun shwoInfo() = println(12)

    @Test
    fun testTimer() {
        val timer = Observable.timer(1, TimeUnit.SECONDS)
        timer.map {
            shwoInfo()
        }.subscribe {
            testTimer()
        }
        Thread.sleep(20000)
    }


    @Test
    fun testSharedData() {
        val vertx = Vertx.vertx()
        vertx.sharedData().rxGetAsyncMap<String, Long>("data").flatMap { map ->
            map.rxGet("aa").map { v ->
                println("----->$v")
            }
        }.subscribe({
            println("===end")
        }, {
            it.printStackTrace()
        })

    }

    @Test
    fun testSubscribe() {
        Single.just(1).map {
            println(1)
        }.map {
            println(2)
        }.doOnSubscribe {
            println("ONsubs")
        }.doOnUnsubscribe {
            println("ON UN subs")
        }.subscribe {
            println("subs")
        }


        Thread.sleep(20000)
    }

    @Test
    fun testObserable() {
        Single.just(1).map {
            logger.info("---1---")
        }.map {
            logger.info("---2---")
        }

                .observeOn(Schedulers.io())
                .map {
                    logger.info("---3---")
                }.map {
                    logger.info("---4---")
                }
                .observeOn(Schedulers.computation())
                .map {
                    logger.info("---5---")
                }.subscribe()

        Thread.sleep(2000)
    }

    @Test
    fun testFunction() {
        f1(::f2)
        f1(f3)
        f1(f4)
        f1(f5())
        f1(f6())
        println("aBc12c".filter { it in 'a'..'z' })
        println(z(2))
        println(z)
        println(h(2))
        println(h)
    }

    fun f2(a: String): Boolean = a.length > 2
    var f3: (s: String) -> Boolean = { it.length > 2 }
    var f31: (String) -> Boolean = { it.length > 2 }
    val f4: (s: String) -> Boolean = { it.length > 2 }
    fun f5(): (s: String) -> Boolean = { it.length > 2 }
    fun f6(): (s: String) -> Boolean {
        return { it.length > 2 }
    }

    val sum = { x: Int, y: Int -> x + y }
    val action = { println(42) }
    val sum1: (Int, Int) -> Int = { x, y -> x + y }
    val sum2: (Int, Int) -> Int = { x, y ->
        x + y
    }
    val action1: () -> Unit = { println(42) }

    fun f1(a: (String) -> Boolean) {
        val a = mutableListOf("aa", "bbb", "c").filter { a(it) }
        println(a)
    }

    private fun String.filter(predicate: (Char) -> Boolean): String {
        val sb = StringBuilder()
        for (index in 0 until length) {
            val element = get(index)
            if (predicate(element)) sb.append(element)
        }
        return sb.toString()
    }

    fun f(x: Int) = x + 10
    val g = fun(x: Int) = x * 10
    val z = g andThen ::f // f is a member function
    val h = A().g andThen A()::f


    @Test
    fun testSchedler() {
        val vertx = Vertx.vertx()
        vertx.setPeriodic(1000) {
            logger.info("---$it")
        }
        Observable.interval(1, TimeUnit.SECONDS).map {
            logger.info("o---$it")
        }.subscribe()

        Thread.sleep(20000)
    }

    @Test
    fun testFormat() {
        val sdf = SimpleDateFormat("yyyy/MM/DD HH:mm")
        println(sdf.parse("2019/10/24 ").toString())
    }

    data class AA(val aa: String, val name: String, val age: Int)

    @Test
    fun testJsonMd5() {
//        val aa = AA("AAA","JACK",230)
        val aa = ACC()
        aa.bb = 23
        aa.aa = "aa"
        aa.cc = "cc"
        val a = JsonObject.mapFrom(aa)
        val b = JSONObject.toJSON(aa)
        println(a)
        println(b)
        val cc = JsonObject().sortedBy { it.key }.map {
            if (it.value is JsonObject || it.value is JsonArray) {
                JsonObject().put(it.key, it.value)
            } else {
                JsonObject().put(it.key, it.value)
            }
        }.fold(JsonObject()) { a, b ->
            a.mergeIn(b)
        }
        println(cc)
/*
        println(a)
        println(DigestUtils.md5DigestAsHex(a.toString().toByteArray()))
        println(b)
        println(DigestUtils.md5DigestAsHex(b.toString().toByteArray()))
        println(a == b)
        println(a.equals(b))
        println(a.toString() == b.toString())

*/
    }

    @Test
    fun testSingle() {
        Single.just(12).map {
            45 / 0
        }
                .doOnError {
                    println("asdf")
                }
                .onErrorReturn {
                    println("2222")
                    234
                }.subscribe({
                    println("------")
                }, {
                    println("0000")
                })
    }

    @Test
    fun testJsonGetNull() {
        println(JsonObject().remove("sign"))
    }

    @Test
    fun fileToHexString() {
        val file = File("D:\\zhupingjing\\testFile\\4bed2e738bd4b31c96d85a4281d6277f9f2ff867.jpg")
        val array = file.readBytes()
        println(array.size)
    }

    @Test
    fun testWhen2() {
        val a = 0
        val b = 1
        val c = 2
        when {
            b == 1 -> {
                println("---b--")
//                return //return 针对的方法进行return，不是对于 when表达式的return
            }
            a == 0 -> {
                println("---a--")
            }
            c == 0 -> {
                println("---c--")
            }
            else -> println("--oo---")
        }
        println("--end")
    }


    @Test
    fun testSingleToBlocking() {
        println("----------")
        val single = Single.just("11").observeOn(Schedulers.newThread()).map {
            Thread.sleep(5000)
            "222"
        }.map {
            10 / 0
            println(it)
        }.onErrorReturn {

            logger.error("----", it)
        }
        val blok = single.toBlocking().value()
        println("---->" + blok)

        println("-----end-----")
    }

    @Test
    fun testTime2() {

        val a = 1000L * 60 * (60 * 9 - 1)
        val b = 1000L * 60 * 60 * 9
        println(b - a)

    }

    @Test
    fun testSequence() {
        (1 until 10).toList().asSequence().map {
            println(it)
            it * 10
        }
                .forEach { println(it) }
    }


    @Test
    fun testMaxBy() {
        // 查询最大的值，判断条件是列表中的元素 最大值
        val res = (1 until 10).toList().maxBy {
            it
        }
        println(res)
        (1 until 10).toList().parallelStream().forEach{
            Thread.sleep(Int.MAX_VALUE.toLong())
            it
        }
        Thread.sleep(Int.MAX_VALUE.toLong())

    }

    @Test
    fun testSingleError(){
        Single.just(1).map {
            logger.info("-[[")
            2
        }.flatMap {
            Single.error<Int>(RuntimeException("SDDD"))
        }.subscribe({
            logger.info("--rsulut$it")
        },{
            logger.error("====",it)
        })

        Thread.sleep(20000)
    }


    @Test
    fun testTime3(){
        val a = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        println(a)
        println(LocalDateTime.now())
        println(LocalDateTime.now().toInstant(ZoneOffset.UTC))
        println(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli())
        println(LocalDateTime.now().toInstant(ZoneOffset.ofHours(0)).toEpochMilli())
        println(System.currentTimeMillis())
    }

    @Test
    fun testApolloConfig(){
        val config = ConfigService.getAppConfig(); //config instance is singleton for each namespace and is never null
        val someKey = "someKeyFromDefaultNamespace";
        val someDefaultValue = "someDefaultValueForTheKey";
        val value = config.getProperty(someKey, someDefaultValue);
        println(value)
    }


}

class A {
    fun f(x: Int) = x + 10
    val g = fun(x: Int) = x * 10
}