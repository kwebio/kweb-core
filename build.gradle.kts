plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
    id("org.jetbrains.dokka") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.ben-manes.versions") version "0.42.0"
    kotlin("plugin.serialization")
}

group = "com.github.kwebio"
version = "0.0.1-SNAPSHOT"

tasks.test {
    systemProperty("sel.jup.default.browser", System.getProperty("sel.jup.default.browser"))
}

dependencies {
    api("org.jsoup:jsoup:1.15.3")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.guava:guava:31.1-jre")

    //////////////////////////////
    // Kotlin library dependencies
    //////////////////////////////

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    ////////////////////
    // Ktor dependencies
    ////////////////////
    api("io.ktor:ktor-server-jetty:2.1.2")
    api("io.ktor:ktor-server-websockets:2.1.2")
    api("io.ktor:ktor-server-default-headers:2.1.2")
    api("io.ktor:ktor-server-compression:2.1.2")
    api("io.ktor:ktor-server-caching-headers:2.1.2")
    api("io.ktor:ktor-network-tls-certificates:2.1.2")

    api("io.mola.galimatias:galimatias:0.2.1")

    implementation("io.github.microutils:kotlin-logging:3.0.0")

    ///////////////////////////
    // Dependencies for testing
    ///////////////////////////
    // Pinned to 5.4.2 for now since there are issues with test discovery in 5.5.0
    // See: https://github.com/kotest/kotest/issues/3223
    testApi(platform("io.kotest:kotest-bom:5.5.0"))
    testApi(platform("org.junit:junit-bom:5.9.1"))

    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-assertions-core")

    testImplementation("ch.qos.logback:logback-classic:1.4.3")

    testImplementation("org.seleniumhq.selenium:selenium-opera-driver:4.4.0")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:4.5.0")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.5.0")
    testImplementation("io.github.bonigarcia:selenium-jupiter:4.3.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            samples.from(layout.projectDirectory.dir("src/main/kotlin/samples.kt"))
        }
    }
}
