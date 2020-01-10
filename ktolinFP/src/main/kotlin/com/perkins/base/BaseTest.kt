package com.perkins.base

import arrow.core.extensions.list.apply.ap
import arrow.core.extensions.list.foldable.foldLeft
import arrow.fx.extensions.io.concurrent.sleep
import arrow.fx.typeclasses.Duration
import com.sun.org.apache.xalan.internal.lib.ExsltMath.power
import org.junit.Test
import java.util.concurrent.TimeUnit
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import kotlin.concurrent.timer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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


    @Test
    fun testList() {
        val list: List<Int> = (1 until 10).toList()
        list.map { it + 10 }
        list.fold(0) { a, b -> a + b }
        list.foldLeft(0) { a, b -> a + b }
        list.flatMap { it.rangeTo(it + 10) }
        println(list.zipWithNext())

        // 注意下面的区别。返回结果加{}和不加{}的区别
        //a: (Int,Int) ->Int
        val a = { a: Int, b: Int -> a + b }
        //b:(Int,Int) ->() ->Int
        val b = { a: Int, b: Int -> { a + b } }

        asList(1, 2, 3, 4, 5)
        val aa = arrayOf(1, 2, 3)
        val newList = asList(-1, 0, *aa, 4) //通过 * 号可以把数组展开
    }


    //接受可变长度参数
    fun <T> asList(vararg ts: T): List<T> {
        val result = ArrayList<T>()
        for (t in ts) // ts is an Array
            result.add(t)
        return result
    }

    @Test
    fun testLoop() {
        loop@ for (i in 1..100) {
            println("i-$i")
            for (j in 1..100) {
                println("j-$j")
                if (i == j) {
                    println(i)
                    break@loop //如果不加标签，则break只能返回出内层循环，这里加了标签，则可以返回到标签指定的循环
                }
            }
        }
    }


    //内联函数会把函数体中的代码在编译的时候嵌入到调用处
    inline fun inlineFunc(prefix: String, action: () -> Unit) {
        println("call before $prefix")
        action()
        println("call after $prefix")
    }

    @Test
    fun testInline() {
        val a = ::inlineFunc
        inlineFunc("testInline") { println("=========") }
        val myTree = DefaultMutableTreeNode()
    }

    //通过reified，可以在使用泛型的时候，获取到泛型的真实类型
    inline fun <reified T> TreeNode.findParentOfType(): T? {
        var p = parent
        while (p != null && p !is T) {
            p = p.parent
        }
        return p as T?
    }


    @Test
    fun testRange() {
        for (i in 1..10) println(i)
        for (i in 1..10 step 2) println(i)
        for (i in 10 downTo 1) println(i)
        for (i in 10 downTo 1 step 2) println(i)
    }

    @Test
    fun testType() {
        println((-12).unaryPlus())
        println((-12).unaryMinus())
        println(false.not())
        12.inc()
        12.dec()
    }


    val nullableList: List<Int?> = listOf(1, 2, null, 4)
    // filterNotNull , 把允许为空的集合转换为非空元素集合类型
    val intList: List<Int> = nullableList.filterNotNull()


    @Test
    fun testForYield() {
        val seq = sequence {
            for (i in 1..5) {
                // 产生一个 i 的平方
                yield(i * i)
            }
            yieldAll(26..28)
        }
        println(seq.toList())
    }


    fun computeRunTime(action: (() -> Unit)?) {
        val startTime = System.currentTimeMillis()
        action?.invoke()
        println("the code run time is ${System.currentTimeMillis() - startTime}")
    }

    @Test
    fun testSequence() = computeRunTime {
        (0..10000000)
                .map { it + 1 }
                .filter { it % 2 == 0 }
                .count { it < 10 }
                .run {
                    println("by using list way, result is : $this")
                }
        (0..10000000)
                .asSequence() // sequence是惰性求值，可以提升效率
                .map { it + 1 }
                .filter { it % 2 == 0 }
                .count { it < 10 }
                .run {
                    println("by using sequences way, result is : $this")
                }
    }

    @Test
    fun testPartition() {
        (1..1000).asSequence().partition { it % 5 == 0 }
        val a = power(2.0, 4.0)
        println(a)
    }

    @Test
    fun testTimer() {
        timer("test", true, 1000L, 1000L) {
            println("-----")
        }
        sleep(Duration(100000L, TimeUnit.MINUTES))


    }


    @Test
    fun testLet() {
        val a = 12.let {
            "aaa"
        }
        val block = { a: Int -> a.toString() }
        12.let(block)
        val b = 12.let { a -> a.toString() }

        val user = User()
        val reuslt = user.apply {
            this.eat()
            say()
            he()
            toString() //apply 返回的是this
        }
        val c = with(12) {
            println(this)
            22
        }
        println(c)

        1222.run(::println)

        val aa = 122.also {
            println("----")
            "s"
        }
        println(aa)
        listOf(1, 2, 3).ap(listOf({ x: Int -> x + 10 }, { x: Int -> x * 2 }))
    }

    @Test
    fun testRun() {
    }


}

private typealias T = Any
private typealias R = Any

//函数式使用方式
class Divider : (Int, Int) -> Double {
    override fun invoke(numerator: Int, denominator: Int): Double =
            if (denominator == 0) {
                0.0
            } else {
                numerator.toDouble() / denominator
            }
}


class User {
    fun say() {
        println("name")
    }

    fun eat() {}
    fun he() {}

}