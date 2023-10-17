package nl.avans.services

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class Car(
    val name: String,
    val model: String,
    val type: String,
    val rentalprice: Double,
    val latitude: Double,
    val longitude: Double
)
class CarService(private val connection: Connection) {
    companion object {
        // This drops the table CAR. So delete this if database tables are ready
        private const val DROP_TABLE_CAR = "DROP TABLE CAR;"
        // This creates the tables.
        private const val CREATE_TABLE_CAR = "CREATE TABLE CAR (ID SERIAL PRIMARY KEY, NAME VARCHAR(255), MODEL VARCHAR(255), TYPE VARCHAR(255), RENTALPRICE DOUBLE PRECISION, LATITUDE DOUBLE PRECISION, LONGITUDE DOUBLE PRECISION);"

        private const val SELECT_CAR_BY_ID = "SELECT name, model, type, rentalprice, latitude, longitude FROM CAR WHERE id = ?"
        private const val INSERT_CAR = "INSERT INTO CAR (name, model, type, rentalprice, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)"
        private const val UPDATE_CAR = "UPDATE CAR SET name = ?, model = ?, type = ?, rentalprice = ?, latitude = ?, longitude = ? WHERE id = ?"
        private const val DELETE_CAR = "DELETE FROM CAR WHERE id = ?"

    }

    init {
        val statementDrop = connection.createStatement()
        statementDrop.executeUpdate(DROP_TABLE_CAR)

        val statementCreate = connection.createStatement()
        statementCreate.executeUpdate(CREATE_TABLE_CAR)
    }



    // Create new car
    suspend fun create(car: Car): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_CAR, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, car.name)
        statement.setString(2, car.model)
        statement.setString(3, car.type)
        statement.setDouble(4, car.rentalprice)
        statement.setDouble(5, car.latitude)
        statement.setDouble(6, car.longitude)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted car")
        }
    }

    // Read a car
    suspend fun read(id: Int): Car = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CAR_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val name = resultSet.getString("name")
            val model = resultSet.getString("model")
            val type = resultSet.getString("type")
            val rentalprice = resultSet.getDouble("rentalprice")
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")

            return@withContext Car(name, model, type, rentalprice, latitude, longitude)
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a car
    suspend fun update(id: Int, car: Car) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CAR)
        statement.setString(1, car.name)
        statement.setString(2, car.model)
        statement.setString(3, car.type)
        statement.setDouble(4, car.rentalprice)
        statement.setDouble(5, car.latitude)
        statement.setDouble(6, car.longitude)
        statement.setInt(7, id)
        statement.executeUpdate()
    }

    // Delete a car
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CAR)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}