package com.perkins.bean

class Person {
    constructor()
    constructor(name: String, age: Int) : this()

    var age: Int = 0

}

class Person2(private val username: String, private var age: Int) {}
class Person3(private val username: String, private var age: Int)
