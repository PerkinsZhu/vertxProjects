package com.perkins.mysql

import com.perkins.BaseApp
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import io.vertx.core.AsyncResult as AsyncResult1


object MySqlAPP : BaseApp() {

    @JvmStatic
    fun main(args: Array<String>) {
        val client = getClient()
        client.getConnection {
            if (it.succeeded()) {
                val connection = it.result()
                addStudent(connection)
                listStudent(connection)
            }
        }
    }

    private fun addStudent(connection: SQLConnection) {
        val query = " insert into student(name,age) values ('xiaohong',12);"
        val action = handle<UpdateResult>("添加studnet") {
            logger.info(it.toJson().toString())
        }
        connection.update(query, action)
    }

    private fun listStudent(connection: SQLConnection) {
        val query = "SELECT * FROM student"
        connection.query(query) {
            if (it.succeeded()) {
                val rs = it.result()
                rs.results.forEach { jr ->
                    println(jr)
                }
            }
        }
    }

    fun getClient(): JDBCClient {
        val vertx = Vertx.vertx()
        val config = JsonObject()
        config.put("url", "jdbc:mysql://localhost:3306/test?useUnicode=true&serverTimezone=UTC")
        config.put("driver_class", "com.mysql.cj.jdbc.Driver")
        config.put("user", "root")
        config.put("password", "123456")
        config.put("max_pool_size", 5)
        config.put("initial_pool_size", 3)

        val client = JDBCClient.createShared(vertx, config, "MyDBPool")
//        创建非共享数据源
//        val client = JDBCClient.createNonShared(vertx, config)
        return client
    }


}