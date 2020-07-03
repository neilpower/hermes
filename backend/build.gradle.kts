import io.kotless.plugin.gradle.dsl.Webapp.Route53
import io.kotless.plugin.gradle.dsl.kotless

plugins {
    id("maven")
    id("java")
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    id("org.jetbrains.kotlin.jvm")
    id("application")
    id("distribution")
    id("com.github.johnrengelman.shadow")
    id("io.kotless") version "0.1.5" apply true
}

repositories {
    jcenter()
    mavenCentral()
}

val kotlinVersion = "1.3.72"
val ktorVersion = "1.3.2"
val logbackVersion = "1.2.3"
val fuelVersion = "2.2.3"
val exposedVersion = "0.25.1"
val awsVersion = "1.11.650"
val kotlessVersion = "0.1.5"

dependencies {
    implementation("io.kotless:ktor-lang:$kotlessVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // TODO Version
    implementation("com.amazonaws:aws-java-sdk-dynamodb:$awsVersion")

    // Fuel for sending http
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

kotless {
    config {
        bucket = "hermesapp-test"

        terraform {
            profile = "default"
            region = "us-west-2"
        }
    }

    webapp {
        // Optional parameter, by default technical name will be generated
        // route53 = Route53("kotless", "example.com")
    }

    extensions {
        local {
            useAWSEmulation = true
        }

        terraform {
            files {
                add(file("src/main/tf/extensions.tf"))
            }
        }
    }
}