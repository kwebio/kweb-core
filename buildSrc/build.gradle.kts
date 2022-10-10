import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

dependencies {
    implementation(platform(libs.kotlin.bom))

    // Import Gradle Plugins that will be used in the buildSrc pre-compiled script plugins, and any `build.gradle.kts`
    // files in the project.
    // Use their Maven coordinates (plus versions), not Gradle plugin IDs!
    // This should be the only place that Gradle plugin versions are defined, so they are aligned across all build scripts

    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.kotlinSerialization)
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.shadow)
    implementation(libs.gradlePlugin.gradleVersions)
}

val gradleJvmTarget = 11

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(gradleJvmTarget))
    }
}

kotlinDslPluginOptions {
    jvmTarget.set("$gradleJvmTarget")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "$gradleJvmTarget"
    }
}
