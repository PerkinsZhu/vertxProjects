package com.perkins.vertxsession

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

class MyAuthProvider : AuthProvider {
    override fun authenticate(authInfo: JsonObject, resultHandler: Handler<AsyncResult<User>>) {
        println(authInfo) //这里是进行验证时传入的用户信息
        //TODO 调用数据库验证用户的密码是否正确
        //成功之后返回user
        val future: AsyncResult<User> = Future.succeededFuture(MyUser())
        resultHandler.handle(future)
    }

}

class MyUser : AbstractUser() {
    var count = 0
    override fun doIsPermitted(permission: String, resultHandler: Handler<AsyncResult<Boolean>>) {
        println("------验证权限----$permission")
        //返回true则有权限，返回false则无权限
        //这里可以从数据库中取出用户的权限配置来动态判断
        //这里 对同一个permission貌似做了缓存，只会调用一次
        count += 1
        resultHandler.handle(Future.succeededFuture(count % 2 == 0))
    }

    override fun setAuthProvider(authProvider: AuthProvider) {
        println(authProvider)
    }

    override fun principal(): JsonObject {
        //返回user的身份信息
        return JsonObject().put("name", "jack")
    }

}