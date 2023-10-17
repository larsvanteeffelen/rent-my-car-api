package nl.avans

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import nl.avans.plugins.*
import nl.avans.routes.cars.configureCarRouting

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureCarRouting()
}
