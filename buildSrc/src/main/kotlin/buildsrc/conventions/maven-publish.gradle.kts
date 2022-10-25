package buildsrc.conventions

import java.util.Base64

plugins {
    id("buildsrc.conventions.base")
    `maven-publish`
    signing
}

// the Signing and OSSRH Properties should be defined in $GRADLE_USER_HOME (on developer's machines
// or in environment variables prefixed with ORG_GRADLE_PROJECT_ on CI/CD
// https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties

val signingKeyId = providers.gradleProperty("signingKeyId")
val signingKey = providers
    .gradleProperty("signingKey")
    .map { String(Base64.getUrlDecoder().decode(it)) }
val signingPassword = providers.gradleProperty("signingPassword")

val signingPropertiesPresent = provider {
    signingKey.isPresent && signingKeyId.isPresent && signingPassword.isPresent
}

val ossrhUsername = providers.gradleProperty("ossrhUsername")
val ossrhPassword = providers.gradleProperty("ossrhPassword")

val isSnapshotVersion = provider { version.toString().endsWith("SNAPSHOT") }

val javadocJarStub by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Stub empty javadoc.jar artifact (a Javadoc jar is required by Maven Central)"
    archiveClassifier.set("javadoc")
}


publishing {
    publications.withType<MavenPublication>().configureEach {
        groupId = "io.kweb"
        artifactId = "kweb-core"
        version = project.version.toString()

        artifact(javadocJarStub)

        // apply default configs for all Maven publications
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
                }
            }
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
         /*
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                name = "SonatypeStaging"
                credentials {
                    username = ossrhUsername.get()
                    password = ossrhPassword.get()
                }
            }
*/
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
        logger.lifecycle("[${project.displayName}] Signing is enabled")
        useInMemoryPgpKeys(signingKey.get(), signingPassword.get())

        // Gradle hasn't updated the signing plugin to be compatible with lazy-configuration,
        // so it needs weird workarounds: https://github.com/gradle/gradle/issues/19903
        sign(closureOf<SignOperation> { sign(publishing.publications) })
    } else {
        logger.info("Signing is not enabled")
    }
}
