package com.perkins.kotlintest

import java.io.OutputStream
import java.nio.charset.Charset

//复合函数
//求f(g(x))的值
val add5={i:Int->i+5}
val mutilplyBy2 = {i:Int-> i*2}


//复合函数 扩展Function1的扩展方法 infix 中缀表达式
//Function1 传入1个参数的函数 P1 接收的参数类型 P2返回的参数类型
//扩展方法andThen接收一个 一个参数的函数 他的参数 是add5的返回值 再返回最终结果
//andThen左边的函数  Function1<P1,P2> 接收一个参数P1 返回结果P2
//andThen右边的函数 function:Function1<P2,R> 参数为左边函数的返回值P2 返回结果R
//聚合的结果返回函数Function1<P1,R> 是以P1作为参数 R做为结果的函数
//相当于P1,P2 聚合 P2,R 返回 P1,R
//f(g(x))  P1相当于x P2 相当于g(x)返回值 返回的结果Function1<P1,R> R相当于f(g(x)) 的返回值
//Function1<P1,P2> 相当于g(x)
//function:Function1<P2,R> 相当于x
//g(x).andThen(f(g(x)))
infix fun<P1,P2,R> Function1<P1,P2>.andThen(function:Function1<P2,R>):Function1<P1,R>{
    return fun(p1:P1):R{
        return function.invoke(this.invoke(p1))//先执行函数 p1返回p2 再执行 function(p2)返回R
    }
}
//compose左边函数接收参数P2 返回R
//compse右边函数 接收参数P1 返回P2
//返回结果函数P1,R
//相当于先执行右边返回了P1,P2  在执行P2,R函数 聚合成P1,R
//g(f(x))
//f(x).compose(g(f(x)))
infix fun<P1,P2,R> Function1<P2,R>.compose(function:Function1<P1,P2>):Function1<P1,R>{
    return fun(p1:P1):R{
        return this.invoke(function.invoke(p1))
    }
}
//复合函数
fun executeFuhe(){
    println(mutilplyBy2(add5(8)))//(5+8)*2
    val add5andThen = add5 andThen mutilplyBy2
    println(add5andThen(8))
    val add5ComposeThen = add5 compose  mutilplyBy2
    println(add5ComposeThen(8))//g(f(x)) 先*2 再+5
}

//函数的链式调用
//Currying 多元函数变成一元函数的调用链
fun hello(x:String,y:Int,z:Double):Boolean{
    return true
}
//多个参数的函数 演变成多个单参数函数的链式调用
//fun curriedHello(x:String):(y:Int)->(z:Double)->Boolean{
//}
fun log(tag:String,target:OutputStream,message:Any?){
    target.write("[$tag] $message\n".toByteArray())
}

fun log1(tag:String) = fun(target:OutputStream)=fun(message:Any?)=target.write("[$tag] $message\n".toByteArray())

//三个函数链式调用的扩展方法
fun <P1,P2,P3,R> Function3<P1,P2,P3,R>.curry() = fun(p1:P1)=fun(p2:P2)=fun(p3:P3) = this(p1,p2,p3)

fun curryExecute(){
    log("gac",System.out,"HelloWorld")
    log1("gacmy")(System.out)("hello worldAgain")
    ::log.curry()("gacmy")(System.out)("hello worldAgain") //不需要写链式调用方法 统一为模板方法
}

//偏函数
//
fun pianhanshu(){
    val consoleLog=::log.curry()("gac")(System.out)
    consoleLog("hello pioanhanshu")

    val makeString = fun(byteArray:ByteArray,charset:Charset):String{
        return String(byteArray,charset)
    }
    //偏函数
    val makeStringGBK = makeString.partial2(charset("GBK"))
    makeStringGBK("反反复复付".toByteArray())
}
//偏函数的模板方法 固定第二个参数 只需要 最后传入一个参数的函数
fun <P1,P2,R> Function2<P1,P2,R>.partial2(p2:P2) = fun(p1:P1) = this(p1,p2)

//偏函数的模板方法 固定第一个参数 只需要 最后传入一个参数的函数
fun <P1,P2,R> Function2<P1,P2,R>.partial(p1:P1) = fun(p2:P2) = this(p1,p2)


fun main(args: Array<String>) {
    executeFuhe()
    curryExecute()
    pianhanshu()

}