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
import com.dashx.DashX as DashXClient
...

@RestController
class MyController(private val dashXClient: DashXClient) {

    @GetMapping("/login")
    fun login(...) {
        ...
        dashXClient.identify(userId)
        ...
    }

    @GetMapping("/add-to-cart")
    fun addToCart(...) {
        ...
        dashXClient.track("Item Added to Cart")
        ...
    }
}
```

## Example

See the [dashx-demo-spring-boot](../dashx-demo-spring-boot) project for a complete example of how to use this starter.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
