package com.perkins.security

class Person constructor(val aa: String, var bb: String, cc: String) {
    constructor(aaa: String, bbb: String, ccc: String, dd: String) : this(aaa, bbb, ccc) {
        println(aaa)
    }

    lateinit var ee: String
    val ff = 123

    fun show(a: String) {
        println(a)
    }

}