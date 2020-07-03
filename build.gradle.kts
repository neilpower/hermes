import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.72" apply false
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
    kotlin("jvm") version "1.3.72" apply false
}

subprojects {
    version = "0.0.1"

    apply {
        plugin("kotlin")
    }

    repositories {
        jcenter()
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            languageVersion = "1.3"
            apiVersion = "1.3"
        }
    }
}