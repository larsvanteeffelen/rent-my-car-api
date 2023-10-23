package nl.avans.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.avans.dto.Booking
import java.sql.Connection
import java.sql.Statement
import java.sql.Timestamp
import java.text.SimpleDateFormat

class BookingDAO(private val connection: Connection) {

    companion object {
        private const val CREATE_TABLE_BOOKING = "CREATE TABLE IF NOT EXISTS booking (ID SERIAL PRIMARY KEY, carId INT, userId INT, startTime VARCHAR(255), endTime VARCHAR(255));"
        private const val SELECT_BOOKING_BY_ID = "SELECT id, carId, userId, startTime, endTime FROM booking WHERE id = ?"
        private const val SELECT_BOOKING_BY_TIMEFRAME = "SELECT id, carId, userId, startTime, endTime FROM booking WHERE carid = ? AND (? BETWEEN startTime AND endTime  OR ? BETWEEN  startTime AND endTime)"
        private const val INSERT_BOOKING = "INSERT INTO booking (carId, userId, startTime, endTime) VALUES (?, ?, ?, ?)"
        private const val UPDATE_BOOKING = "UPDATE booking SET carId = ?, userId = ?, startTime = ?, endTime = ? WHERE id = ?"
        private const val DELETE_BOOKING = "DELETE FROM booking WHERE id = ?"
    }

    init {
        val statementCreate = connection.createStatement()
        statementCreate.executeUpdate(CREATE_TABLE_BOOKING)
    }

    // Create new booking
    suspend fun create(booking: Booking): Int = withContext(Dispatchers.IO) {
        val checkStatement = connection.prepareStatement(SELECT_BOOKING_BY_TIMEFRAME)
        checkStatement.setInt(1, booking.carId)
        checkStatement.setTimestamp(2, convertDateToTimestamp(booking.startTime))
        checkStatement.setTimestamp(3, convertDateToTimestamp(booking.endTime))
        val checkResultSet = checkStatement.executeQuery()
        if(checkResultSet.next()) {
            throw Exception("Car is already booked in this timeframe")
        }

        val statement = connection.prepareStatement(INSERT_BOOKING, Statement.RETURN_GENERATED_KEYS)
        statement.setInt(1, booking.carId)
        statement.setInt(2, booking.userId)
        statement.setTimestamp(3, convertDateToTimestamp(booking.startTime))
        statement.setTimestamp(4, convertDateToTimestamp(booking.endTime))
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted booking")
        }
    }

    // Read a booking
    suspend fun read(id: Int): Booking = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_BOOKING_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val carId = resultSet.getInt("CarId")
            val userId = resultSet.getInt("UserId")
            val startTime = resultSet.getString("StartTime")
            val endTime = resultSet.getString("EndTime")

            return@withContext Booking(id, carId, userId, startTime, endTime)
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a booking
    suspend fun update(id: Int, booking: Booking) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_BOOKING)
        statement.setInt(1, booking.carId)
        statement.setInt(2, booking.userId)
        statement.setTimestamp(3, convertDateToTimestamp(booking.startTime))
        statement.setTimestamp(4, convertDateToTimestamp(booking.endTime))
        statement.setInt(5, id)
        statement.executeUpdate()
    }

    // Delete a booking
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_BOOKING)
        statement.setInt(1, id)
        statement.executeUpdate()
    }

    fun convertDateToTimestamp(date: String): Timestamp {
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val date = formatter.parse(date)
        val timeStampDate = Timestamp(date.getTime())
        return timeStampDate
    }
}
