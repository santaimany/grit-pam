package com.example.grit.data.repository

import com.example.grit.data.model.Property
import com.example.grit.data.model.Transaction
import com.example.grit.utils.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate

class TransactionRepository {

    suspend fun create(propertyId: String, harga: Long) {
        val tenantId = supabase.auth.currentUserOrNull()?.id ?: error("Tidak terautentikasi")
        val today = LocalDate.now()
        supabase.from("transactions").insert(
            Transaction(
                propertyId = propertyId,
                harga = harga,
                tanggalMulai = today.toString(),
                tanggalSelesai = today.plusMonths(3).toString()
            ).copy(tenantId = tenantId)
        )
    }

    suspend fun getMyTransactions(): List<Transaction> {
        val tenantId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        val txs = supabase.from("transactions")
            .select {
                filter { eq("tenant_id", tenantId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Transaction>()
        return txs.map { tx ->
            val prop = runCatching {
                supabase.from("properties")
                    .select { filter { eq("id", tx.propertyId) } }
                    .decodeSingleOrNull<Property>()
            }.getOrNull()
            tx.copy(
                namaProperti = prop?.namaProperti ?: "",
                kategori = prop?.kategori ?: "",
                lokasi = "${prop?.kabupatenKota ?: ""}, ${prop?.provinsi ?: ""}"
            )
        }
    }

    suspend fun getActivePropertyIds(): Set<String> =
        supabase.from("transactions")
            .select { filter { eq("status", "active") } }
            .decodeList<Transaction>()
            .map { it.propertyId }
            .toSet()

    suspend fun isPropertyRented(propertyId: String): Boolean =
        supabase.from("transactions")
            .select {
                filter {
                    eq("property_id", propertyId)
                    eq("status", "active")
                }
            }
            .decodeList<Transaction>()
            .isNotEmpty()

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id
}
