import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.main

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.google.protobuf") version "0.9.4"
    application
}

group = "com.polytradings"
version = "0.1.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // NATS
    implementation("io.nats:jnats:2.20.4")

    // Protobuf
    implementation("com.google.protobuf:protobuf-java:3.25.5")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.15")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
}

application {
    mainClass.set("com.polytradings.betaction.BetActionApplicationKt")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
}

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
        java {
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    exclude("**/*.proto")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateProto")
    kotlinOptions {
        jvmTarget = "21"
    }
}
