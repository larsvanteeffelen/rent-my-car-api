package nl.avans.dao

import kotlinx.coroutines.*
import nl.avans.dto.Car
import java.sql.Connection
import java.sql.Statement


class CarDAO(private val connection: Connection) {

    companion object {
        private const val CREATE_TABLE_CAR = "CREATE TABLE IF NOT EXISTS car (ID SERIAL PRIMARY KEY, make VARCHAR(255), MODEL VARCHAR(255), TYPE VARCHAR(255), RENTALPRICE DOUBLE PRECISION, OWNERID INT, LATITUDE DOUBLE PRECISION, LONGITUDE DOUBLE PRECISION);"
        private const val SELECT_ALL_CARS = "SELECT id, make, model, type, rentalprice, latitude, longitude FROM car"
        private const val SELECT_AVAILABLE_CARS = "SELECT id, make, model, type, rentalprice, latitude, longitude FROM car WHERE id NOT IN (SELECT carid FROM booking WHERE current_timestamp BETWEEN startTime AND endTime)"
        private const val SELECT_CAR_BY_ID = "SELECT make, model, type, rentalprice, latitude, longitude FROM car WHERE id = ?"
        private const val INSERT_CAR = "INSERT INTO car (make, model, type, rentalprice, latitude, longitude, ownerid) VALUES (?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_CAR = "UPDATE car SET make = ?, model = ?, type = ?, rentalprice = ?, latitude = ?, longitude = ? WHERE id = ?"
        private const val DELETE_CAR = "DELETE FROM car WHERE id = ?"
        private const val SELECT_AVAILABLE_CARS_IN_RANGE = "SELECT * FROM car WHERE 6371 * ACOS(SIN(RADIANS(?)) * SIN(RADIANS(latitude)) + COS(RADIANS(?)) * COS(RADIANS(latitude)) * COS(RADIANS(? - longitude))) * 1000 <= ?;"
        private const val SELECT_ALL_CARS_BY_OWNER = "SELECT * FROM car where ownerId = ?"

    }

    init {
        val statementCreate = connection.createStatement()
        statementCreate.executeUpdate(CREATE_TABLE_CAR)
    }

    // Create new car
    suspend fun create(car: Car): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_CAR, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, car.make)
        statement.setString(2, car.model)
        statement.setString(3, car.type)
        statement.setDouble(4, car.rentalPrice)
        statement.setDouble(5, car.latitude)
        statement.setDouble(6, car.longitude)
        statement.setInt(7, car.ownerId)

        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted car")

        }
    }

    suspend fun readAll(): List<Car> = withContext(Dispatchers.IO) {
        val cars = mutableListOf<Car>()

        val statement = connection.prepareStatement(SELECT_ALL_CARS)
        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val ownerId = resultSet.getInt("ownerid")
            val make = resultSet.getString("make")
            val model = resultSet.getString("model")
            val type = resultSet.getString("type")
            val rentalprice = resultSet.getDouble("rentalprice")
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")

            val car = Car(id, ownerId, make, model, type, rentalprice, latitude, longitude)
            cars.add(car)
        }

        if (cars.isNotEmpty()) {
            return@withContext cars
        } else {
            throw Exception("No cars found")
        }
    }
    suspend fun readAvailable(): List<Car> = withContext(Dispatchers.IO) {
        val cars = mutableListOf<Car>()
        val statement = connection.prepareStatement(SELECT_AVAILABLE_CARS)

        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val ownerId = resultSet.getInt("ownerid")
            val make = resultSet.getString("make")
            val model = resultSet.getString("model")
            val type = resultSet.getString("type")
            val rentalprice = resultSet.getDouble("rentalprice")
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")

            val car = Car(id, ownerId, make, model, type, rentalprice, latitude, longitude)
            cars.add(car)
        }

        if (cars.isNotEmpty()) {
            return@withContext cars
        } else {
            throw Exception("No cars found")
        }
    }

    suspend fun readAvailableCarsInRange(lat: Double, long: Double, km: Int): List<Car> = withContext(Dispatchers.IO) {
        val cars = mutableListOf<Car>()
        val statement = connection.prepareStatement(SELECT_AVAILABLE_CARS_IN_RANGE)
        statement.setDouble(1, lat)
        statement.setDouble(2, lat)
        statement.setDouble(3, long)
        statement.setInt(4, km * 1000)


        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val ownerId = resultSet.getInt("ownerid")
            val make = resultSet.getString("make")
            val model = resultSet.getString("model")
            val type = resultSet.getString("type")
            val rentalprice = resultSet.getDouble("rentalprice")
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")

            val car = Car(id, ownerId, make, model, type, rentalprice, latitude, longitude)
            cars.add(car)
        }

        if (cars.isNotEmpty()) {
            return@withContext cars
        } else {
            throw Exception("No cars found")
        }
    }


    // Read a car
    suspend fun read(id: Int): Car = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CAR_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val ownerId = resultSet.getInt("ownerId")
            val make = resultSet.getString("make")
            val model = resultSet.getString("model")
            val type = resultSet.getString("type")
            val rentalprice = resultSet.getDouble("rentalprice")
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")

            return@withContext Car(id, ownerId, make, model, type, rentalprice, latitude, longitude)
        } else {
            throw Exception("Record not found")
        }
    }

    // Read a car by ower id
    suspend fun readByOwner(ownerId: Int):  List<Car> = withContext(Dispatchers.IO) {
        val cars = mutableListOf<Car>()
        val statement = connection.prepareStatement(SELECT_ALL_CARS_BY_OWNER)
        statement.setInt(1, ownerId)
        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val make = resultSet.getString("make")
            val model = resultSet.getString("model")
            val type = resultSet.getString("type")
            val rentalprice = resultSet.getDouble("rentalprice")
            val latitude = resultSet.getDouble("latitude")
            val longitude = resultSet.getDouble("longitude")

            cars.add(Car(id, ownerId, make, model, type, rentalprice, latitude, longitude))
        }
        if (cars.isNotEmpty()){
            return@withContext cars
        } else{
            throw Exception("No cars found")
        }
    }

    // Update a car
    suspend fun update(id: Int, car: Car) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CAR)
        statement.setString(1, car.make)
        statement.setString(2, car.model)
        statement.setString(3, car.type)
        statement.setDouble(4, car.rentalPrice)
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