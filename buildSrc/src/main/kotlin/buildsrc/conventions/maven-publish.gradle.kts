package buildsrc.conventions

plugins {
    id("buildsrc.conventions.base")
    `maven-publish`
    signing
    // NOTE: external plugin version is specified in implementation dependency artifact of the project's build file
    id("io.github.gradle-nexus.publish-plugin")
}

val signingKey = providers.gradleProperty("signingKeyB64")
val signingPassword = providers.gradleProperty("signingPassword")
//val signingSecretKeyRingFile = providers.gradleProperty("signingSecretKeyRingFile")

val signingPropertiesPresent = provider {
    signingKey.isPresent && signingPassword.isPresent
}

val ossrhUsername = providers.gradleProperty("ossrhUsername")

val ossrhPassword = providers.gradleProperty("ossrhPassword")

val isSnapshotVersion = provider { version.toString().endsWith("SNAPSHOT") }

val javadocJarStub by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Stub empty javadoc.jar artifact (a Javadoc jar is required by Maven Central)"
    archiveClassifier.set("javadoc")
}

nexusPublishing {
    repositories {
        sonatype {
            // This was a guess, trying without
            // this.stagingProfileId.set("io.kweb")
            username.set(ossrhUsername.get())
            password.set(ossrhPassword.get())
        }
    }
}
/*
publishing {
    publications.withType<MavenPublication>().configureEach {

        artifact(javadocJarStub)

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

        if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                name = "SonatypeStaging"
                credentials {
                    username = ossrhUsername.get()
                    password = ossrhPassword.get()
                }
            }

            if (!isSnapshotVersion.get()) {
                maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                    name = "SonatypeProduction"
                    credentials {
                        username = ossrhUsername.get()
                        password = ossrhPassword.get()
                    }
                }
            }
        }
    }
}
*/
plugins.withType<JavaPlugin>().configureEach {
    // only create a Java publication when the Java plugin is applied
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = "io.kweb"
                artifactId = "kweb-core"
                version = project.version.toString()
                pom {
                    name.set("Kweb")
                    description.set("A Kotlin web framework")
                    url.set("https://kweb.io/")
                    licenses {
                        license {
                            name.set("GNU Lesser General Public License v3.0")
                            url.set("https://www.gnu.org/licenses/lgpl-3.0.en.html")
                        }
                    }
                    developers {
                        developer {
                            id.set("sanity")
                            name.set("Ian Clarke")
                            email.set("ian.clarke@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/kwebio/kweb-core.git")
                        developerConnection.set("scm:git:ssh://github.com:kwebio/kweb-core.git")
                        url.set("https://github.com/kwebio/kweb-core")
                    }
                }

                from(components["java"])
            }
        }
    }
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    // Gradle warns about some signing tasks using publishing task outputs without explicit dependencies.
    // Here's a quick fix.
    dependsOn(tasks.withType<Sign>())
    mustRunAfter(tasks.withType<Sign>())

    doLast {
        logger.lifecycle("[task: ${this@configureEach.path}] ${publication.groupId}:${publication.artifactId}:${publication.version}")
    }
}

signing {

    if (signingPropertiesPresent.get()) {
        val decodedKey = String(java.util.Base64.getDecoder().decode(signingKey.get()))
        logger.debug("[${project.displayName}] Signing is enabled")
        useInMemoryPgpKeys(decodedKey, signingPassword.get())
        sign(publishing.publications)
    }
}
