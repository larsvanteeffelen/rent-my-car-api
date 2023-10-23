package nl.avans.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.avans.dto.CarPicture
import java.sql.Connection
import java.sql.Statement

class CarPictureDAO(private val connection: Connection) {

    companion object {
        private const val CREATE_TABLE_CAR_PICTURE = "CREATE TABLE IF NOT EXISTS car_picture (ID SERIAL PRIMARY KEY, carId INT, pictureUrl VARCHAR(255));"
        private const val SELECT_CAR_PICTURE_BY_ID = "SELECT id, carId, pictureUrl FROM car_picture WHERE id = ?"
        private const val SELECT_CAR_PICTURES_BY_CAR_ID = "SELECT id, carId, pictureUrl FROM car_picture WHERE carId = ?"
        private const val INSERT_CAR_PICTURE = "INSERT INTO car_picture (carId, pictureUrl) VALUES (?, ?)"
        private const val UPDATE_CAR_PICTURE = "UPDATE car_picture SET carId = ?, pictureUrl = ? WHERE id = ?"
        private const val DELETE_CAR_PICTURE = "DELETE FROM car_picture WHERE id = ?"
    }

    init {
        val statementCreate = connection.createStatement()
        statementCreate.executeUpdate(CREATE_TABLE_CAR_PICTURE)
    }

    // Create a new car picture
    suspend fun create(carPicture: CarPicture): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_CAR_PICTURE, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, carPicture.carId)
        statement.setString(2, carPicture.pictureUrl)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted car picture")
        }
    }

    // Read a car picture by ID
    suspend fun read(id: Int): CarPicture = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CAR_PICTURE_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val carId = resultSet.getInt("carId")
            val pictureUrl = resultSet.getString("pictureUrl")

            return@withContext CarPicture(id, carId, pictureUrl)
        } else {
            throw Exception("Car picture not found")
        }
    }

    // Read all car pictures for a specific car by car ID
    suspend fun readAllByCarId(carId: Int): List<CarPicture> = withContext(Dispatchers.IO) {
        val carPictures = mutableListOf<CarPicture>()

        val statement = connection.prepareStatement(SELECT_CAR_PICTURES_BY_CAR_ID)
        statement.setInt(1, carId)
        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val pictureUrl = resultSet.getString("pictureUrl")

            val carPicture = CarPicture(id, carId, pictureUrl)
            carPictures.add(carPicture)
        }

        return@withContext carPictures
    }

    // Update a car picture
    suspend fun update(id: Int, carPicture: CarPicture) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CAR_PICTURE)
        statement.setInt(1, carPicture.carId)
        statement.setString(2, carPicture.pictureUrl)
        statement.setInt(3, id)
        statement.executeUpdate()
    }

    // Delete a car picture by ID
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CAR_PICTURE)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}
