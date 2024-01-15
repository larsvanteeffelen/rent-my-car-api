package nl.avans.routes.cars

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.avans.dao.CarDAO
import nl.avans.dto.Car

fun Application.configureCarRouting(carDAO: CarDAO) {

    routing {
        // Create car
        post("/car") {
            try {
                val car = call.receive<Car>()
                val id = carDAO.create(car)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, "Car values are incorrect!")
            }
        }
        // Read car
        get("/car/all") {
            try {
                var ownerId = call.parameters["ownerId"]?.toIntOrNull()
                if(ownerId != null){
                    val cars = carDAO.readByOwner(ownerId)
                    call.respond(HttpStatusCode.OK, cars)
                }
                val cars = carDAO.readAll()
                call.respond(HttpStatusCode.OK, cars)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Read Currently Available Cars
        get("/car/available") {
            try {
                var long = call.parameters["longitude"]?.toDoubleOrNull()
                var lat = call.parameters["latitude"]?.toDoubleOrNull()
                var km = call.parameters["km"]?.toIntOrNull()

                if(long != null && lat != null && km != null) {
                    val carsInRange = carDAO.readAvailableCarsInRange(lat, long, km);
                    call.respond(HttpStatusCode.OK, carsInRange)
                }
                val cars = carDAO.readAvailable()
                call.respond(HttpStatusCode.OK, cars)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Read car
        get("/car/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                try {
                    val car = carDAO.read(id)
                    call.respond(HttpStatusCode.OK, car)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
        // Update car
        put("/car/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if(id != null) {
                try {
                    val car = call.receive<Car>()
                    carDAO.update(id, car)
                    call.respond(HttpStatusCode.OK)
                } catch (e: BadRequestException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request, check the values")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
        // Delete car
        delete("/car/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                carDAO.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
    }
}