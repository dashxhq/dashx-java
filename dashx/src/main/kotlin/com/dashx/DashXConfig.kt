package com.dashx

import kotlinx.serialization.Serializable

@Serializable
data class DashXConfig(
    val baseUrl: String = "https://api.dashx.com/graphql",
    val publicKey: String,
    val privateKey: String,
    val targetEnvironment: String?,
)
