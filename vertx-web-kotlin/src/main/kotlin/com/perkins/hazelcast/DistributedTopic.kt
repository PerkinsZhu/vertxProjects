package com.perkins.hazelcast

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.Message
import com.hazelcast.core.MessageListener
import org.checkerframework.checker.units.qual.h

class DistributedTopic : MessageListener<String> {

    override fun onMessage(message: Message<String>?) {
        println("Got message " + message?.messageObject);
//        h.shutdown()
    }

    companion object {
        lateinit var h: HazelcastInstance
        @JvmStatic
        fun main(args: Array<String>) {
            val config = Config()
            h = Hazelcast.newHazelcastInstance(config)
            val topic = h.getTopic<String>("my-distributed-topic")
            topic.addMessageListener(DistributedTopic())

            (1 until 100).forEach {
                Thread.sleep(500)
                topic.publish("$it --Hello to distributed world")
            }



        }
    }
}