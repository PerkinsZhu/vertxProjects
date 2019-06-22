package com.perkins.security

import org.junit.Test

class SecurityApp {

    @Test
    fun testSalt() {
        val password = "123456"
        val byteArray = ByteArray(256)
        java.security.SecureRandom().nextBytes(byteArray)
        val salt = SHA256Util.byte2Hex(byteArray)
        val securityPassword = SHA256Util.getSHA256StrJava(password + salt)
        val securityPassword2 = SHA256Util.getSHA256StrJava(password + salt)
        println(securityPassword)
        println(securityPassword2)
        println(salt)
    }

    @Test
    fun testByteToHex() {
        val byteArray = ByteArray(10)
        java.security.SecureRandom().nextBytes(byteArray)
        SHA256Util.byte2HexTest(byteArray)
        /*val a = 100
        println(Integer.toBinaryString(a))
        println(Integer.toHexString(a))
        println(Integer.toOctalString(a))
*/
    }


}