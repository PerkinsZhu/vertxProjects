package com.perkins.shiro

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.config.IniSecurityManagerFactory
import org.junit.Assert
import org.junit.Test


class MainApp {

    @Test
    fun testHelloworld() {
        //1、获取SecurityManager工厂，此处使用Ini配置文件初始化SecurityManager  Factory<org.apache.shiro.mgt.SecurityManager> factory =


//        val factory = IniSecurityManagerFactory("classpath:shiro.ini")
        //通过 realm 配置来验证身份
        val factory = IniSecurityManagerFactory("classpath:shiro-realm.ini")


        //2、得到SecurityManager实例 并绑定给SecurityUtils   org.apache.shiro.mgt.SecurityManager
        val securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager)
        //3、得到Subject及创建用户名/密码身份验证Token（即用户身份/凭证）
        val subject = SecurityUtils.getSubject()
        val token = UsernamePasswordToken("zhang", "123")
        try {
            //4、登录，即身份验证
            subject.login(token)
            val subject = SecurityUtils.getSubject()
            val session = subject.getSession(true)
            println(session.host)
            if (subject.hasRole("admin")) {
                //有权限
                println("有权限")
            } else {
                //无权限
                println("无权限")
            }

        } catch (e: AuthenticationException) {
            e.printStackTrace()
            //5、身份验证失败
        }

        Assert.assertEquals(true, subject.isAuthenticated()) //断言用户已经登录
        //6、退出
        subject.logout()
    }
}