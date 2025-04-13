plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
}

group = "com.dashx"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":dashx"))
    implementation("org.springframework.boot:spring-boot-autoconfigure:3.4.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
