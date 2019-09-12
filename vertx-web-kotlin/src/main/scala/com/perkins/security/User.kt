package com.perkins.security

class User constructor(var nameq: String, var age: Int, aa: String, val bb: String) {

    constructor(name: String, alexa: Int) : this(name, 23, "222", "bbb") {
    }

    fun show() {
        println(name)
        println(age)
//        println(aa)
        println(bb)
//        bb = "aaaa"
        age = 200
    }

    var name: String? = null

    var no: Int = 0
        get() {
            val a = field + 10
            return a
        }
        set(value) {
            if (value < 10) {
                field = value
            } else {
                field = -1
            }
        }

    override fun toString(): String {
        return "User(name='$name', age=$age)"
    }
}

class AAA() {
    lateinit var a: String

}