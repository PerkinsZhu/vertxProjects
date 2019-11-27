package com.perkins.genericity

import org.junit.Test

class GenericityTest {
    @Test
    fun testT() {
        val production1: Production<Food> = FoodStore()
        val production2: Production<Food> = FastFoodStore()
        val production3: Production<Food> = InOutBurger()

        /*
         val production1 : Production<Burger> = FoodStore()  // Error
         val production2 : Production<Burger> = FastFoodStore()  // Error
         val production3 : Production<Burger> = InOutBurger()
         */


    }
}

interface Production<out T> {
    fun produce(): T
}

interface Consumer<in T> {
    fun consume(item: T)
}

interface ProductionConsumer<T> {
    fun produce(): T
    fun consume(item: T)
}

open class Food
open class FastFood : Food()
class Burger : FastFood()

class FoodStore : Production<Food> {
    override fun produce(): Food {
        println("Produce food")
        return Food()
    }
}

class FastFoodStore : Production<FastFood> {
    override fun produce(): FastFood {
        println("Produce food")
        return FastFood()
    }
}

class InOutBurger : Production<Burger> {
    override fun produce(): Burger {
        println("Produce burger")
        return Burger()
    }
}