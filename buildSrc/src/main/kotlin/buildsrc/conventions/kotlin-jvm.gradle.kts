package buildsrc.conventions

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("buildsrc.conventions.base")
    kotlin("jvm")

    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

val projectJvmVersion = 11

kotlin {
    jvmToolchain(projectJvmVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "$projectJvmVersion"
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
