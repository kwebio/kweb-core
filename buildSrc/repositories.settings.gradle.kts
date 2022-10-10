// Centralized Repository configuration, that will be shared between both buildSrc and the root project

@Suppress("UnstableApiUsage") // Central declaration of repositories is an incubating feature
dependencyResolutionManagement {

    repositories {
        mavenCentral()
        jitpack()
        gradlePluginPortal()
    }

    pluginManagement {
        repositories {
            gradlePluginPortal()
            mavenCentral()
            jitpack()
        }
    }
}

fun RepositoryHandler.jitpack() {
    maven("https://jitpack.io")
}
