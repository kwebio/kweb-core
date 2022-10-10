package buildsrc.conventions

plugins {
    base
}

description = "common Gradle configuration that should be applied to all projets"

if (project != rootProject) {
    version = rootProject.version
    group = rootProject.group
}
