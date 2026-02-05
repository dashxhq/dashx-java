# DashX SDK for Java

## Installation

### Spring Boot

Add the Spring Boot starter dependency to your `build.gradle`:

```gradle
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

### Standalone Java

Add the core dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.dashx:dashx:${version}'
}
```

## Configuration

### Option 1: Spring Boot (application.properties)

Add the following properties to your `application.properties`:

```properties
# Required properties
dashx.public-key=${DASHX_PUBLIC_KEY}
dashx.private-key=${DASHX_PRIVATE_KEY}
dashx.target-environment=${DASHX_TARGET_ENVIRONMENT}
dashx.base-url=${DASHX_BASE_URL:https://api.dashx.com/graphql}

# Optional: Connection and timeout configuration (defaults shown, all values in milliseconds)
dashx.connection-timeout=10000
dashx.response-timeout=30000
dashx.max-connections=500
dashx.max-idle-time=20000
```

The DashX client will be automatically configured and available for injection:

```java
@RestController
public class MyController {
    private final DashX dashX;

    public MyController(DashX dashX) {
        this.dashX = dashX;
    }

    @GetMapping("/identify")
    public CompletableFuture<Account> identify(@RequestParam Map<String, Object> options) {
        return dashX.identify(options);
    }
}
```

### Option 2: Direct Java Configuration

If you're not using Spring Boot, configure DashX directly:

```java
DashXConfig config = new DashXConfig.Builder()
    .publicKey("your-public-key")
    .privateKey("your-private-key")
    .targetEnvironment("production")
    .baseUrl("https://api.dashx.com/graphql")
    // Optional: customize timeouts (all values in milliseconds)
    .connectionTimeout(15000)          // 15 seconds
    .responseTimeout(45000)            // 45 seconds
    .maxConnections(1000)              // Increase for high-load scenarios
    .maxIdleTime(30000)                // 30 seconds
    .build();

DashX dashx = DashX.getInstance();
dashx.configure(config);

// Use the client
dashx.track("button_clicked", "user123", Map.of("button", "submit"))
    .thenAccept(response -> System.out.println("Event tracked!"))
    .exceptionally(error -> {
        System.err.println("Error: " + error.getMessage());
        return null;
    });
```

## Connection Configuration

The SDK uses connection pooling and configurable timeouts for reliable communication with the DashX API:

- **`connection-timeout`**: Time to establish a connection (default: 10000ms)
- **`response-timeout`**: Time to receive complete response (default: 30000ms)
- **`max-connections`**: Maximum connections in pool (default: 500)
- **`max-idle-time`**: Maximum idle time before connection closure (default: 20000ms)

### Recommended Values by Traffic Level

#### Low Traffic (< 100 req/min)
```properties
dashx.connection-timeout=10000
dashx.response-timeout=30000
dashx.max-connections=100
dashx.max-idle-time=20000
```

#### Medium Traffic (100-1000 req/min)
```properties
dashx.connection-timeout=10000
dashx.response-timeout=30000
dashx.max-connections=500
dashx.max-idle-time=30000
```

#### High Traffic (> 1000 req/min)
```properties
dashx.connection-timeout=15000
dashx.response-timeout=45000
dashx.max-connections=1000
dashx.max-idle-time=60000
```

#### Behind Proxy/Load Balancer
```properties
dashx.connection-timeout=20000
dashx.response-timeout=60000
dashx.max-connections=500
dashx.max-idle-time=45000
```

## Troubleshooting

### Connection Timeout Errors

If you encounter "Connection prematurely closed BEFORE response" errors:

1. **Increase timeouts** - Your network latency might be higher:
   ```properties
   dashx.response-timeout=60000
   dashx.connection-timeout=20000
   ```

2. **Enable debug logging** - Add to `application.properties`:
   ```properties
   logging.level.reactor.netty=DEBUG
   logging.level.com.dashx=DEBUG
   ```

## Examples

See the [dashx-demo-spring-boot](dashx-demo-spring-boot) project for a complete working example.

For detailed Spring Boot starter documentation, see [dashx-spring-boot-starter README](dashx-spring-boot-starter/README.md).

## Development

### Prerequisites

#### Java
```
brew install java
```

#### SDKMAN
```
curl -s "https://get.sdkman.io" | bash
```

#### Gradle
```
sdk install gradle 8.12
```

This project uses gradle wrapper, to initialize it, run:
```
gradle wrapper
```

### Running gradle tasks
You can build the project using:
```
gradle build
```

To do a clean build, run:
```
gradle clean
```
before the build command.

To start the demo application, run:
```
gradle dashx-demo-spring-boot:bootRun
```

Now you should be able to see the greeting message on `http://localhost:8080`.

To see all the available gradle commands, run:
```
gradle -q :tasks --all
```

## Publishing

You will need `gnupg` to generate the signing keys. If you don't have it installed, please run:
```
brew install gnupg gnupg2
```

#### Generate a new GPG key
```
gpg --full-generate-key
```
- Select “RSA and RSA” (default)
- Enter key size → 4096 (recommended for security)
- Set expiration → Choose 1y (1 year) or 0 (no expiration)
- Enter name & email → Should match your Sonatype account
- Set a strong passphrase

#### Distribute your public key

First, list your gpg keys using:
```
gpg --list-keys --keyid-format LONG
```

You should see something like this:
```
pub   rsa4096/3AA5C34371567BD2 2024-01-01 [SC]
....
```

`3AA5C34371567BD2` is your GPG key id.

Since you'll be using this key to sign the release files, other people would need your public key to verify the signature. To distribute your public key, please run:
```
gpg --keyserver keyserver.ubuntu.com --send-keys 3AA5C34371567BD2
```

You can find more information regarding this [here](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key).

#### Start the GPG Agent Daemon
```
gpg-agent --daemon
```

In order for GPG agent to work properly, you might need to put the following in your `~/.zshrc` file:
```
export GPG_TTY=$(tty)
```

Don't forget to run `source ~/.zshrc` after updating your `~/.zshrc` file.

#### Setup publishing credentials
To publish to Maven Central repository, you will first need to generate the user token in your Sonatype account. You can do so by clicking on the **User** icon in the top right corner -> View Account -> Generate User Token.

You can then supply credentials to the publishing task by setting them in `~/.gradle/gradle.properties` file. You might already have `~/.gradle` folder but no `gradle.properties` file in it. If that's the case, please go ahead and create this file and set the following values in it:
```
mavenCentralUsername=GENERATED_TOKEN_USERNAME
mavenCentralPassword=GENERATED_TOKEN_PASSWORD

signing.keyId=GPG_KEY_ID
signing.password=GPG_KEY_PASSPHRASE
```

#### Publishing a new version
Please make sure you've set the new version number in the [build file](dashx/build.gradle.kts) before publishing.

To publish the new version, run:
```
gradle publish
```

If all goes correctly, you should be able to see a new deployment in your [Sonatype account](https://central.sonatype.com/publishing/deployments).
