package nl.avans.dto

import kotlinx.serialization.Serializable

@Serializable
data class Car(
    val id: Int,
    val make: String,
    val model: String,
    val type: String,
    val rentalprice: Double,
    val latitude: Double,
    val longitude: Double
)
