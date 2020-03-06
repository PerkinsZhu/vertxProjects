package com.perkins.sign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.junit.Test;

public class TestDemo {

    @Test
    public void testSing(){
        JSON array = getDataArray();
        JSON signJson = SignUtilDemo.jsonSort(array);
        System.out.println(signJson);
    }


    static JSONArray getDataArray() {
        String str = "[\"1231\",\"1231\",\"1231\"]";
        return JSON.parseArray(str);
    }
}
