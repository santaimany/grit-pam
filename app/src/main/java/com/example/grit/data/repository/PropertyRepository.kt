package com.example.grit.data.repository

import com.example.grit.data.model.Property
import com.example.grit.data.model.PropertyImage
import com.example.grit.utils.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class PropertyRepository {

    suspend fun getAll(): List<Property> =
        supabase.from("properties")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList()

    suspend fun getByUserId(userId: String): List<Property> =
        supabase.from("properties")
            .select { filter { eq("user_id", userId) } }
            .decodeList()

    suspend fun getById(id: String): Property =
        supabase.from("properties")
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun insert(property: Property) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        supabase.from("properties").insert(property.copy(userId = userId))
    }

    suspend fun update(property: Property) {
        supabase.from("properties").update(property) {
            filter { eq("id", property.id) }
        }
    }

    suspend fun delete(id: String) {
        supabase.from("properties").delete {
            filter { eq("id", id) }
        }
    }

    suspend fun getPrimaryImage(propertyId: String): PropertyImage? =
        supabase.from("property_images")
            .select {
                filter {
                    eq("property_id", propertyId)
                    eq("urutan", 1)
                }
            }
            .decodeSingleOrNull()

    suspend fun getImages(propertyId: String): List<PropertyImage> =
        supabase.from("property_images")
            .select { filter { eq("property_id", propertyId) } }
            .decodeList()

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id
}
