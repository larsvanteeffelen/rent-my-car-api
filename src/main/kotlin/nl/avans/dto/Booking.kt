package nl.avans.dto

import java.sql.Timestamp

data class Booking(
    val id: Int,
    val carId: Int,
    val userId: Int,
    val startTime: Timestamp,
    val endTime: Timestamp
)
