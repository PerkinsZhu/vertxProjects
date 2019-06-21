package com.perkins.spring

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.JsonObject
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class SpringApp : VertxCommandLauncher(), VertxLifecycleHooks {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //TODO  可以借鉴一下 vertx-example
            SpringApp().dispatch(args)
        }
    }

    override fun handleDeployFailed(vertx: Vertx?, mainVerticle: String?, deploymentOptions: DeploymentOptions?, cause: Throwable?) {
    }

    override fun beforeStartingVertx(options: VertxOptions?) {
        options?.isClustered = false
    }

    override fun afterStoppingVertx() {
    }

    override fun afterConfigParsed(config: JsonObject?) {
    }

    override fun afterStartingVertx(vertx: Vertx?) {
    }

    override fun beforeStoppingVertx(vertx: Vertx?) {
    }

    override fun beforeDeployingVerticle(deploymentOptions: DeploymentOptions?) {

        AnnotationConfigApplicationContext(ApplicationContextAware {
            SpringProxy.context = it
        }.javaClass)
    }

}