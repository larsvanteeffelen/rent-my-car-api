package nl.avans.dto;

import kotlinx.serialization.Serializable;

@Serializable
data class CarPicture(
        val id: Int? = null,
        val carId: Int,
        val pictureUrl: String // URL to were the picture is stored. Example: /uploads/car/car123.jpg or remote: https://example.com/uploads/car/car123.jpg
)
