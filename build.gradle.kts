plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.spring) apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}
