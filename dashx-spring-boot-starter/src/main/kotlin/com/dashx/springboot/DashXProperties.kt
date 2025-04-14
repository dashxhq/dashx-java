package com.dashx.springboot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dashx")
data class DashXProperties(
    var baseUrl: String = "https://api.dashx.com/graphql",
    var publicKey: String,
    var privateKey: String,
    var targetEnvironment: String? = null,
)
