package nl.avans.routes.carpictures

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.avans.dao.CarPictureDAO
import nl.avans.dto.CarPicture

fun Application.configureCarPictureRouting(carPictureDAO: CarPictureDAO) {
    routing {
        // Create car picture
        post("/carpictures") {
            try {
                val carPicture = call.receive<CarPicture>()
                val id = carPictureDAO.create(carPicture)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, "Car picture values are incorrect!")
            }
        }

        // Read car picture by ID
        get("/carpictures/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                try {
                    val carPicture = carPictureDAO.read(id)
                    call.respond(HttpStatusCode.OK, carPicture)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        // Read all car pictures for a specific car by car ID
        get("/carpictures/bycar/{carId}") {
            val carId = call.parameters["carId"]?.toIntOrNull()
            if (carId != null) {
                try {
                    val carPictures = carPictureDAO.readAllByCarId(carId)
                    call.respond(HttpStatusCode.OK, carPictures)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid Car ID")
            }
        }

        // Update car picture
        put("/carpictures/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                try {
                    val carPicture = call.receive<CarPicture>()
                    carPictureDAO.update(id, carPicture)
                    call.respond(HttpStatusCode.OK)
                } catch (e: BadRequestException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request, check the values")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        // Delete car picture by ID
        delete("/carpictures/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                carPictureDAO.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
    }
}
