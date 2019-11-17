package com.perkins.arrow

import arrow.aql.extensions.list.union.union
import arrow.aql.extensions.list.sum.*

import arrow.Kind
import arrow.aql.extensions.sequence.select.query
import arrow.core.*
import arrow.core.Option
import arrow.core.extensions.fx
import arrow.extension
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.unsafeRun.runBlocking
import arrow.fx.extensions.io.unsafeRun.unsafeRun
import arrow.fx.typeclasses.Concurrent
import arrow.fx.typeclasses.UnsafeRun
import arrow.typeclasses.Eq
import arrow.unsafe
import com.perkins.AbstractApp
import kotlinx.coroutines.*
import org.junit.Test
import arrow.core.extensions.option.apply.map
import arrow.core.extensions.list.traverse.sequence
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.`try`.apply.map
import arrow.core.extensions.eq
import arrow.core.extensions.list.functor.map
import arrow.core.extensions.list.semigroupK.combineK
import arrow.core.extensions.list.traverse.traverse
import arrow.core.extensions.show
import arrow.typeclasses.Show
import java.util.*
import arrow.core.extensions.nonemptylist.apply.map
import arrow.core.*
import arrow.core.extensions.list.apply.ap
import arrow.core.extensions.option.applicative.*
import arrow.core.extensions.list.traverse.*
import arrow.core.extensions.option.monad.monad
import arrow.mtl.Kleisli
import arrow.mtl.fix
import arrow.ui.*
import arrow.aql.extensions.list.select.*
import arrow.aql.extensions.list.where.where
import arrow.aql.extensions.listk.select.select
import arrow.aql.extensions.sequencek.select.select as ss
import arrow.aql.extensions.list.groupBy.*
import arrow.aql.extensions.listk.select.selectAll
import arrow.aql.extensions.list.orderBy.*
import arrow.aql.extensions.listk.select.select
import arrow.core.extensions.order
import arrow.aql.Ord
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.applicative.map
import arrow.fx.extensions.io.applicativeError.attempt
import arrow.fx.extensions.io.concurrent.sleep
import arrow.fx.extensions.io.effect.runAsync
import arrow.fx.extensions.io.monadDefer.monadDefer
import arrow.fx.typeclasses.milliseconds
import arrow.fx.typeclasses.seconds
import arrow.syntax.function.pipeLazy
import io.reactivex.rxkotlin.toObservable


class ArrowApp : AbstractApp() {
    @Test
    fun testBase() {
        println("-----")
    }

    suspend fun sayHello(): Unit =
            println("Hello World")

    suspend fun sayGoodBye(): Unit =
            println("Good bye World!")

    @Test
    fun FPTest() {
        println(greet())
        println(greet2())
        Thread.sleep(2000)

    }


    fun greet(): IO<Unit> =
            IO.fx {
                val pureHello = effect { sayHello() }
                val pureGoodBye = effect { sayGoodBye() }
            }

    fun greet2(): IO<Unit> =
            IO.fx {
                !effect { sayHello() }
                !effect { sayGoodBye() }
            }

    @Test
    fun main() { // The edge of our world
        unsafe { runBlocking { greet2() } }
//        Thread.sleep(2000)
        println(greet3()) //greet is a pure IO program
    }

    fun sayInIO(s: String): IO<Unit> =
            IO { println(s) }

    fun greet3(): IO<Unit> =
            IO.fx {
                sayInIO("Hello World").bind()
            }

    val contextA = newSingleThreadContext("A")

    suspend fun printThreadName(): Unit =
            logger.info(Thread.currentThread().name)

    val program = IO.fx {
        logger.info("------")
        continueOn(contextA) // 执行执行的线程
        !effect { printThreadName() }
        continueOn(dispatchers().default())
        !effect { printThreadName() }
    }

    @Test
    fun main2() { // The edge of our world
        unsafe { runBlocking { program } }
        Thread.sleep(2000)
    }

    suspend fun threadName(): String = Thread.currentThread().name
    val program3 = IO.fx {
        val fiberA = !effect {
            logger.info("start fiber A")
            Thread.sleep(5000)
            threadName()
        }.map {
            23
        }.fork(dispatchers().default())
        val fiberB = !effect { threadName() }.fork(dispatchers().default())
        val threadA = !fiberA.join()
        val threadB = !fiberB.join()
        !effect { logger.info(threadA.toString()) }
        !effect { logger.info(threadB) }
    }

