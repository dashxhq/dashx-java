package com.dashx.demo.springboot

import com.dashx.DashX
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(private val dashx: DashX) {
    @GetMapping("/")
    fun index(): String {
        dashx.track("Test Event")
        return "Greetings from Spring Boot!"
    }
}
