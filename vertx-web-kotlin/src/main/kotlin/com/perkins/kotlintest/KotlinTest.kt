package com.perkins.kotlintest

import com.hazelcast.client.impl.protocol.codec.AtomicReferenceIsNullCodec
import com.perkins.bean.User
import com.perkins.common.PropertiesUtil
import com.perkins.util.DESUtils
import io.vertx.core.json.JsonObject
import org.apache.commons.codec.binary.Base64
import org.junit.Test
import org.springframework.util.DigestUtils
import rx.Single
import rx.plugins.RxJavaCompletableExecutionHook
import sun.misc.BASE64Encoder
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Matcher
import java.util.regex.Pattern
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
        val str = "{\"\$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"UserImportMode\",\"description\":\"商城导入坐席/商铺时请求参数校验\",\"type\":\"object\",\"properties\":{\"nonce\":{\"type\":\"string\",\"minLength\":10},\"timestamp\":{\"type\":\"number\"},\"token\":{\"type\":\"string\",\"minLength\":10},\"data\":{\"type\":\"object\",\"properties\":{\"platform\":{\"type\":\"integer\"},\"list\":{\"type\":\"array\",\"minItems\":1,\"items\":{\"type\":\"object\",\"required\":[\"account_id\",\"leave_start_time\",\"leave_end_time\",\"platform\"],\"properties\":{\"account_id\":{\"type\":\"string\",\"minLength\":1},\"agent_id\":{\"type\":\"string\"},\"agent_name\":{\"type\":\"string\"},\"leave_start_time\":{\"type\":\"integer\"},\"leave_end_time\":{\"type\":\"integer\"},\"platform\":{\"type\":\"integer\"}}}}},\"required\":[\"platform\",\"list\"]}},\"required\":[\"nonce\",\"timestamp\",\"token\",\"data\"]}"
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
//        println(map.getValue("a"))
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
        val salt = BASE64Encoder().encode(byteArray)
        println(salt)
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
    fun testMD5(){
//        val id = "zpj-urge-03-1"
        val id = "temp-user-02"
//        val id = "temp-user-01"
        val res = DigestUtils.md5DigestAsHex(id.toByteArray()).substring(8, 24)
        println(res)
    }
}
