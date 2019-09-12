package com.perkins.service

import com.perkins.security.User
import org.junit.Test

class UserService {


    @Test
    fun testUser() {
        val user = User("jack", 24, "sd", "bb")
        println(user)
        println(user.no)
        user.no = 100
        println(user.no)
        println(user.name)
        println(User("jack", 24))
    }
}