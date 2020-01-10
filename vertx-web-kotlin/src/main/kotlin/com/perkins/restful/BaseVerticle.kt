package com.perkins.restful

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import org.apache.shiro.SecurityUtils
import org.apache.shiro.config.IniSecurityManagerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BaseVerticle : AbstractVerticle() {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val router = Router.router(vertx)
        loadRouter(router)
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(8091) { result ->
                    if (result.succeeded()) {
                        initShiro()
                        println("------success--")
                    } else {
                        println("------failure--")
                    }
                }
    }

    private fun loadRouter(router: Router) {
        router.get("/login").handler(Handlers.loginHandler)
        router.route("/*").handler(Handlers.loginFilter) // 拦截后面的请求必须登录，未登录则不放行
        router.get("/getuser").handler(Handlers.getUsers)
        router.get("/logout").handler(Handlers.logout)
    }

    private fun initShiro() {
        val factory = IniSecurityManagerFactory("classpath:shiro-realm.ini")
        val securityManager = factory.instance;
        SecurityUtils.setSecurityManager(securityManager)
    }
}