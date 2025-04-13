package com.dashx.springboot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dashx")
data class DashXProperties(
        var publicKey: String = "",
        var privateKey: String = "",
        var targetEnvironment: String = "",
        var baseUrl: String = "https://api.dashx.com/graphql",
)