    @Test
    fun main3() { // The edge of our world
        unsafe { runBlocking { program3 } }
    }


    data class ThreadInfo(
            val threadA: String,
            val threadB: String
    )

    val program4 = IO.fx {
        val (threadA: String, threadB: String) =
                !dispatchers().default().parMapN(
                        effect { threadName() },
                        effect { threadName() },
                        ::ThreadInfo
                )
        !effect { println(threadA) }
        !effect { println(threadB) }
    }

    @Test
    fun main4() { // The edge of our world
        unsafe { runBlocking { program4 } }
    }


    suspend fun threadName(i: Int): String =
            "$i on ${Thread.currentThread().name}"

    val program5 = IO.fx {
        val result: List<String> = !listOf(1, 2, 3).parTraverse { i -> effect { threadName(i) } }
        val result2: List<String> = listOf(1, 2, 3).parTraverse { i -> effect { threadName(i) } }.bind()

        !effect { result.forEach { logger.info(it) } }

    }

    @Test
    fun main5() { // The edge of our world
        unsafe { runBlocking { program5 } }
    }


    val program6 = IO.fx {
        val result: List<String> = !listOf(
                effect { threadName() },
                effect { threadName() },
                effect { threadName() }
        ).parSequence()
        !effect { println(result) }
    }

    @Test
    fun main6() { // The edge of our world
        unsafe { runBlocking { program6 } }
    }

    suspend fun program() = GlobalScope.async { printThreadName() }

    @Test
    fun main7() {
        runBlocking { program().await() }
    }

    suspend fun program7() = GlobalScope.async(start = CoroutineStart.LAZY) { printThreadName() }

    @Test
    fun main8() {
        runBlocking { program7().await() }
    }

    /* a side effect */
    val const = 1

    suspend fun sideEffect(): Int {
        println(Thread.currentThread().name)
        return const
    }

    /* for all `F` that provide an `Fx` extension define a program function */
    fun <F> Concurrent<F>.program(): Kind<F, Int> = fx.concurrent { !effect { sideEffect() } }

    /* for all `F` that provide an `UnsafeRun` extension define a main function */
    fun <F> UnsafeRun<F>.main(fx: Concurrent<F>): Int = unsafe { runBlocking { fx.program() } }

    /* Run program in the IO monad */
    @Test
    fun main9() {
        IO.unsafeRun().main(IO.concurrent())
    }


    @Test
    fun testOption() {
        val option = Option.fx {
            val (a) = Option(1)
            val (b) = Option(a + 1)
            a + b
        }
        logger.info(option.toString())
        val map = map(Option(1), Option(2), Option(3)) { (one, two, three) ->
            one + two + three
        }
        logger.info(map.toString())
        val seq = listOf(Option(1), Option(2), Option(3)).sequence(Option.applicative())
        logger.info(seq.toString())

        val tryTest = Try.fx {
            val (a) = Try { 1 }
            val (b) = Try { a + 1 }
            a + b
        }
        logger.info(tryTest.toString())

        val tryMap = map(Try { 1 }, Try { 2 }, Try { 3 }) { (one, two, three) ->
            one + two + three
        }

        logger.info(tryMap.toString())
    }

    @Test
    fun testShow() {
        logger.info(Int.show().run {
            //这里面调用的show函数式 Show<Int> 中扩展给Int的函数
            1.show()
        })

        logger.info(Show.any().run {
            //这里会自动为Show<T>中的T类型扩展show函数
            Option.just(1).show()
        })
        logger.info(String.eq().run {
            "1".eqv("2")
        }.toString())
        Int.eq().run { 1.neqv(2) }
        Eq.any().run { Some(1).eqv(Option.just(1)) }
        Eq.any().run { Eval.later { 1 }.eqv(Eval.later { 1 }) }
        val intEq = Eq<Int> { a, b -> a == b }


        /* String.show().run {
             "werwe".show()
         }
         val a = "asd".apply {
             println(this + "---")
         }
         println(a)*/
    }

    val log: (Any) -> Unit = {
        logger.info("$it")
    }

