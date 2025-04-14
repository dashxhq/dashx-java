import com.expediagroup.graphql.plugin.gradle.config.GraphQLScalar
import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.graphql
import com.vanniktech.maven.publish.SonatypeHost

val group = libs.versions.group.get()
val artifactId = "dashx-java"
val version = libs.versions.dashx.get()

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.graphql)
    alias(libs.plugins.gradle.maven.publish)
    alias(libs.plugins.ktfmt)

    id("signing")
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.serialization)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.graphql.ktor.client)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)
    implementation(libs.json)
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

graphql {
    client {
        endpoint = "https://api.dashx-staging.com/graphql"
        packageName = "com.dashx.graphql.generated"

        customScalars =
            listOf(
                GraphQLScalar(
                    "JSON",
                    "kotlinx.serialization.json.JsonObject",
                    "com.dashx.graphql.scalars.converters.JsonScalarConverter"),
            )
        serializer = GraphQLSerializer.KOTLINX
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

mavenPublishing {
    coordinates(group, artifactId, version)

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    pom {
        name.set("DashX Java SDK")
        description.set("DashX SDK for Java")
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

ktfmt {
    blockIndent.set(4)
    continuationIndent.set(4)
}
