package com.perkins.eventbus.controller;

import io.vertx.rxjava.ext.web.Router;

class BaseController constructor(override val router: Router) : Controller({
    route()

}) {}