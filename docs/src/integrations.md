# Integrations

<!-- toc -->

## Spring Boot
To integrate Kweb into a Spring Boot Web application you need a Spring Boot project with the web starter, which can be easily obtained from [Spring initializr](https://start.spring.io/#!language=kotlin&dependencies=web).
Once you have a Spring Boot Web project, simply follow the [Getting Started](https://docs.kweb.io/book/gettingstarted.html) guide to add Kweb to your project.

For ease of demonstration here is all the code you need to integrate Kweb into Spring Boot:

```kotlin
package com.example.kwebspringboot

import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.servlet.*
import io.ktor.server.websocket.*
import kweb.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import java.time.Duration
import javax.servlet.annotation.WebServlet

@ServletComponentScan
@SpringBootApplication
class KwebSpringBootApplication {

    @WebServlet(urlPatterns = ["/*"], asyncSupported = true)
    class KwebServlet : ServletApplicationEngine() {

        override fun init() {
            super.init()

            application.install(DefaultHeaders)
            application.install(Compression)
            application.install(WebSockets) {
                pingPeriod = Duration.ofSeconds(10)
                timeout = Duration.ofSeconds(30)
            }

            application.install(Kweb) {
                buildPage = {
                    doc.body {
                        div {
                            h1().text("Enter Your Name")
                            val nameInput = input(type = InputType.text)
                            br()
                            span().text(nameInput.value.map { "Hello, $it" })
                        }
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KwebSpringBootApplication>(*args)
}
```

## Google Cloud Platform

### FireStore

Kweb can be used with Google Cloud Platform's FireStore. Our demo website defines some utility methods in
[firestore.kt](https://github.com/freenet/freenetorg-website/blob/staging/src/main/kotlin/org/freenet/website/util/firestore.kt),
and you can see how they're used in [news.kt](https://github.com/freenet/freenetorg-website/blob/staging/src/main/kotlin/org/freenet/website/landing/news.kt)
and [landing.kt](https://github.com/freenet/freenetorg-website/blob/staging/src/main/kotlin/org/freenet/website/landing/landing.kt#L85).
