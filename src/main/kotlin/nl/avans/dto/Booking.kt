package nl.avans.dto

import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
data class Booking(
    val id: Int? = null,
    val carId: Int,
    val userId: Int,
    val startTime: String,
    val endTime: String
)
