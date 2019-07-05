package com.perkins.vavr;

import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import kotlin.jvm.functions.Function3;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.vavr.API.*;

public class BaseTest {
    //参考：https://www.playscala.cn/article/view?_id=10-5d19ca62eeab561fdb2794b4

    @Test
    public void tesVavr() {
        Tuple2<String, Integer> tuple2 = Tuple.of("Hello", 100);
        Tuple2<String, Integer> updatedTuple2 = tuple2.map(String::toUpperCase, v -> v * 5);
        String result = updatedTuple2.apply((str, number) -> String.join(", ", str, number.toString()));
        System.out.println(result);
    }


    @Test
    public void testFunction() {
/*
        Function3<Integer, Integer, Integer, Integer> function3 = (v1, v2, v3) -> (v1 + v2) * v3;
        Function3<Integer, Integer, Integer, Integer> composed = function3.andThen(v -> v * 100);
        int result = composed.apply(1, 2, 3);
        System.out.println(result);*/
        // 输出结果 900

        Function1<String, String> function1 = String::toUpperCase;
        Function1<Object, String> toUpperCase = function1.compose(Object::toString);
        String str = toUpperCase.apply(List.of("a", "b"));
        System.out.println(str);
        // 输出结果[A, B]


        Function4<Integer, Integer, Integer, Integer, Integer> function4 = (v1, v2, v3, v4) -> (v1 + v2) * (v3 + v4);
        Function2<Integer, Integer, Integer> function2 = function4.apply(1, 2);  //注意这里返回的类型 。部分应用函数
        System.out.println(function2.apply(4, 5));
        // 输出 27

/*

        Function3<Integer, Integer, Integer, Integer> function31 = (v1, v2, v3) -> (v1 + v2) * v3;
        int result2 = function31.curried().apply(1).curried().apply(2).curried().apply(3);
        System.out.println(result2);
*/


    }

    @Test
    public void testVal() {
        Option<String> str = Option.of("Hello");
        str.map(String::length);
        str.flatMap(v -> Option.of(v.length())).forEach(o -> {
            println(o);
        });

        Either<String, String> either = compute()
                .map(item -> item + " World")
                .mapLeft(Throwable::getMessage); // 当为Left时执行该map
        System.out.println(either);


        Try<Integer> result = Try.of(() -> 1 / 0).recover(e -> 1);
        System.out.println(result);
        Try<String> lines = Try.of(() -> Files.readAllLines(Paths.get("D:\\zhupingjing\\testFile\\datatest.txt")))
                .map(list -> String.join(",", list))
                .andThen((Consumer<String>) System.out::println);
        System.out.println(lines);


        // ================= 延迟计算 ============================
        Lazy<BigInteger> lazy = Lazy.of(() -> BigInteger.valueOf(1024).pow(1024));
        System.out.println(lazy.isEvaluated());
        System.out.println(lazy.get());
        System.out.println(lazy.isEvaluated());

    }


    @Test
    public void testStream() {
        Map<Boolean, List<Integer>> booleanListMap = Stream.ofAll(1, 2, 3, 4, 5)
                .groupBy(v -> v % 2 == 0)
                .mapValues(Value::toList);
        System.out.println(booleanListMap);
// 输出 LinkedHashMap((false, List(1, 3, 5)), (true, List(2, 4)))

        Tuple2<List<Integer>, List<Integer>> listTuple2 = Stream.ofAll(1, 2, 3, 4)
                .partition(v -> v > 2)
                .map(Value::toList, Value::toList);
        System.out.println(listTuple2);
// 输出 (List(3, 4), List(1, 2))

        List<Integer> integers = Stream.ofAll(List.of("Hello", "World", "a"))
                .scanLeft(0, (sum, str) -> sum + str.length())
                .toList();
        System.out.println(integers);
// 输出 List(0, 5, 10, 11)

        List<Tuple2<Integer, String>> tuple2List = Stream.ofAll(1, 2, 3)
                .zip(List.of("a", "b"))
                .toList();
        System.out.println(tuple2List);
// 输出 List((1, a), (2, b))

        List.of(1, 2, 3).map(v -> v + 10); //Vavr
//        java.util.List.of(1, 2, 3).stream().map(v -> v + 10).collect(Collectors.toList()); //Java 中 Stream
    }

    @Test
    public void testMatch() {
        String input = "g";
        String result = Match(input).of(
                Case($("g"), "good"),
                Case($("b"), "bad"),
                Case($(), "unknown")
        );
        System.out.println(result);
// 输出 good
    }


    private static Either<Throwable, String> compute() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.nextBoolean() ? Either.left(new RuntimeException("Boom!")) : Either.right("Hello");
    }


    public static void main(String[] args) {
        remberFunction();
    }


    static public void remberFunction() {
        //函数的记忆化
        Function2<BigInteger, Integer, BigInteger> pow = BigInteger::pow;
        Function2<BigInteger, Integer, BigInteger> memoized = pow.memoized();
        long start = System.currentTimeMillis();
        memoized.apply(BigInteger.valueOf(1024), 1024);
        long end1 = System.currentTimeMillis();
        memoized.apply(BigInteger.valueOf(1024), 1024); // 本次计算由于有缓存，就可以直接得出结果，不再进行计算
        long end2 = System.currentTimeMillis();
        System.out.printf("%d ms -> %d ms", end1 - start, end2 - end1);
    }

}
