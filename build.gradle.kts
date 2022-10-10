plugins {
    buildsrc.conventions.`kotlin-jvm`
    buildsrc.conventions.`maven-publish`
    id("org.jetbrains.dokka")
    id("com.github.johnrengelman.shadow")
    id("com.github.ben-manes.versions")
    kotlin("plugin.serialization")
}

group = "com.github.kwebio"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(libs.jsoup)
    implementation(libs.apacheCommons.text)
    implementation(libs.google.guava)

    //////////////////////////////
    // Kotlin library dependencies
    //////////////////////////////

    implementation(platform(libs.kotlinxCoroutines.bom))
    api(libs.kotlinxCoroutines.core)
    api(libs.kotlinxCoroutines.jdk8)

    implementation(platform(libs.kotlinxSerialization.bom))
    api(libs.kotlinxSerialization.json)

    ////////////////////
    // Ktor dependencies
    ////////////////////
    implementation(platform(libs.ktor.bom))
    api(libs.ktorServer.jetty)
    api(libs.ktorServer.websockets)
    api(libs.ktorServer.defaultHeaders)
    api(libs.ktorServer.compression)
    api(libs.ktorServer.cachingHeaders)
    api(libs.ktorNetwork.tlsCertificates)

    api(libs.galimatias)

    implementation(libs.kotlinLogging)

    ///////////////////////////
    // Dependencies for testing
    ///////////////////////////
    // Pinned to 5.4.2 for now since there are issues with test discovery in 5.5.0
    // See: https://github.com/kotest/kotest/issues/3223
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junitJupiter.api)
    testImplementation(libs.junitJupiter.engine)

    testImplementation(platform(libs.kotest.bom))
    testImplementation(libs.kotest.runnerJunit5)
    testImplementation(libs.kotest.assertionsCore)

    testImplementation(libs.logbackClassic)

    testImplementation(libs.selenium.operaDriver)
    testImplementation(libs.selenium.chromeDriver)
    testImplementation(libs.selenium.java)
    testImplementation(libs.selenium.jupiter)
}

tasks.test {
    systemProperty("sel.jup.default.browser", System.getProperty("sel.jup.default.browser"))
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            samples.from(layout.projectDirectory.dir("src/main/kotlin/samples.kt"))
        }
    }
}
