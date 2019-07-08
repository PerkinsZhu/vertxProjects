package com.perkins.jsonvalidate

import com.perkins.eventbus.handles.writeDataToFile
import io.vertx.kotlin.core.json.JsonArray
import io.vertx.kotlin.core.json.JsonObject
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import org.junit.Test
import java.io.File
import java.io.FileReader
import java.nio.file.Files

class MainApp {

    @Test
    fun testJsonSchema() {
        val str = FileReader(File("D:\\myProjects\\vertxProjects\\vertx-web-kotlin\\src\\main\\kotlin\\com\\perkins\\jsonvalidate\\format.json")).readText()
        val data = JsonArray(JsonObject().put("realName", "asdf").put("accountId", 2134).put("massId", "wer").put("massName", "serf"))
        val json = JsonObject().put("key", "123456").put("data", data)
        val a = JSONObject(JSONTokener(str))
        val schema = SchemaLoader.load(a)
        schema.validate(JSONObject(json.toString()))
        println("-------")

    }

    @Test
    fun testPath() {
        val path = MainApp::class.java.getResource("\\")
        println(path)

    }

}