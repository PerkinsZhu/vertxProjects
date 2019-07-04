package com.perkins

import io.vertx.scala.core.Vertx

object MainApp {

  private val vertx = Vertx.vertx()

  def deployVerticle(): Unit = {
    val verticle = new ServerVerticle()
    vertx.deployVerticle(verticle)
  }

  def main(args: Array[String]): Unit = {
    //        httpServer()
    deployVerticle()
  }


  def httpServer(): Unit = {
    val server = vertx.createHttpServer()
    server.requestHandler((request: io.vertx.scala.core.http.HttpServerRequest) => {
      val response = request.response()
      response.putHeader("content-type", "text/plain")
      response.end("Hello World! I am scala vertx!")
    })
    server.listen(8080)
  }

}
