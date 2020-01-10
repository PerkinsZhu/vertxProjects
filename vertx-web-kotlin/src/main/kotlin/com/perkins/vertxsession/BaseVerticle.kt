package com.perkins.vertxsession

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BasicAuthHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.vertx.groovy.ext.sql.SQLRowStream_GroovyExtension.handler
import io.vertx.groovy.ext.auth.User_GroovyExtension.principal


class BaseVerticle : AbstractVerticle() {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun start() {
        val router = Router.router(vertx)
        loadRouter(router)
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(8091) { result ->
                    if (result.succeeded()) {
                        println("------success--")
                    } else {
                        println("------failure--")
                    }
                }
    }

    private fun loadRouter(router: Router) {
        router.route().handler(CookieHandler.create())
        val store = LocalSessionStore.create(vertx, "myapp3.sessionmap", 100000)
        val sessionHandler = SessionHandler.create(store)
        router.route().handler(sessionHandler)
        val authProvider = MyAuthProvider()
        router.route().handler(UserSessionHandler.create(authProvider));


        val basicAuthHandler = BasicAuthHandler.create(authProvider);

        router.get("/login").handler(Handlers.loginHandler(authProvider))
//        router.get("/login").handler(basicAuthHandler)
        router.route("/*").handler(Handlers.loginFilter) // 拦截后面的请求必须登录，未登录则不放行
        router.get("/getuser").handler(Handlers.getUsers)
        router.get("/logout").handler(Handlers.logout)
    }
}