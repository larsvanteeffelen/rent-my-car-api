package nl.avans.routes.cars

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.avans.services.Car
import nl.avans.services.CarService
import nl.avans.plugins.connectToPostgres
import java.sql.Connection

fun Application.configureCarRouting() {

    val dbConnection: Connection = connectToPostgres(embedded = true)
    val carService = CarService(dbConnection)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        // Create car
        post("/car") {
            val car = call.receive<Car>()
            val id = carService.create(car)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read car
        get("/car/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val car = carService.read(id)
                call.respond(HttpStatusCode.OK, car)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update car
        put("/car/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val car = call.receive<Car>()
            carService.update(id, car)
            call.respond(HttpStatusCode.OK)
        }
        // Delete car
        delete("/car/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            carService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}