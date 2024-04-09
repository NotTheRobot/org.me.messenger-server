package org.me

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.me.data.DatabaseSingleton
import org.me.plugins.*
import java.io.File

fun main() {
    val ip = File("ip.txt").readText()
    embeddedServer(Netty, port = 8080, host = ip, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseSingleton.init()
    configureSerialization()
    configureSockets()
    configureRouting()
}
