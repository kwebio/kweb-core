package buildsrc.conventions

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("buildsrc.conventions.base")
    kotlin("jvm")
}

val projectJvmVersion = 17

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "$projectJvmVersion"
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
