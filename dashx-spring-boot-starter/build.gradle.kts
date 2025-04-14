import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.gradle.maven.publish)

    id("signing")
}

val group = libs.versions.group.get()
val artifactId = "dashx-spring-boot-starter"
val version = libs.versions.dashx.get()

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

    implementation(libs.spring.boot.autoconfigure)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

mavenPublishing {
    coordinates(group, artifactId, version)

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    pom {
        name.set("DashX Spring Boot Starter")
        description.set("DashX Starter for Spring Boot Applications")
        url.set("https://github.com/dashxhq/dashx-java")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/dashxhq/dashx-java/blob/main/LICENSE")
            }
        }

        developers {
            developer {
                id.set("gulshan.dashx")
                name.set("Gulshan Dhingra")
            }
        }

        scm {
            connection.set("scm:git:github.com/dashxhq/dashx-java.git")
            developerConnection.set("scm:git:ssh://github.com/dashxhq/dashx-java.git")
            url.set("https://github.com/dashxhq/dashx-java")
        }
    }
}
