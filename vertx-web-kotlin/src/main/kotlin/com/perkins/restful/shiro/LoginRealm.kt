package com.perkins.restful.shiro

import org.apache.shiro.authc.*
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection

class LoginRealm : AuthorizingRealm() {
    override fun doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo {
        val username = token.principal as String  //得到用户名
        val password = String(token.credentials as CharArray)//得到密码
        if (!mutableListOf("zhang", "lisi").contains(username)) {
            throw UnknownAccountException() //如果用户名错误
        }
        if ("123" != password) {
            throw IncorrectCredentialsException() //如果密码错误
        }
        //如果身份认证验证成功，返回一个AuthenticationInfo实现；
        return SimpleAuthenticationInfo(username, password, name)

    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo {
        println("shiro 授权管理...")
        //根据用户ID查询权限（permission），放入到Authorization里。
        val permissions = mutableSetOf<String>()
        val roles = mutableSetOf<String>()
        //在这里查询数据库设置role和permission
        if (principals.asList().contains("zhang")) {
            permissions.add("getusers")
            roles.add("admin")
        }
        val info = SimpleAuthorizationInfo()
        //根据用户ID查询角色（role），放入到Authorization里。
        info.roles = roles

        info.stringPermissions = permissions
        return info
    }

}