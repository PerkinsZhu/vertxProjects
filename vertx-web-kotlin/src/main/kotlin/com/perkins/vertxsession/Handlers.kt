package com.perkins.vertxsession

import io.vertx.core.Handler
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.Cookie
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

object Handlers {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun loginHandler(authProvider: AuthProvider) = Handler<RoutingContext> {
        val res = it.response()
        val req = it.request()
        val name = req.getParam("name")
        val password = req.getParam("password")

        it.addCookie(Cookie.cookie("name", name))
        val session = it.session()

        //通过 在session中设置参数来记录用户是否登录
        session.put("user", name)
        val user = session.get<String>("user")

        //通过authProvider 传入用户名和密码进行登录验证，验证成功之后回调user，然后把 user设置到context中即可。
        //在外面加拦截器判断是否有context中是否有user，如果没有则重定向到登录页面或拒绝
        //这里使用了authProvider来进行验证，也可以自己直接调用数据库来验证。使用它的目的是和其他框架进行整合
        authProvider.authenticate(JsonObject().put("userName", name).put("ps", password)) { ar ->
            if (ar.succeeded()) {
                it.setUser(ar.result())
                res.end("$user 登录成功")
            } else {
                res.end("$user 登录失败")
            }
        }
    }

    val getUsers = Handler<RoutingContext> {
        println(it.user())
        it.user().isAuthorized(it.request().path()) { ar ->
            if (ar.succeeded()) {
                it.response().end("users")
            } else {
                it.response().end("您无权限反问该接口")
            }
        }
    }

    val logout = Handler<RoutingContext> {
        it.clearUser()
        it.response().end("logout")
    }

    val loginFilter = Handler<RoutingContext> {
        val session = it.session()
        val name = session.get<String>("user")
        val isPass = it.user() != null
        if (!isPass) {
            it.response().end("请登录")
        } else {
            println(it.user().principal())
            it.next()
        }


    }


}