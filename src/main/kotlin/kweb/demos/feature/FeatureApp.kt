package kweb.demos.feature

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.websocket.WebSockets
import kweb.*
import kweb.state.KVar
import java.time.Duration

fun main() {
    embeddedServer(Jetty, port = 16097, module = Application::kwebFeature).start()
}

private fun Application.kwebFeature() {
    install(DefaultHeaders)
    install(Compression)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
        timeout = Duration.ofSeconds(30)
    }

    install(Kweb) {
        buildPage = {
            doc.body.new {
                val path = url(simpleUrlParser).path
                val greeting = path.map { it.removePrefix("/") }.map { "Hello " + if (it.isNotBlank()) it else "World" }

                val next = KVar("")

                h1().text(greeting)
                span().text("Where to next?")
                input().setValue(next)
                button().text("Go!").on.click {
                    path.value = next.value
                }
            }
        }
    }
}
