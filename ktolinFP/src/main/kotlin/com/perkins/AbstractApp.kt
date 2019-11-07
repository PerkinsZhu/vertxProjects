package com.perkins

import org.slf4j.LoggerFactory.getLogger

abstract class AbstractApp {
    val logger = getLogger(this.javaClass)
}