package com.example.grit.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PropertyImage(
    val id: String = "",
    @SerialName("property_id") val propertyId: String = "",
    @SerialName("foto_url")    val fotoUrl: String = "",
    val urutan: Int = 1,
    @SerialName("created_at")  val createdAt: String = ""
)
