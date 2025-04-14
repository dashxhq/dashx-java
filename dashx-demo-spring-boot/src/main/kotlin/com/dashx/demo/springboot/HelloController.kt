package com.dashx.demo.springboot

import com.dashx.DashX as DashXClient
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(private val dashx: DashXClient) {
    @GetMapping("/hello")
    fun hello(): String {
        dashx.track(event = "hello", null, hashMapOf("message" to "Hello, World!"))

        return "Hello, World!"
    }

    @GetMapping("/assets")
    suspend fun listAssets(
        @RequestParam(required = false) resourceId: String?,
        @RequestParam(required = false, defaultValue = "20") limit: Int
    ): String {
        val result =
            dashx
                .listAssets(
                    filter =
                        buildJsonObject {
                            if (resourceId != null) {
                                put("resourceId", buildJsonObject { put("eq", resourceId) })
                            }
                        },
                    limit = limit,
                )
                .await()

        return """
            <div>
                ${result?.filter { it.url != null }?.joinToString("<br />") { asset ->
                    """<a href="${asset.url}">${asset.name}</a>"""
                } ?: "No assets found"}
            </div>
        """
            .trimIndent()
    }
}
