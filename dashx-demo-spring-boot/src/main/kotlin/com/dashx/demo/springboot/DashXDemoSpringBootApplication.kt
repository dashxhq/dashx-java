package com.dashx.demo.springboot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class DashXDemoSpringBootApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(DashXDemoSpringBootApplication::class.java, *args)
        }
    }
}
