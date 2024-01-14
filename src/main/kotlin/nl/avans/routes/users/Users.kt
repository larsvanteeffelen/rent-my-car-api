package nl.avans.routes.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.avans.dao.UserDAO
import nl.avans.dto.User

fun Application.configureUserRouting(userDAO: UserDAO) {

    routing {
        // Create user
        post("/user") {
            try {
                val user = call.receive<User>()
                val id = userDAO.create(user)
                call.respond(HttpStatusCode.Created, id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "User values are incorrect!")
            }

        }
        // Read user
        get("/user/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                try {
                    val user = userDAO.read(id)
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }

        get("/user") {
            val authId = call.parameters["authId"].toString()
            if (authId != null) {
                try {
                    val user = userDAO.readUserFromAuthId(authId)
                    call.respond(HttpStatusCode.OK, user)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid AuthID")
            }
        }
        // Update user
        put("/user/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if(id != null) {
                try {
                    val user = call.receive<User>()
                    userDAO.update(id, user)
                    call.respond(HttpStatusCode.OK)
                } catch (e: BadRequestException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request, check the values")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
        // Delete user
        delete("/user/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                userDAO.delete(id)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            }
        }
    }
}
