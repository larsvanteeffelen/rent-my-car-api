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
import nl.avans.dao.CarDAO
import nl.avans.dao.UserDAO
import nl.avans.dao.BookingDAO
import nl.avans.dto.Car
import nl.avans.dto.User
import kotlin.test.*
import nl.avans.plugins.*
import nl.avans.routes.bookings.configureBookingRouting
import nl.avans.routes.cars.configureCarRouting
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
                setBody(
                    """{
                    "name": "Jeroen",
                    "address": "Hoogeschoollaan 1",
                    "zipcode": "4818CR",
                    "city": "Breda",
                    "email": "jeroen@example.com",
                    "drivingScore": 8
                    }"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Created, response.status())

                // Verify the response content (user ID)
                // If the user table is empty then will pass this test with every run there is one user added by the registration test.
                val id = response.content
                // Check that retrieving the user returns status code 200 OK
                handleRequest(HttpMethod.Get, "/user/$id") {
                    addHeader("Content-Type", ContentType.Application.Json.toString())
                }.apply { assertEquals(HttpStatusCode.OK, response.status()) }

            }
        }
    }

    @Test
    fun testUserLogin() {
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
            handleRequest(HttpMethod.Get, "/user/1") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testAvailableCars() {
        val connection = connectToPostgres(embedded = true) // Use the embedded database

        // Initialize the CarDAO with the database connection
        val carDAO = connection?.let { CarDAO(it) }
        val bookingDAO = connection?.let { BookingDAO(it) }

        var bookingid = 0;
        var carID = 0;

        withTestApplication({
            configureSecurity()
            configureSerialization()
            if (carDAO != null) {
                configureCarRouting(carDAO)
            }
            if (bookingDAO != null) {
                configureBookingRouting(bookingDAO)
            }
        })

//        Create the testcar
        {
            handleRequest(HttpMethod.Post, "/car") {
                setBody(
                    """{
                    "make": "Test",
                    "model": "Test",
                    "type": "Test",
                    "rentalPrice": "1000",
                    "latitude": "51.58440",
                    "longitude": "4.79759"
                }"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Created, response.status())

                carID = response.content!!.toInt()
            }

            // Make sure that the car is in the available cars list
            handleRequest(HttpMethod.Get, "/car/available") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())

                // Verify that the testcar is not in the list of available cars
                assertEquals(response.content!!.contains("\"id\":$carID"), true)


            }


            // Create a booking for the next hour
            val currentTimeStamp = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16) + ":00"
            val currentTimeStampPlusHour =
                java.time.LocalDateTime.now().plusHours(1).toString().replace("T", " ").substring(0, 16) + ":00"
            handleRequest(HttpMethod.Post, "/booking") {
                setBody(
                    """{
                    "userId": "1",
                    "carId": "$carID",
                    "startTime": "$currentTimeStamp",
                    "endTime": "$currentTimeStampPlusHour"
                }"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Created, response.status())

                bookingid = response.content!!.toInt()
            }

            // Make sure that the car is not in the available cars list
            handleRequest(HttpMethod.Get, "/car/available") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())

                // Verify that the testcar is not in the list of available cars
                assertNotEquals(response.content!!.contains("\"id\":$carID"), true)


            }

            // Delete the booking
            handleRequest(HttpMethod.Delete, "/booking/$bookingid") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())
            }

            // Delete the testcar
            handleRequest(HttpMethod.Delete, "/car/$carID") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testCarDetails() {
        val connection = connectToPostgres(embedded = true) // Use the embedded database

        // Initialize the CarDAO with the database connection
        val carDAO = connection?.let { CarDAO(it) }
        var carID = 0;
        withTestApplication({
            configureSecurity()
            configureSerialization()
            if (carDAO != null) {
                configureCarRouting(carDAO)
            }
        }) {

            handleRequest(HttpMethod.Post, "/car") {
                setBody(
                    """{
                    "make": "TestMake",
                    "model": "TestModel",
                    "type": "TestType",
                    "rentalPrice": "500",
                    "latitude": "15",
                    "longitude": "10"
                }"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Created, response.status())

                carID = response.content!!.toInt()
            }


            handleRequest(HttpMethod.Get, "/car/$carID") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())

                assertNotEquals(response.content, null)
                assertContains(response.content!!, "TestMake")
                assertContains(response.content!!, "TestModel")
                assertContains(response.content!!, "TestType")
                assertContains(response.content!!, "500")
                assertContains(response.content!!, "15")
                assertContains(response.content!!, "10")


            }

            // Delete the testcar
            handleRequest(HttpMethod.Delete, "/car/$carID") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    //    Test should fail
    @Test
    fun cancelBooking() {
        val connection = connectToPostgres(embedded = true) // Use the embedded database

        val bookingDAO = connection?.let { BookingDAO(it) }

        var bookingid = 0;

        withTestApplication({
            configureSecurity()
            configureSerialization()
            if (bookingDAO != null) {
                configureBookingRouting(bookingDAO)
            }
        })

        {
            // Create a booking for the next hour
            val currentTimeStamp = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16) + ":00"
            val currentTimeStampPlusHour =
                java.time.LocalDateTime.now().plusHours(1).toString().replace("T", " ").substring(0, 16) + ":00"
            handleRequest(HttpMethod.Post, "/booking") {
                setBody(
                    """{
                    "userId": "1",
                    "carId": "1",
                    "startTime": "$currentTimeStamp",
                    "endTime": "$currentTimeStampPlusHour"
                }"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Created, response.status())

                bookingid = response.content!!.toInt()
            }


            // Delete the booking
            handleRequest(HttpMethod.Delete, "/booking/$bookingid") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())
            }

        }
    }

    @Test
    fun testDoubleBooking(){
        val connection = connectToPostgres(embedded = true) // Use the embedded database

        val bookingDAO = connection?.let { BookingDAO(it) }

        var bookingid = 0;

        withTestApplication({
            configureSecurity()
            configureSerialization()
            if (bookingDAO != null) {
                configureBookingRouting(bookingDAO)
            }
        })

        {
            // Create a booking for the next hour
            val currentTimeStamp = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16) + ":00"
            val currentTimeStampPlusHour =
                java.time.LocalDateTime.now().plusHours(1).toString().replace("T", " ").substring(0, 16) + ":00"
            handleRequest(HttpMethod.Post, "/booking") {
                setBody(
                    """{
                    "userId": "1",
                    "carId": "1",
                    "startTime": "$currentTimeStamp",
                    "endTime": "$currentTimeStampPlusHour"
                }"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Created, response.status())

                bookingid = response.content!!.toInt()
            }

            // Try to create a booking for the same car in the same timeframe
            handleRequest(HttpMethod.Post, "/booking") {
                setBody(
                    """{
                    "userId": "1",
                    "carId": "1",
                    "startTime": "$currentTimeStamp",
                    "endTime": "$currentTimeStampPlusHour"
                }"""
                )
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.Conflict, response.status())
            }


            // Delete the booking
            handleRequest(HttpMethod.Delete, "/booking/$bookingid") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
            }.apply {
                // Verify the response status code
                assertEquals(HttpStatusCode.OK, response.status())
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

