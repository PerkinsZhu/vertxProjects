package com.perkins.apollo

import com.ctrip.framework.apollo.ConfigService
import org.junit.Test


class ApolloTest {

    //本地wind 启动，服务目录：D:\tmp\apollo-build-scripts-master
    // git base ./demo.sh start 启动
    @Test
    fun testGet() {
        val config = ConfigService.getAppConfig() //ConfigService.getConfig(Namespace);
        print(config.getProperty("name", "TOME"))
    }

}