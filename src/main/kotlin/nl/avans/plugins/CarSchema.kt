package nl.avans.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class Car(val name: String, val brand: String)
class CarService(private val connection: Connection) {
    companion object {
        private const val DROP_TABLE_CAR =
            "DROP TABLE CAR;"
        private const val CREATE_TABLE_CAR =
            "CREATE TABLE CAR (ID SERIAL PRIMARY KEY, NAME VARCHAR(255), BRAND VARCHAR(255));"
        private const val SELECT_CAR_BY_ID = "SELECT name, brand FROM car WHERE id = ?"
        private const val INSERT_CAR = "INSERT INTO car (name, brand) VALUES (?, ?)"
        private const val UPDATE_CAR = "UPDATE car SET name = ?, brand = ? WHERE id = ?"
        private const val DELETE_CAR = "DELETE FROM car WHERE id = ?"

    }

    init {
        val statementDrop = connection.createStatement()
        statementDrop.executeUpdate(DROP_TABLE_CAR)
        val statementCreate = connection.createStatement()
        statementCreate.executeUpdate(CREATE_TABLE_CAR)
    }

    private var newCarId = 0

    // Create new car
    suspend fun create(car: Car): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_CAR, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, car.name)
        statement.setString(2, car.brand)
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
            val brand = resultSet.getString("brand")
            return@withContext Car(name, brand)
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a car
    suspend fun update(id: Int, car: Car) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CAR)
        statement.setString(1, car.name)
        statement.setString(2, car.brand)
        statement.setInt(3, id)
        statement.executeUpdate()
    }

    // Delete a car
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CAR)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}