    @Test
    fun testOption2() {
        log(Some("I am wrapped in something").getOrElse { "hello" })
        log(none<String>().getOrElse { "hello" })
        val myString: String? = "Nullable string"
        val option: Option<String> = Option.fromNullable(myString)
        log(option)

        val number: Option<Int> = Some(3)
        val noNumber: Option<Int> = None
        val mappedResult1 = number.map {
            log("=======some map")
            it * 1.5
        }
        val mappedResult2 = noNumber.map {
            log("=======none map")
            it * 1.5
        }
        log(mappedResult1)
        log(mappedResult2)
        val a = Some(3).fold({ 1 }, { it * 3 })
        val b = none<Int>().fold({ 10 }, { it * 3 })
        log(a)
        log(b)
        log(none<String>().fold({ "aaa" }, { "$it--" }))

        val myList: List<Int> = listOf(1, 2, 3, 4)
        val first4 = myList.firstOrNone { it == 4 }
        val first5 = myList.firstOrNone { it == 5 }
        log(first4)
        log(first5)

        val foxMap = mapOf(1 to "The", 2 to "Quick", 3 to "Brown", 4 to "Fox")
        val ugly = foxMap.entries.firstOrNull { it.key == 5 }?.value.let { it?.toCharArray() }.toOption()
        val pretty = foxMap.entries.firstOrNone { it.key == 5 }.map { it.value.toCharArray() }
        log(ugly)
        log(pretty)
        val tuple3 = Tuple3(Some(1), Some("Hello"), Some(20.0))
        log(tuple3.a.t)
        val res = Option.fx {
            val (a) = Some(1)
            val (b) = Some(1 + a)
            val (c) = Some(1 + b)
            a + b + c
        }
        log(res)

        Option.fx {
            val (x) = none<Int>()
            val (y) = Some(1 + x)
            val (z) = Some(1 + y)
            x + y + z
        }.map(log)
    }

    @Test
    fun testEither() {
        Either.Right(5).flatMap { Either.Right(it + 1) }.map { it + 10 }.map(log)
//        Either.left(10).let(log)
        Either.left(10).map(log)
        val r: Either<Int, Int> = Either.Right(7)
        r.mapLeft { it + 1 }.let(log)
        val l: Either<Int, Int> = Either.Left(7)
        l.mapLeft { it + 10 }.let(log)

        val sw: Either<String, Int> = Either.Right(7)
        val swapped = sw.swap()
        log("-changeType:$sw to $swapped")
        log(7.right())
        log("hello".left())
        log(7.right().contains(7))
        log(7.right().contains(8))
        log("hello".left().getOrHandle { "$it world!" })
        log(Either.cond(true, { 42 }, { "Error" }))
        log(Either.cond(false, { 42 }, { "Error" }))
        log(2.right().fold({ 1 }, { it + 3 }))
        val either: Either<Int, Int> = 2.left()
        log(either.fold({ 1 }, { it + 10 }))

        val ri: Either<Throwable, Int> = Either.Left(NumberFormatException())
        ri.getOrHandle {
            when (it) {
                is NumberFormatException -> 400
                else -> 500
            }
        }.let(log)

        log(Right(12).leftIfNull { -1 })
        log(Right(null).leftIfNull { -1 })
        log(Left(12).leftIfNull { -1 })
        log("value".rightIfNotNull { "left" })
        log(null.rightIfNotNull { "left" })
        log("value".rightIfNull { "left" })
        log(null.rightIfNull { "left" })
        val cc = Tuple3(Either.Right(1), Either.Right("a"), Either.Right(2.0))
        log(cc)
        Either.fx<Int, Int> {
            val (a) = Either.Right(1)
            val (b) = Either.Right(1 + a)
            val (c) = Either.Right(1 + b)
            a + b + c
        }.let(log)
    }

