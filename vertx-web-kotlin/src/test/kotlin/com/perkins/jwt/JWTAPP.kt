package com.perkins.jwt

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.KeyStoreOptions
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.auth.jwt.impl.JWTAuthProviderImpl
import io.vertx.ext.jwt.JWTOptions
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import com.mchange.v2.c3p0.impl.C3P0Defaults.user
import com.perkins.mongodb.MongClientUtil
import io.vertx.ext.auth.User
import io.vertx.ext.auth.mongo.HashSaltStyle
import io.vertx.ext.auth.mongo.HashStrategy
import io.vertx.ext.auth.mongo.MongoAuth
import io.vertx.ext.auth.mongo.impl.DefaultHashStrategy
import org.junit.After
import com.mchange.v2.c3p0.impl.C3P0Defaults.user


class JWTAPP {
    val logger = LoggerFactory.getLogger(this.javaClass)
    lateinit var vertx: Vertx

    @Before
    fun init() {
        vertx = Vertx.vertx()
    }

    @Test
    fun testJwt() {
        val option = JWTAuthOptions()
        option.keyStore = KeyStoreOptions().setPassword("secret").setPath("keystore.jceks").setType("jceks")
        val authProvider = JWTAuthProviderImpl(vertx, option)
        val token = authProvider.generateToken(
                JsonObject().put("name", "jack").put("phone", "15888888888")
                , JWTOptions())
        println(token)

        val authInfo = JsonObject().put("jwt", token)
        authProvider.authenticate(authInfo) { res ->
            if (res.succeeded()) {
                val user = res.result()
                logger.info("User " + user.principal() + " is now authenticated")
                user.isAuthorized("printers:printer1234") { res ->
                    if (res.succeeded()) {
                        val hasAuthority = res.result()
                        if (hasAuthority) {
                            println("User has the authority")
                        } else {
                            println("User does not have the authority")
                        }
                    } else {
                        res.cause().printStackTrace()
                    }
                }

            } else {
                res.cause().printStackTrace()
            }
        }
    }

    @Test
    fun testMongodbAuth() {
        val client = MongClientUtil.client
        val authProperties = JsonObject()

        val strategy = DefaultHashStrategy()
        strategy.saltStyle = HashSaltStyle.NO_SALT  // 这里设置密码不加密，也可以设置其他加密方式

        val authProvider = MongoAuth.create(client.value, authProperties)
                .setCollectionName("user") //设置collection
                .setUsernameField("username") // 设置用户名字段
                .setPasswordField("password") //设置密码字段
                .setHashStrategy(strategy)
                .setUsernameCredentialField("username")
                .setPasswordCredentialField("password")

        val authInfo = JsonObject()
                .put("username", "tim")
                .put("password", "sausages")

        authProvider.authenticate(authInfo) {
            if (it.succeeded()) {
                val user = it.result();
                logger.info(user.toString())
                //TODO  这里授权是怎么判断的？
                user.isAuthorized("manager") { res ->
                    if (res.succeeded()) {
                        val hasPermission = res.result()
                        logger.info("hasPermission-->$hasPermission")
                    } else {
                        // Failed to
                    }
                }
            } else {
                it.cause().printStackTrace()
            }
        }
    }

    @After
    fun sleep() {
        Thread.sleep(5000)
    }
}