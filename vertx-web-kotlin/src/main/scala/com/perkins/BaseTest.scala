package com.perkins

import org.junit.Test

class BaseTest {
  def main(args: Array[String]): Unit = {
    println("Hello SCALA")
  }


  @Test
  def createStr(): Unit ={
    import org.apache.commons.lang3.RandomStringUtils
    println(RandomStringUtils.randomAlphanumeric(56))
  }

}
