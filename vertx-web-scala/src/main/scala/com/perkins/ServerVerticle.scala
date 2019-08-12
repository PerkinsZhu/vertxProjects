package com.perkins

import io.vertx.core.Handler
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.{HttpServerOptions, HttpServerRequest}
import io.vertx.scala.ext.web.{Router, RoutingContext}

class ServerVerticle extends ScalaVerticle {
  override def start(): Unit = {
    val router = createRouter

    val handler = new Handler[HttpServerRequest] {
      override def handle(e: HttpServerRequest): Unit = {
        router.accept(e)
      }
    }

    val server = vertx.createHttpServer()
    server.requestHandler(handler)
      .listen(8080)

    super.start()
  }

  private def createRouter: Router = {
    val router = Router.router(vertx)

    router.route("/hello").handler((routingContext: RoutingContext) => {
      routingContext.response().end("Hello World from Vert.x-Web!")
    })

    router.route("/test").handler((routingContext: RoutingContext) => {
      val response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("Hello World from Vert.x-Web!")
    })


    router
  }
}
