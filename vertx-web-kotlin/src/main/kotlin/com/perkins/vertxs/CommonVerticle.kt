package com.perkins.vertxs

import com.perkins.handlers.BaseHandle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.KeyStoreOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.auth.jwt.impl.JWTAuthProviderImpl
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.JWTAuthHandler


class CommonVerticle : AbstractVerticle() {
    override fun start(startFuture: Future<Void>) {
        val router: Router = createRouter()
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"))
        router.route().handler(BodyHandler.create())
        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(config().getInteger("http.port", 8082)) { result ->
                    if (result.succeeded()) {
                        startFuture.complete()
                    } else {
                        startFuture.fail(result.cause())
                    }
                }
    }


    private fun createRouter() = Router.router(vertx).apply {
        val sessionStore = LocalSessionStore.create(vertx, "sessionName")


        val option = JWTAuthOptions()
        option.keyStore = KeyStoreOptions().setPassword("secret").setPath("keystore.jceks").setType("jceks")
        val provider = JWTAuthProviderImpl(vertx, option)
        val jwtHandler: Handler<RoutingContext> = JWTAuthHandler.create(provider)



        route().failureHandler(BaseHandle.failure)
        //        route().handler(BodyHandler.create())
        get("/").handler(BaseHandle.indexHandle)
        post("/json").handler(BaseHandle.jsonHandle)
        post("/uploadFile").handler(BaseHandle.uploadFile)
        get("/redirect/:key").handler(BaseHandle.redirectHandle)
        get("/path1/:key/bucketName").handler(BaseHandle.path1)
        get("/path2/:key").handler(BaseHandle.path2)

        // 对于该路径添加session管理
        get("/session/*").handler(CookieHandler.create())
        get("/session/*").handler(SessionHandler.create(sessionStore))
        get("/session/set").handler(BaseHandle.setSession)
        get("/session/get").handler(BaseHandle.getSession)

        //测试JWT 实现
        route("/jwt/*").handler(jwtHandler)
        get("/login").handler(BaseHandle.login(provider))
        get("/jwt/getUser").handler(BaseHandle.getUser)

    }

}