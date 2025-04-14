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

# Optional: Your DashX private key (if needed for specific operations)
dashx.private-key=your-private-key-here

# Optional: Target environment (e.g., "production", "staging")
dashx.target-environment=production

# Optional: Override the default base URL (defaults to https://api.dashx.com/graphql)
dashx.base-url=https://api.dashx-staging.com/graphql
```

## Usage

The DashX client will be automatically configured and available for injection in your Spring components:

```kotlin
@RestController
class MyController(private val dashxClient: DashxClient) {

    @GetMapping("/identify")
    fun identifyUser(@RequestParam userId: String) {
        dashxClient.identify(userId)
    }

    @GetMapping("/track")
    fun trackEvent(@RequestParam eventName: String) {
        dashxClient.track(eventName)
    }
}
```

## Available Methods

The `DashxClient` provides the following methods:

- `identify(userId: String, traits: Map<String, Any>? = null)`: Identify a user
- `track(eventName: String, properties: Map<String, Any>? = null)`: Track an event
- `reset()`: Reset the user's identity

## Example

See the [dashx-demo-spring-boot](../dashx-demo-spring-boot) project for a complete example of how to use this starter.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
