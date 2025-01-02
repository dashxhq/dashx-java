package com.dashx.util

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val baseUri: String,
    val publicKey: String,
    val privateKey: String,
    val targetEnvironment: String,
)
