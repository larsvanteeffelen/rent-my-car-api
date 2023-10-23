package nl.avans

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import nl.avans.dao.BookingDAO
import nl.avans.plugins.*
import nl.avans.routes.cars.configureCarRouting
import nl.avans.routes.users.configureUserRouting
import nl.avans.dao.CarDAO
import nl.avans.dao.CarPictureDAO
import nl.avans.dao.UserDAO
import nl.avans.routes.bookings.configureBookingRouting
import nl.avans.routes.carpictures.configureCarPictureRouting
import java.sql.Connection

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)


}

fun Application.module() {

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val dbConnection: Connection = connectToPostgres(embedded = true)

    val carDAO = CarDAO(dbConnection)
    val userDAO = UserDAO(dbConnection)
    val bookingDAO = BookingDAO(dbConnection)
    val carPictureDAO = CarPictureDAO(dbConnection)

    configureSecurity()
    configureSerialization()

    configureCarRouting(carDAO)
    configureUserRouting(userDAO)
    configureBookingRouting(bookingDAO)
    configureCarPictureRouting(carPictureDAO)
}
