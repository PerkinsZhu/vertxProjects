package com.perkins.mysql

import com.perkins.BaseApp
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.JsonArray
import io.vertx.rxjava.core.Vertx
import io.vertx.rxjava.ext.asyncsql.AsyncSQLClient
import io.vertx.rxjava.ext.asyncsql.MySQLClient
import org.junit.After
import org.junit.Test
import rx.Observable
import rx.Observer
import rx.Single
import java.sql.Connection
import java.sql.DriverManager


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
        Thread.sleep(5000)
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
        }, {
            it.printStackTrace()
        })
    }

    @Test
    fun testParam() {
        val sql = "SELECT id FROM user WHERE account_id =? AND realname =?"
        val params = JsonArray()
        params.add("test01")
        params.add("test01")
        execute {
            //            it.rxQuery(sql)
            it.rxQueryWithParams(sql, params)
        }.map {
            it.rows.forEach {
                println(it)
            }
        }.subscribe()
    }

    @Test
    fun testInsertNull() {
        var uuidStr = "_userasdlkfjwifndfogvhddlfgjk"
        val sql = "INSERT INTO `user` (`uuid`, `account`, `account_id`, `realname`, `platform`, `locked`, `ismasking`,`isoutsource`, `isimport`, `created_at`, `created_by`)" +
                "\tVALUES (?,?,?,?,?,?,?,?,?,?,?)"
        logger.debug("addDefaultUserSQL:$sql")
        val params = JsonArray().add(uuidStr).add("accountId").add("accountId").add("realname").add(16).add(0).add(0)
                .add(0).add(1).add(System.currentTimeMillis()).add(1)

        execute {
            it.rxUpdateWithParams(sql, params)
        }.map {
            it.keys.forEach {
                println(it)
            }
        }.subscribe({

        }, {
            it.printStackTrace()
        })
    }


    @Test
    fun testInsertNull2() {
        var uuidStr = "_userasdlkfjwifndfogvhddlfgjk"
        val sql = "INSERT INTO `user` (`uuid`, `account`, `account_id`, `realname`, `platform`, `locked`, `ismasking`,`isoutsource`, `isimport`, `created_at`, `created_by`,`updated_by`)" +
                "\tVALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
        logger.debug("addDefaultUserSQL:$sql")
        val params = JsonArray().add(uuidStr).add("accountId").add("accountId").add("realname").add(16).add(0).add(0)
                .add(0).add(1).add(System.currentTimeMillis()).add(1).addNull()

        execute {
            it.rxUpdateWithParams(sql, params)
        }.map {
            it.keys.forEach {
                println(it)
            }
        }.subscribe({

        }, {
            it.printStackTrace()
        })
    }

    private fun <T> execute(sqlStatement: (io.vertx.rxjava.ext.sql.SQLConnection) -> Single<T>): Single<T> {
        return getClient().rxGetConnection().flatMap {
            sqlStatement(it).doAfterTerminate(it::close)
        }
    }


    @Test
    fun testTrancation() {
        val connection = getJDBCCollecion()
        Single.just(connection).map {
            it.prepareStatement("update user set name = 'jack' where id = 1").executeUpdate()
            it
        }.map {
            it.prepareStatement("update user set name = 'jack' where id = 1").executeUpdate()
            it
        }.map {
            it.prepareStatement("update user set name = 'jack' where id = 1").executeUpdate()
            it
        }.subscribe({
            connection.commit()
        }, {
            connection.rollback()
        })
    }


    fun getJDBCCollecion(): Connection {
        Class.forName("com.mysql.cj.jdbc.Driver")
        return DriverManager.getConnection("jdbc:mysql://localhost:3308/test")
    }
}