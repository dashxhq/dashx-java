<p align="center">
    <br />
    <a href="https://dashx.com"><img src="https://raw.githubusercontent.com/dashxhq/brand-book/master/assets/logo-black-text-color-icon@2x.png" alt="DashX" height="40" /></a>
    <br />
    <br />
    <strong>Your All-in-One Product Stack</strong>
</p>

<div align="center">
  <h4>
    <a href="https://dashx.com">Website</a>
    <span> | </span>
    <a href="https://docs.dashx.com">Documentation</a>
  </h4>
</div>

<br />

# dashx-java

_DashX SDK for Java_

## Install

The SDK is published to [Maven Central](https://central.sonatype.com/) (via Sonatype).

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
    implementation 'com.dashx:dashx-java:${version}'
}
```

## Documentation

For detailed documentation, visit [Java / Spring Boot SDK documentation](https://docs.dashx.com/sdks/server-side-sdk).
