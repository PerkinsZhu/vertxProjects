package com.perkins.common

import java.io.BufferedReader
import java.io.FileReader
import java.util.*

class PropertiesUtil {
    companion object {
        val properties = Properties();
        val filePath = ""

        init {
            val bufferedReader = BufferedReader(FileReader("D:\\zhupingjing\\testFile\\testConfig\\config.properties"));
            properties.load(bufferedReader);
        }

        fun get(key: String): String {
            return properties.getProperty(key)
        }
    }

}