package com.perkins.sign

import com.perkins.common.PropertiesUtil
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Test
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneOffset

class SignUtil {
    val SALT = PropertiesUtil.get("salt")
    @Test
    fun createSing() {
        val nonce = RandomStringUtils.randomAlphabetic(10).toString()
        val timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        val token = RandomStringUtils.randomAlphabetic(20).toString()
//        val data = JsonArray( SignUtilDemo.getDataArray().toString())
        val data = getData()

        val signJson = sortJson(data)
        val body = JsonObject()
                .put("nonce", nonce)
                .put("timestamp", timestamp)
                .put("token", token)
                .put("data", data)

        val map = mutableMapOf<String, Any>()
        map["nonce"] = nonce
        map["timestamp"] = timestamp
        map["token"] = token
        map["data"] = signJson
        map["salt"] = SALT
        val str = map.toSortedMap().map { e ->
            "${e.key}=${e.value}"
        }.joinToString("&")
        println(str)
        val sign = DigestUtils.md5Hex(DigestUtils.md5Hex(str))
        println(body)
        println(sign)

    }

    @Test
    fun testJons() {
        val data = JsonObject(SignUtilDemo.getDataArray().toString())
        val res = sortJson(data)
        println(res)
    }

    /**
     * 对json 对象进行排序，排序规则：
     *         JsonArray中的每一项顺序保持不变
     *         JsonObject 中，根据 key从小到大进行排序
     * @param data 待排序的Json对象，有可能是JsonObject 或者是JsonArray
     * @return 返回JsonD对象，有可能是JsonObject 或者是JsonArray
     */
    fun sortJson(data: Any): Any {
        return if (data is JsonArray) {
            val temp = data.map {
                sortJson(it)
            }
            JsonArray(temp)
        } else {
            val obj = data as JsonObject
            obj.sortedBy { it.key }.map {
                if (it.value is JsonObject || it.value is JsonArray) {
                    val temp = sortJson(it.value)
                    JsonObject().put(it.key, temp)
                } else {
                    JsonObject().put(it.key, it.value)
                }
            }.fold(JsonObject()) { a, b ->
                a.mergeIn(b)
            }
        }
    }


    fun getData(): JsonArray {
        return JsonArray()
    }

    @Test
    fun getSignFromAllData() {
        val str = "{\"nonce\":\"CawmoplBJr\",\"timestamp\":1563363211701,\"token\":\"GkafKFGiZXgyHTiCwUQy\",\"data\":[{\"account_id\":\"zuoxi-id-05\",\"real_name\":\"zuoxi-name-05\",\"mass_id\":\"zpj-massid-05\",\"mass_name\":\"ZPJ商铺05\"},{\"account_id\":\"zuoxi-id-06\",\"real_name\":\"zuoxi-name-06\",\"mass_id\":\"zpj-massid-06\",\"mass_name\":\"ZPJ商铺06\"},{\"account_id\":\"zuoxi-name-07\",\"real_name\":\"zuoxi-name-07\",\"mass_id\":\"zpj-massid-07\",\"mass_name\":\"ZPJ商铺07\"}]}"
        val json = JsonObject(str)
        println(json)
        val nonce = json.getString("nonce")
        val timestamp = json.getLong("timestamp")
        val token = json.getString("token")
        val data = json.getJsonArray("data")
        createSign(nonce, timestamp, token, data)


    }


    fun createSign(nonce: String, timestamp: Long, token: String, data: JsonArray) {
        val body = JsonObject()
                .put("nonce", nonce)
                .put("timestamp", timestamp)
                .put("token", token)
                .put("data", data)

        val map = mutableMapOf<String, Any>()
        map["nonce"] = nonce
        map["timestamp"] = timestamp
        map["token"] = token
        map["data"] = sortJson(data)
        map["salt"] = SALT
        val str = map.toSortedMap().map { e ->
            "${e.key}=${e.value}"
        }.joinToString("&")
        val sign = DigestUtils.md5Hex(DigestUtils.md5Hex(str))
        val dataStr = "dataStr:\r\n$str"
        val bodyStr = "body:\r\n$body"
        val signStr = "sign:\r\n$sign"

        println(dataStr)
        println(bodyStr)
        println(signStr)

    }

    @Test
    fun testString() {
//        println(" ".isNullOrBlank())
//        println(" ".isNullOrEmpty())
//        println("{\"\$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"UserImportMode\",\"description\":\"商城导入坐席/商铺时请求参数校验\",\"type\":\"object\",\"properties\":{\"key\":{\"type\":\"string\",\"enum\":[\"aW8uaXRzLmNvbW11bmljYXRpb24ubW9kdWxlcy5hcGkua\"]},\"data\":{\"type\":\"array\",\"minItems\":1,\"items\":{\"type\":\"object\",\"required\":[\"mass_name\",\"mass_id\",\"account_id\",\"real_name\"],\"properties\":{\"account_id\":{\"type\":\"string\",\"minLength\":1},\"real_name\":{\"type\":\"string\",\"minLength\":1},\"mass_id\":{\"type\":\"string\",\"minLength\":1},\"mass_name\":{\"type\":\"string\",\"minLength\":1}}}}},\"required\":[\"key\",\"data\"]}")
        val str = "data=[{\"account_id\":\"zuoxi-id-05\",\"mass_id\":\"zpj-massid-05\",\"mass_name\":\"ZPJ商铺05\",\"real_name\":\"zuoxi-name-05\"},{\"account_id\":\"zuoxi-id-06\",\"mass_id\":\"zpj-massid-06\",\"mass_name\":\"ZPJ商铺06\",\"real_name\":\"zuoxi-name-06\"},{\"account_id\":\"zuoxi-name-07\",\"mass_id\":\"zpj-massid-07\",\"mass_name\":\"ZPJ商铺07\",\"real_name\":\"zuoxi-name-07\"}]&nonce=iSmoHskmSH&salt=aW8uaXRzLmNvbW11bmljYXRpb24ubW9kdWxlcy5hcGkua&timestamp=1563363323180&token=yZUDbuhPAXtttOAewAuE"
        val sign = DigestUtils.md5Hex(DigestUtils.md5Hex(str))
        println(sign)
    }


    @Test
    fun createSign() {
        val str = "123"
        val str2 = "213"
        val sign = DigestUtils.md5Hex(DigestUtils.md5Hex(str))
        val sign2 = DigestUtils.md5Hex(DigestUtils.md5Hex(str2))
        println(sign)
        println(sign2)
    }
}