package com.perkins.handlers

import io.vertx.core.logging.LoggerFactory

open class  AbstractHandle{
    val logger = LoggerFactory.getLogger(this.javaClass)
}