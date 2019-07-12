package com.perkins.hazelcast

import com.hazelcast.core.Hazelcast
import com.mchange.v1.identicator.StrongIdentityIdenticator
import org.junit.Test

class TestDemo {

    @Test
    fun baseTest() {
        val instance = Hazelcast.newHazelcastInstance()
        val map = instance.getMap<String, Int>("test")
        map.put("a",1)
    }
}