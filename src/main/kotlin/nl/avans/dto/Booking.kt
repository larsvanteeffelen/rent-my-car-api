package nl.avans.dto

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Int? = null,
    val carId: Int,
    val userId: Int,
    val startTime: String,
    val endTime: String
)
