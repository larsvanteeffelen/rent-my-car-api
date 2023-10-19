package nl.avans.routes.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.avans.dao.UserDAO
import nl.avans.dto.User

fun Application.configureUserRouting(userDAO: UserDAO) {

    routing {
        // Create user
        post("/user") {
            val user = call.receive<User>()
            val id = userDAO.create(user)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read user
        get("/user/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val user = userDAO.read(id)
                call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update user
        put("/user/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<User>()
            userDAO.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
        // Delete user
        delete("/user/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userDAO.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
