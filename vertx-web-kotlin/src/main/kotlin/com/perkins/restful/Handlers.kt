package com.perkins.restful

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.slf4j.LoggerFactory

object Handlers {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    val loginHandler = Handler<RoutingContext> {
        val res = it.response()
        val req = it.request()
        val name = req.getParam("name")
        val password = req.getParam("password")
        val subject = SecurityUtils.getSubject()
        val token = UsernamePasswordToken(name, password)
        try {
            subject.login(token)
        } catch (e: AuthenticationException) {
            e.printStackTrace()
            res.end("密码错误")
        }
        val session = subject.getSession(true)
        session.setAttribute("user", "123456")

        res.end("登录成功")
    }

    val getUsers = Handler<RoutingContext> {
        val sub = SecurityUtils.getSubject()
        val sesssion = sub.getSession(false)
        val user = sesssion.getAttribute("user")
        logger.info("$user")

        //可以在这里判断是否有权限进行拦截
        //或者在拦截器中拦截
        val a = sub.isPermitted("getusers")
        val b = sub.isPermitted("admin")

        if (a) {
            it.response().end("users")
        } else {
            it.response().end("您无权限操作")
        }
    }

    val logout = Handler<RoutingContext> {
        SecurityUtils.getSubject().logout() //会自动清楚该session
        it.response().end("logout")
    }

    val loginFilter = Handler<RoutingContext> {
        val sub = SecurityUtils.getSubject()
        val session = sub.getSession(false)
        if (session == null) {
            logger.warn("当前用户未登录")
            it.response().end("请登录")
        } else {
            logger.info("当前用户:${sub.principal}")
            it.next()
        }
    }


}