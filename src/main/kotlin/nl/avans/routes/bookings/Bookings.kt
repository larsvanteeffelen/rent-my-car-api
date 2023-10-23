package nl.avans.routes.bookings

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.avans.dao.BookingDAO
import nl.avans.dto.Booking

fun Application.configureBookingRouting(bookingDAO: BookingDAO) {

    routing {
        // Create booking
        post("/booking") {
            try {
                val booking = call.receive<Booking>()
                val id = bookingDAO.create(booking)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, "Booking values are incorrect!")
            } catch (e: Exception) {
                if(e.message?.contains("Car is already booked in this timeframe") == true){
                    call.respond(HttpStatusCode.Conflict, "Car is already booked in this timeframe!")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Booking values are incorrect!")
                }
            }
        }

        // Read booking
        get("/booking/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                try {
                    val booking = bookingDAO.read(id)
                    call.respond(HttpStatusCode.OK, booking)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        // Update booking
        put("/booking/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if(id != null) {
                try {
                    val booking = call.receive<Booking>()
                    bookingDAO.update(id, booking)
                    call.respond(HttpStatusCode.OK)
                } catch (e: BadRequestException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request, check the values")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        // Delete booking
        delete("/booking/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                bookingDAO.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
    }
}
