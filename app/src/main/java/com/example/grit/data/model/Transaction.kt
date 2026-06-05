package com.example.grit.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Transaction(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val id: String = "",
    @SerialName("property_id") val propertyId: String = "",
    @SerialName("tenant_id") val tenantId: String = "",
    val harga: Long = 0,
    @SerialName("tanggal_mulai") val tanggalMulai: String = "",
    @SerialName("tanggal_selesai") val tanggalSelesai: String = "",
    val status: String = "active",
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("created_at") val createdAt: String = "",
    @kotlinx.serialization.Transient val namaProperti: String = "",
    @kotlinx.serialization.Transient val kategori: String = "",
    @kotlinx.serialization.Transient val lokasi: String = ""
)
