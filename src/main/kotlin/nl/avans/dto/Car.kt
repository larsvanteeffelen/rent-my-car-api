package nl.avans.dto

import kotlinx.serialization.Serializable

@Serializable
data class Car(
    val id: Int? = null,
    val make: String,
    val model: String,
    val type: String,
    val rentalPrice: Double,
    val latitude: Double,
    val longitude: Double
)
