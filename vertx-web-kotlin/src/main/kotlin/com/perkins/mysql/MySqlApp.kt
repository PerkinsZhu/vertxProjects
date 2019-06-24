package com.perkins.mysql

import com.perkins.BaseApp
import io.vertx.core.json.JsonObject
import io.vertx.rxjava.core.Vertx
import io.vertx.rxjava.ext.asyncsql.AsyncSQLClient
import io.vertx.rxjava.ext.asyncsql.MySQLClient
import org.junit.After
import org.junit.Test
import rx.Observable


class MySqlApp : BaseApp() {

    @Test
    fun testGetUser() {
        val client = getClient()
        /*client.query("SELECT * FROM student") { ar ->
            if (ar.succeeded()) {
                if (ar.succeeded()) {
                    val result = ar.result();
                    val rowSize = result.numRows
                    for (i in 0..rowSize) {
                        val item = result.next
                        println(item.rows)
                    }
                } else {
                    ar.cause().printStackTrace()
                }
            }
        }*/


        client.getConnection { res ->
            if (res.succeeded()) {
                val connection = res.result()
                connection.query("select * from student") {
                    val rs = it.result()
                    println(rs.numRows)
                    rs.results.forEach { item ->
                        val id = item.getInteger(0)
                        println(id)
                    }
                }
            } else {
                res.cause().printStackTrace()
            }
        }
    }

    fun getClient(): AsyncSQLClient {
        val vertx = Vertx.vertx()
        val config = JsonObject()
        config.put("host", "localhost")
        config.put("port", 3308)
        config.put("user", "vertx") //TODO 这里设置的用户名好像没有生效，使用的是vertx 用户登录的
        config.put("password", "123456")
        config.put("database", "test")
        config.put("charset", "UTF-8")
        config.put("maxPoolSize", 5)
        config.put("sslMode", "disable")

        val client = MySQLClient.createShared(vertx, config, "MySQLPool1");
        return client
    }


    @After
    fun testEnd() {
        Thread.sleep(10000)
    }


    @Test
    fun testAddUser() {
        val client = getClient()
        /*client.getConnection { res ->
            if (res.succeeded()) {
                val connection = res.result()
                val update = "update student set name = 'aaaaa'  where id = 6"
                connection.update(update) {
                    val rs = it.result()
                    println(rs.toJson())
                    println(rs.keys)
                    println(rs.updated)
                }
            } else {
                res.cause().printStackTrace()
            }
        }*/

        client.rxGetConnection().flatMap { col ->
            val update = "insert into student(`name`,`age`) values('dddd',230)"
            col.rxUpdate(update).doAfterTerminate(col::close)
        }.subscribe({
            println(it.keys) // 返回的id
            println(it.updated)
            println(it.toJson())
        }, {
            it.printStackTrace()
        })
    }

    @Test
    fun testGet() {
        val client = getClient()
        client.rxGetConnection().flatMap { col ->
            val query = "SELECT id FROM user WHERE account_id = 'asdfasdf' AND realname = 212121"
            col.rxQuery(query)
                    .doAfterTerminate(col::close)
                    .map { rs ->
                        val data = rs.toJson()
                        data
            }
        }.subscribe({
            println(it)
        },{
            it.printStackTrace()
        })
    }

}