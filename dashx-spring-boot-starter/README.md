# DashX Spring Boot Starter

A Spring Boot starter for the DashX Java SDK that provides auto-configuration and easy integration with Spring Boot applications.

## Features

- Auto-configuration of the DashX client
- Property-based configuration
- Easy integration with Spring Boot applications

## Requirements

- Java 17 or higher
- Spring Boot 3.4.1 or higher

## Installation

Add the following dependency to your `build.gradle`:

```
dependencies {
    implementation 'com.dashx:dashx-spring-boot-starter:${version}'
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

# Optional: Connection timeout in milliseconds (default: 10000)
dashx.connection-timeout=10000

# Optional: Response timeout in milliseconds (default: 30000)
dashx.response-timeout=30000

# Optional: Maximum number of connections in the pool (default: 500)
dashx.max-connections=500

# Optional: Maximum idle time for connections in milliseconds (default: 20000)
dashx.max-idle-time=20000
```

### Connection Configuration

The DashX client uses connection pooling and configurable timeouts to ensure reliable communication with the DashX API. You can fine-tune these settings based on your application's needs:

- `dashx.connection-timeout`: Time to wait for establishing a connection (in milliseconds). Default: 10000ms (10 seconds).
- `dashx.response-timeout`: Time to wait for receiving a response after the connection is established (in milliseconds). Default: 30000ms (30 seconds).
- `dashx.max-connections`: Maximum number of connections in the connection pool. Default: 500.
- `dashx.max-idle-time`: Maximum time a connection can remain idle in the pool before being closed (in milliseconds). Default: 20000ms (20 seconds).

These settings help prevent connection timeouts and improve reliability, especially in high-load scenarios or when dealing with network latency.

```

## Usage

The DashX client will be automatically configured and available for injection in your Spring components:

```
import com.dashx.DashX
...

@RestController
public class DemoController {
    private final DashX dashX;

    public DemoController(DashX dashX) {
        this.dashX = dashX;
    }

    @GetMapping("/home")
    public CompletableFuture<Map<String, Object>> home(
            @RequestParam Map<String, Object> options) {
        ...
        dashX.identify(options).thenApply(response -> {
            Map<String, Object> result = new HashMap<>();

            result.put("id", response.getId());
            result.put("firstName", response.getFirstName());
            result.put("lastName", response.getLastName());
            ...
        })
        ...
    }
}
```

## Example

See the [dashx-demo-spring-boot](../dashx-demo-spring-boot) project for a complete example of how to use this starter.

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
