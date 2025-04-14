package com.dashx.demo.springboot

import com.dashx.DashX as DashXClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(
    private val dashx: DashXClient,
) {
    @GetMapping("/")
    fun index(): String {
        dashx.track("Test Event", null)
        return "Greetings from Spring Boot!"
    }
}
