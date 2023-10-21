package nl.avans

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import io.ktor.http.cio.*
import io.ktor.server.response.*
import io.ktor.server.testing.*
import io.ktor.server.util.*
import nl.avans.dao.UserDAO
import nl.avans.dto.User
import kotlin.test.*
import nl.avans.plugins.*
import nl.avans.routes.users.configureUserRouting
import org.h2.util.JdbcUtils.serializer
import java.sql.Connection
import java.sql.DriverManager
import kotlin.text.get


class ApplicationTest {
    @Test
    fun testUserRegistrationSuccess() {
        val connection = connectToPostgres(embedded = true) // Use the embedded database

        // Initialize the UserDAO with the database connection
        val userDAO = connection?.let { UserDAO(it) }

        withTestApplication({
            configureSecurity()
            configureSerialization()
            if (userDAO != null) {
                configureUserRouting(userDAO)
            }
        }) {
            handleRequest(HttpMethod.Post, "/user") {
                setBody("""{
                    "name": "Jeroen",
                    "address": "Hoogeschoollaan 1",
                    "zipcode": "4818CR",
                    "city": "Breda",
                    "email": "jeroen@example.com",
                    "drivingscore": 8
                    }""")
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Created, response.status())

                // Verify the response content (user ID)
                // If the user table is empty then will pass this test with every run there is one user added by the registration test.
                val id = response.content
                assertEquals("10", id)
            }
        }
    }
// Connecton with the database postgresSQL in DOcker environment.
    private fun connectToPostgres(embedded: Boolean): Connection? {
        Class.forName("org.postgresql.Driver")
        if (embedded) {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/rent_my_car_db", "postgres", "root")
        } else {
            val url = "your_database_url_here"
            val user = "your_database_user_here"
            val password = "your_database_password_here"

            return DriverManager.getConnection(url, user, password)
        }
    }
}

