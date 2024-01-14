package nl.avans.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.avans.dto.User
import java.sql.Connection
import java.sql.Statement

class UserDAO(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS \"user\" (ID SERIAL PRIMARY KEY, NAME VARCHAR(255), ADDRESS VARCHAR(255), ZIPCODE VARCHAR(20), CITY VARCHAR(255), EMAIL VARCHAR(255), DRIVINGSCORE INT, AUTHID VARCHAR(255));"
        private const val SELECT_USER_BY_ID = "SELECT * FROM \"user\" WHERE id = ?"
        private const val INSERT_USER = "INSERT INTO \"user\" (name, address, zipcode, city, email, drivingscore, authid) VALUES (?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_USER = "UPDATE \"user\" SET name = ?, address = ?, zipcode = ?, city = ?, email = ?, drivingscore = ? WHERE id = ?"
        private const val DELETE_USER = "DELETE FROM \"user\" WHERE id = ?"
    }


    init {
        val statementCreate = connection.createStatement()
        statementCreate.executeUpdate(CREATE_TABLE_USER)
    }

    suspend fun create(user: User): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, user.name)
        statement.setString(2, user.address)
        statement.setString(3, user.zipcode)
        statement.setString(4, user.city)
        statement.setString(5, user.email)
        statement.setInt(6, user.drivingScore)
        statement.setString(7, user.authId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted user")
        }
    }

    suspend fun read(id: Int): User = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USER_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val name = resultSet.getString("name")
            val address = resultSet.getString("address")
            val zipcode = resultSet.getString("zipcode")
            val city = resultSet.getString("city")
            val email = resultSet.getString("email")
            val drivingscore = resultSet.getInt("drivingscore")
            val authid = resultSet.getString("authid")

            return@withContext User(id, name, address, zipcode, city, email, drivingscore, authid)
        } else {
            throw Exception("Record not found")
        }
    }

    suspend fun update(id: Int, user: User) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_USER)
        statement.setString(1, user.name)
        statement.setString(2, user.address)
        statement.setString(3, user.zipcode)
        statement.setString(4, user.city)
        statement.setString(5, user.email)
        statement.setInt(6, user.drivingScore)
        statement.setInt(7, id)
        statement.executeUpdate()
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_USER)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}
