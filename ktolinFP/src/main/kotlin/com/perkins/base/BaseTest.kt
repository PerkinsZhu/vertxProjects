package com.perkins.base

import org.junit.Test

class BaseTest {

    @Test
    fun testSealed() {
        /*fun eval(expr: Expr): Double = when (expr) {
            is Const -> expr.number
            is Sum -> eval(expr.el) + eval(expr.e2)
            NotANumber -> Double.NaN
        }*/
    }


    fun safeDivide(numerator: Int, denominator: Int) = if (denominator == 0) 0.0 else numerator.toDouble() / denominator
    val f: (Int, Int) -> Double = ::safeDivide //通过 ::可以把方法转换为函数

    @Test
    fun testFunction() {
        val quotient = f(3, 0)
        println(quotient)
    }

    val safeDivide = { numerator: Int, denominator: Int ->
        if (denominator == 0) 0.0 else numerator.toDouble() / denominator
    }



}

//函数式使用方式
class Divider : (Int, Int) -> Double {
    override fun invoke(numerator: Int, denominator: Int): Double =
            if (denominator == 0) {
                0.0
            } else {
                numerator.toDouble() / denominator
            }
}


