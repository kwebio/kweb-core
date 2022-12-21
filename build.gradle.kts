import java.net.URL

plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
    id("org.jetbrains.dokka") version "1.7.20"
    id("com.github.ben-manes.versions") version "0.44.0"
    kotlin("plugin.serialization")

    // See api/API_README.md for details
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.12.1"

    // Coverage
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

// This is overridden by the maven release process
group = "com.github.kwebio"

// Don't set version here, it must be set in gradle.properties so it can be overridden
// by the build script

tasks.test {
    systemProperty("sel.jup.default.browser", System.getProperty("sel.jup.default.browser"))
}

dependencies {
    api("org.jsoup:jsoup:1.15.3")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.guava:guava:31.1-jre")
    api("dev.forkhandles:result4k:2.3.0.0")

    //////////////////////////////
    // Kotlin library dependencies
    //////////////////////////////

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    ////////////////////
    // Ktor dependencvies
    ////////////////////
    api("io.ktor:ktor-server-jetty:2.2.1")
    api("io.ktor:ktor-server-websockets:2.2.1")
    api("io.ktor:ktor-server-default-headers:2.2.1")
    api("io.ktor:ktor-server-compression:2.2.1")
    api("io.ktor:ktor-server-caching-headers:2.2.1")
    api("io.ktor:ktor-network-tls-certificates:2.2.1")

    api("io.mola.galimatias:galimatias:0.2.1")

    implementation("io.github.microutils:kotlin-logging:3.0.4")

    ///////////////////////////
    // Dependencies for testing
    ///////////////////////////
    testApi(platform("io.kotest:kotest-bom:5.5.4"))
    testApi(platform("org.junit:junit-bom:5.9.1"))

    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-assertions-core")

    testImplementation("ch.qos.logback:logback-classic:1.4.5")

    testImplementation("org.seleniumhq.selenium:selenium-opera-driver:4.4.0")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:4.7.2")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.7.2")
    testImplementation("io.github.bonigarcia:selenium-jupiter:4.3.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.awaitility:awaitility:4.2.0")
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            includes.from("src/main/kotlin/packages.md")
        }

        named("main") {
            // Convert this to Kotlin
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/kwebio/kweb-core/tree/master/src/main/kotlin"))
            }
        }
        // Converted

    }
}
