package com.perkins.sign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.perkins.common.PropertiesUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TreeMap;


public class SignUtilDemo {

    public static void main(String[] args) {

        createSing();
    }

    public static void createSing() {
        String SALT = PropertiesUtil.Companion.get("salt");

        String nonce = RandomStringUtils.randomAlphabetic(10);
        Long timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        String token = RandomStringUtils.randomAlphabetic(20);

        JSONArray array = getDataArray();

        JSONObject body = new JSONObject();
        body.put("nonce", nonce);
        body.put("timestamp", timestamp);
        body.put("token", token);
        body.put("data", array);

        // 计算签名
        //对JSON 进行排序
        JSON signJson = jsonSort(array);
        //通过TreeMap 对五个参数进行排序
        TreeMap map = new TreeMap<String, Object>();
        map.put("nonce", nonce);
        map.put("timestamp", timestamp);
        map.put("token", token);
        map.put("salt", SALT);
        map.put("data", signJson);

        // 拼接参数 key=value
        StringBuffer stringBuffer = new StringBuffer();
        map.forEach((Object a, Object b) -> {
            stringBuffer.append(a + "=" + b.toString() + "&");
        });
        String str = stringBuffer.toString().substring(0, stringBuffer.length() - 1);
        System.out.println(str);
        String sign = DigestUtils.md5Hex(DigestUtils.md5Hex(str));
        System.out.println("\r\n" + body);
        System.out.println("\r\n" + sign);
    }

    static JSONArray getDataArray() {
        String str = "[\n" +
                "        {\n" +
                "            \"mass_id\": \"zpj-massid-05\",\n" +
                "            \"real_name\": \"zuoxi-name-05\",\n" +
                "            \"account_id\": \"zuoxi-id-05\",\n" +
                "            \"mass_name\": \"ZPJ商铺05\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"mass_id\": \"zpj-massid-06\",\n" +
                "            \"account_id\": \"zuoxi-id-06\",\n" +
                "            \"real_name\": \"zuoxi-name-06\",\n" +
                "            \"mass_name\": \"ZPJ商铺06\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"mass_id\": \"zpj-massid-07\",\n" +
                "            \"account_id\": \"zuoxi-name-07\",\n" +
                "            \"real_name\": \"zuoxi-name-07\",\n" +
                "            \"mass_name\": \"ZPJ商铺07\"\n" +
                "        }\n" +
                "    ]";
        return JSON.parseArray(str);
    }

    /**
     * 对json 对象进行排序，排序规则：
     * JsonArray中的每一项顺序保持不变
     * JsonObject 中，根据 key从小到大进行排序
     *
     * @param json
     * @return
     */
    public static JSON jsonSort(JSON json) {
        if (json instanceof JSONObject) {
            TreeMap temp = new TreeMap();
            ((JSONObject) json).forEach((Object key, Object value) -> {
                if (value instanceof JSON) {
                    JSON tempData = jsonSort((JSON) value);
                    temp.put(key.toString(), tempData);
                } else {
                    temp.put(key.toString(), value);
                }
            });
            return (JSON) JSONObject.toJSON(temp);
        } else if (json instanceof com.alibaba.fastjson.JSONArray) {
            JSONArray array = new JSONArray();
            ((JSONArray) json).forEach((Object o) -> {
                array.add(jsonSort((JSON) o));
            });
            return array;
        } else {
            return json;
        }
    }


}
