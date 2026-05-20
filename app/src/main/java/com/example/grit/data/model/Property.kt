package com.example.grit.data.model

data class Property(
    val id: String = "",
    val userId: String = "",
    val namaProperti: String = "",
    val provinsi: String = "",
    val kabupatenKota: String = "",
    val harga: Long = 0,
    val kategori: String = "",
    val luasTanah: Int = 0,
    val deskripsi: String = "",
    val createdAt: String = "",
    val imageUrl: String = "",
    val rating: Float = 0f
)