    @Test
    fun testNotEmptyList() {
        val nel = NonEmptyList.of(1, 2, 3, 4, 5)
        nel.let(log)
        nel.head.let(log)
        nel.foldLeft(0) { acc, n -> acc + n }.let(log)
        nel.map { it * 10 }.let(log)


        val nelOne: NonEmptyList<Int> = NonEmptyList.of(1)
        val nelTwo: NonEmptyList<Int> = NonEmptyList.of(2)
        val value = nelOne.flatMap { one ->
            nelTwo.map { two ->
                one + two
            }
        }
        log(value)
        val nelThree: NonEmptyList<Int> = NonEmptyList.of(3)

        NonEmptyList.fx {
            val (one) = nelOne
            val (two) = nelTwo
            val (three) = nelThree
            one + two + three
        }.let(log)

        NonEmptyList.fx {
            val (x) = NonEmptyList.of(1, 2, 3)
            val (y) = NonEmptyList.of(1, 2, 3)
            x + y
        }.let(log)

        data class Person(val id: UUID, val name: String, val year: Int)

        // Note each NonEmptyList is of a different type
        val nelId: NonEmptyList<UUID> = NonEmptyList.of(UUID.randomUUID(), UUID.randomUUID())
        val nelName: NonEmptyList<String> = NonEmptyList.of("William Alvin Howard", "Haskell Curry")
        val nelYear: NonEmptyList<Int> = NonEmptyList.of(1926, 1900)

        val list = map(nelId, nelName, nelYear) { (id, name, year) ->
            Person(id, name, year)
        }
        log(list)
        val listK = listOf(1, 2, 3).k()
        listK.map {
            log("key map $it")
            it * 10
        }.let(log)

        val hello = listOf('h', 'e', 'l', 'l', 'o')
        val commaSpace = listOf(',', ' ')
        val world = listOf('w', 'o', 'r', 'l', 'd')
        hello.combineK(commaSpace).combineK(world).let(log)
        val listOption = listOf(Math.random(), Math.random(), Math.random()).traverse(Option.applicative()) { if (it > 0.5) Some(it) else None }
        log(listOption)
        listOf('a', 'b', 'c', 'd', 'e').k().foldLeft("-> ") { x, y -> x + y }.let(log)
        listOf(1, 2, 3).ap(listOf({ x: Int -> x + 10 }, { x: Int -> x * 2 })).let(log)

    }

    @Test
    fun testKleisli() {
        val optionIntKleisli = Kleisli { str: String ->
            if (str.toCharArray().all { it.isDigit() }) Some(str.toInt()) else None
        }

        fun String.safeToInt(): Option<Int> {
            return optionIntKleisli.run(this).fix()
        }
        log("a".safeToInt())
        log("1".safeToInt())
        optionIntKleisli.local { optStr: Option<String> -> optStr.getOrElse { "0" } }.run(None).let(log)
        optionIntKleisli.local { optStr: Option<String> -> optStr.getOrElse { "0" } }.run(Some("2")).let(log)

        val intToDouble = { number: Int -> number.toDouble() }
        val optionIntDoubleKleisli = Kleisli { str: String ->
            if (str.toCharArray().all { it.isDigit() }) Some(intToDouble) else None
        }
        optionIntKleisli.ap(Option.applicative(), optionIntDoubleKleisli).fix().run("1").let(log)
        // Some(1.0)
        optionIntKleisli.map(Option.applicative()) { output -> output + 1 }.fix().run("1").let(log)
        optionIntKleisli.map(Option.applicative()) { output -> output + 1 }.fix().run("10").let(log) // 注意这里的run 不是kotlin自带的作用域函数
        // Some(2)

        val optionDoubleKleisli = Kleisli { str: String ->
            if (str.toCharArray().all { it.isDigit() }) Some(str.toDouble()) else None
        }

        optionIntKleisli.flatMap(Option.monad()) { optionDoubleKleisli }.fix().run("144").let(log)
        optionIntKleisli.flatMap(Option.monad()) { optionDoubleKleisli }.fix().run("1w3").let(log)
        // Some(1.0)

        val optionFromOptionKleisli = Kleisli { number: Int ->
            Some(number + 1)
        }
        optionIntKleisli.andThen(Option.monad(), optionFromOptionKleisli).fix().run("1").let(log)
        // Some(2)
        optionIntKleisli.andThen(Option.monad()) { number: Int -> Some(number + 1) }.fix().run("1").let(log)
        // Some(2)
        optionIntKleisli.andThen(Option.monad(), Some(0)).fix().run("1").let(log)
        // Some(0)
    }

    @Test
    fun testStore() {
        val store = Store(0) { "The current value is: $it" }
        store.extract().let(log)
        store.move(store.state + 1).extract().let(log)
        val tupleStore = store.coflatMap { it: Store<Int, String> -> Tuple2("State", it.state) }
        tupleStore.extract().let(log)
        // Tuple2(a=State, b=0)
    }

