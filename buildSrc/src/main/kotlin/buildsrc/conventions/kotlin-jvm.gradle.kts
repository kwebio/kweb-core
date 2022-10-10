package buildsrc.conventions

import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
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
