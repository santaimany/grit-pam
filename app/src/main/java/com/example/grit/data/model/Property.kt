package com.example.grit.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val id: String = "",
    @SerialName("user_id")        val userId: String = "",
    @SerialName("nama_properti")  val namaProperti: String = "",
    val provinsi: String = "",
    @SerialName("kabupaten_kota") val kabupatenKota: String = "",
    val harga: Long = 0,
    val kategori: String = "",
    @SerialName("luas_tanah")     val luasTanah: Int = 0,
    val deskripsi: String = "",
    @SerialName("created_at")     val createdAt: String = "",
    @kotlinx.serialization.Transient val imageUrl: String = "",
    @kotlinx.serialization.Transient val rating: Float = 0f
)
