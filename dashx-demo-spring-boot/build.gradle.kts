plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.ktfmt)
}

val group = libs.versions.group.get()
val version = libs.versions.dashx.get()

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

dependencies {
    implementation(project(":dashx-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("me.paulschwarz:spring-dotenv:4.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("io.projectreactor:reactor-core:3.6.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

ktfmt {
    blockIndent.set(4)
    continuationIndent.set(4)
}

tasks.withType<Test> { useJUnitPlatform() }
