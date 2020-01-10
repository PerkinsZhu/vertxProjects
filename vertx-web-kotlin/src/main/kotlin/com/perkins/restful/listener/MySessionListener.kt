package com.perkins.restful.listener

import org.apache.shiro.session.Session
import org.apache.shiro.session.SessionListener
import org.slf4j.LoggerFactory

class MySessionListener : SessionListener {
    val logger = LoggerFactory.getLogger(this.javaClass)
    override fun onExpiration(session: Session) {
        logger.info("登录过期")
    }

    override fun onStart(session: Session?) {
        logger.info("用户登录")
    }

    override fun onStop(session: Session?) {
        logger.info("用户登出")
    }

}