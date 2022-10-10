package buildsrc.conventions

plugins {
    id("buildsrc.conventions.base")
    `maven-publish`
}

publishing {

    publications.withType<MavenPublication>().configureEach {
        // apply default configs for all Maven publications
        pom {
            scm {
                connection.set("scm:git:git://github.com/kwebio/kweb-core.git")
                developerConnection.set("scm:git:ssh://github.com:kwebio/kweb-core.git")
                url.set("https://github.com/kwebio/kweb-core")
            }
        }
    }

    repositories {
        // Publish to a project-local Maven directory, for verification. To test, run:
        // ./gradlew publishAllPublicationsToMavenProjectLocalRepository
        // and check $rootDir/build/maven-project-local
        maven(rootProject.layout.buildDirectory.dir("maven-project-local")) {
            name = "MavenProjectLocal"
        }
    }
}

plugins.withType<JavaPlugin>().configureEach {
    // only create a Java publication when the Java plugin is applied
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
}
