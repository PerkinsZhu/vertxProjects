package com.perkins.eventbus

import com.perkins.eventbus.handles.BaseHandle
import io.vertx.core.AbstractVerticle

class BaseVerticle : AbstractVerticle() {


    override fun start() {
        val baseHandle = BaseHandle(vertx)


    }
}