package com.example.grit.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Property(
    // Tidak dikirim saat insert (DB generate UUID); dikirim saat update (non-empty)
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val id: String = "",
    // Tidak dikirim saat update (FormScreen tidak set userId); dikirim saat insert via .copy(userId=...)
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("user_id")        val userId: String = "",
    @SerialName("nama_properti")  val namaProperti: String = "",
    val provinsi: String = "",
    @SerialName("kabupaten_kota") val kabupatenKota: String = "",
    val harga: Long = 0,
    val kategori: String = "",
    @SerialName("luas_tanah")     val luasTanah: Int = 0,
    val deskripsi: String = "",
    // Tidak pernah dikirim dari app (selalu ""); DB pakai DEFAULT NOW()
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("created_at")     val createdAt: String = "",
    @kotlinx.serialization.Transient val imageUrl: String = "",
    @kotlinx.serialization.Transient val rating: Float = 0f
)
