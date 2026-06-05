package com.example.grit.data.repository

import com.example.grit.data.model.UserProfile
import com.example.grit.utils.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class AuthRepository {

    suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun register(email: String, password: String, name: String) {
        // name dikirim via metadata — trigger on auth.users otomatis insert ke user_profiles
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject { put("name", name) }
        }
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    suspend fun currentProfile(): UserProfile? {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return null
        return supabase.from("user_profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull()
    }

    fun isLoggedIn(): Boolean = supabase.auth.currentSessionOrNull() != null
}
