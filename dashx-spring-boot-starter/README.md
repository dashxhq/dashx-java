# DashX Spring Boot Starter

A Spring Boot starter for the DashX Java SDK that provides auto-configuration and easy integration with Spring Boot applications.

## Features

- Auto-configuration of the DashX client
- Property-based configuration
- Easy integration with Spring Boot applications

## Requirements

- Java 21 or higher
- Spring Boot 3.4.1 or higher

## Installation

Add the following dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.dashx:dashx-spring-boot-starter:${version}")
}
```

Or if you're using Maven, add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.dashx</groupId>
    <artifactId>dashx-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

## Configuration

Add the following properties to your `application.properties` or `application.yml`:

```properties
# Required: Your DashX public key
dashx.public-key=your-public-key-here

# Required: Your DashX private key
dashx.private-key=your-private-key-here

# Optional: Target environment (e.g., "production", "staging")
dashx.target-environment=production

# Optional: Override the default base URL to connect to DashX
dashx.base-url=https://...
```

## Usage

The DashX client will be automatically configured and available for injection in your Spring components:

```kotlin
import com.dashx.DashX
...

@RestController
class MyController(private val dashX: DashX) {

    @GetMapping("/login")
    fun login(...) {
        ...
        dashX.identify(userId)
        ...
    }

    @GetMapping("/add-to-cart")
    fun addToCart(...) {
        ...
        dashX.track("Item Added to Cart")
        ...
    }

    @GetMapping("/products")
    suspend fun products(
        @RequestParam(required = false) productId: String?,
        @RequestParam(required = false, defaultValue = "20") limit: Int
    ): String {
        val result =
            dashX
                .listAssets(
                    filter =
                        buildJsonObject {
                            if (productId != null) {
                                put("resourceId", buildJsonObject { put("eq", productId) })
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
```

## Example

See the [dashx-demo-spring-boot](../dashx-demo-spring-boot) project for a complete example of how to use this starter.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
