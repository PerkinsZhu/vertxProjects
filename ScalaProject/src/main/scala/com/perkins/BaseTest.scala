package com.perkins

import org.junit.Test
import scala.collection.JavaConverters._

class BaseTest {

  @Test
  def BaseTest(): Unit = {
    println("hello scala".capitalize) //首字母大写
    println("hello scala".linesIterator.mkString(";"))
    println("hello scala".stripPrefix("=="))
    (1 to 10).asJava.parallelStream().forEach(println(_))
  }

}