    @Test
    fun testIO() {
        IO<Int> { throw RuntimeException() }.attempt().unsafeRunSync().let(log)
        IO<Int> { throw RuntimeException("Boom!") }
                .runAsync { result ->
                    result.fold({ IO { log("Error") } }, { IO { log(it.toString()) } })
                }.unsafeRunSync()

        IO<Int> { throw RuntimeException("Boom!") }
                .unsafeRunAsync { result ->
                    result.fold({ log("Error-result") }, { log(it.toString()) })
                }
        IO { 1 }.attempt().unsafeRunSync().let(log)
        val cancel = IO<Int> { 23 }
                .unsafeRunAsyncCancellable { result ->
                    result.fold({ println("Error") }, { println(it.toString()) })
                }

        cancel()
        IO<Int> {
            //throw RuntimeException("Boom!")
            IO.sleep(5000.milliseconds)
            90
        }.attempt().unsafeRunTimed(100.milliseconds).let { log(it) }
        IO { 1 }.unsafeRunSync().let(log)
        IO.just(23).unsafeRunAsync(log)

        IO.effect { log("-----hello world-----") }.unsafeRunSync().let(log)
        IO.effect { throw RuntimeException("Boom!") }.attempt().unsafeRunSync().let(log)

        IO.defer { IO.just(1) }.attempt().unsafeRunSync().let(log)
        IO.async<Int> { callback ->
            callback(1.right())
        }.attempt().unsafeRunSync().let(log)

        IO.cancelable<String> { callback ->
            val observable = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon").toObservable().filter { it.length >= 5 }
            val subscription = observable.subscribe {
                callback(it.right())
            }
            IO { subscription.dispose() }
        }.attempt().unsafeRunSync().let(log)

        IO.sleep(3.seconds).flatMap { IO.effect { println("Hello World!") } }.unsafeRunSync().let { log }
    }

    data class Student(val name: String, val age: Int)

    val john = Student("John", 30)
    val jane = Student("Jane", 32)
    val jack = Student("Jack", 32)
    val chris = Student("Chris", 40)

    @Test
    fun testSelect() {
        listOf(1, 2, 3).query {
            select { this + 1 }
        }.value().let(log)

        listOf(1, 2, 3).query {
            // `listOf(1, 2, 3)` is what the source of data and what we use as `from`
            select { this }
        }.value().let(log)

        listOf(john, jane, jack).query {
            select { name } where { age > 20 }
        }.value().let(log)

        listOf(john, jane, jack).query {
            selectAll().where { age > 30 }.groupBy { this.age }.from
        }.value().let(log)

        listOf(1, 2, 3).query {
            select { this * 10 } orderBy Ord.Asc(Int.order())
        }.value().let(log)

        listOf(john, jane, jack).query {
            selectAll() groupBy { age } orderMap Ord.Desc(Int.order())
        }.value().let(log)

        listOf(john, jane, jack).query {
            selectAll() where { age > 30 } sum { age.toLong() }
        }.value().let(log)

        val queryA = listOf("customer" to john, "customer" to jane).query { selectAll() }
        val queryB = listOf("sales" to jack, "sales" to chris).query { selectAll() }
        queryA.union(queryB).value().let(log)


    }


    @Test
    fun testMonadDefer() {

        val now = IO.applicative().just(println("eager side effect"))
// Print: "eager side effect"

        now.unsafeRunAsync { }
// Nothing, the effect has run already

        val later = IO.monadDefer().later { println("lazy side effect") }.attempt().run(log)
        IO.monadDefer().defer { IO.just(1) }
        val temp = IO.monadDefer().just("123").map {
            log("----")
            23
        }
        Thread.sleep(4000)
        temp.unsafeRunSync().let(log)


// Nothing, the effect is deferred until executed

// Print: "lazy side effect"
    }

    @Test
    fun  testLazy(){
        val SC = IO.monadDefer()

        val result = SC.fx.monad {
            println("Print: now")
            val (result) = just(1)
            result + 1
        }

//Print: now

        val lazyResult = SC.fx.monad {
            SC.lazy().bind()
            println("Print: lazy")
            val (result) = IO<Int>{2}
            result + 1
        }

//Nothing here!
        lazyResult.map { 23 }.unsafeRunSync().run(log)
    }

}

data class User(val id: Int) {
    companion object
}

@extension
interface UserEq : Eq<User> {
    override fun User.eqv(b: User): Boolean = id == b.id
}

class ForOption private constructor() {
    companion object {}
}

sealed class Option<A> : Kind<ForOption, A>

class ForListK private constructor() {
    companion object {}
}

data class ListK<A>(val list: List<A>) : Kind<ForListK, A>