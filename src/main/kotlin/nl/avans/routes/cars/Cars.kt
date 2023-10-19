package nl.avans.routes.cars

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.avans.dao.CarDAO
import nl.avans.dto.Car

fun Application.configureCarRouting(carDAO: CarDAO) {

    routing {
        // Create car
        post("/car") {
            val car = call.receive<Car>()
            val id = carDAO.create(car)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read car
        get("/car/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val car = carDAO.read(id)
                call.respond(HttpStatusCode.OK, car)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update car
        put("/car/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val car = call.receive<Car>()
            carDAO.update(id, car)
            call.respond(HttpStatusCode.OK)
        }
        // Delete car
        delete("/car/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            carDAO.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}