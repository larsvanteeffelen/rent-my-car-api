package nl.avans.dto

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val address: String,
    val zipcode: String,
    val city: String,
    val email: String,
    val drivingscore: Int
)