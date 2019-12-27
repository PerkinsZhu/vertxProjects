package com.perkins

import java.util.concurrent.TimeUnit

import org.junit.Test

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class BaseTest {

  @Test
  def BaseTest(): Unit = {
    println("hello scala".capitalize) //首字母大写
    println("hello scala".linesIterator.mkString(";"))
    println("hello scala".stripPrefix("=="))
    (1 to 10).asJava.parallelStream().forEach(println(_))
    var list = 1 to 10
    println(list.partition(_ % 3 == 0))
    "123-456".foreach(println(_))
  }

  implicit val StringToList: String => List[String] = _.split("-").toList


  @Test
  def testFuture(): Unit = {
    val f = Future {
      1 to 100
    }.map {
      _.foreach(println(_))
    }
    Await.result(f, Duration.apply(10, TimeUnit.SECONDS))
  }
}

object BaseTest {


}