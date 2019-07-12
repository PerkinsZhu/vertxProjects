package com.perkins.hazelcast

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast


class DistributedMap {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val config = Config()
            val h = Hazelcast.newHazelcastInstance(config)
            val map = h.getMap<String, String>("my-distributed-map")
            map["key"] = "value"

            //Concurrent Map methods
            (map as java.util.Map<String, String>).putIfAbsent("somekey", "somevalue")
            (map as java.util.Map<String, String>).replace("key", "value", "newvalue")

            map.forEach { (k, v) -> println("$k => $v") }

//            h.shutdown()
        }
    }
}