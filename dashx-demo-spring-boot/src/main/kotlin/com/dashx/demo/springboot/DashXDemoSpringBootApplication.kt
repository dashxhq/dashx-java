package com.dashx.demo.springboot

import com.dashx.DashX
import com.dashx.util.Config as DashXConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class DashXDemoSpringBootApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(DashXDemoSpringBootApplication::class.java, *args)
        }
    }

    @Bean
    fun dashx(
            @Value("\${dashx.public_key}") publicKey: String,
            @Value("\${dashx.private_key}") privateKey: String,
            @Value("\${dashx.target_environment}") targetEnvironment: String
    ): DashX {
        val config =
                DashXConfig(
                        publicKey = publicKey,
                        privateKey = privateKey,
                        targetEnvironment = targetEnvironment
                )
        val dashx = DashX.getInstance("default")
        dashx.configure(config)
        return dashx
    }
}
