package com.example.grit.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PropertyImage(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val id: String = "",
    @SerialName("property_id") val propertyId: String = "",
    @SerialName("foto_url")    val fotoUrl: String = "",
    val urutan: Int = 1,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("created_at")  val createdAt: String = ""
)
