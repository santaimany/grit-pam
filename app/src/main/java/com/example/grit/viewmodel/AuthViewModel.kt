package com.example.grit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grit.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                repo.login(email, password)
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = when {
                    e.message?.contains("invalid_credentials", ignoreCase = true) == true -> "Email atau password salah"
                    e.message?.contains("Email not confirmed", ignoreCase = true) == true -> "Email belum dikonfirmasi"
                    e.message?.contains("network", ignoreCase = true) == true -> "Tidak ada koneksi internet"
                    else -> "Login gagal, coba lagi"
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                repo.register(email, password, name)
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = when {
                    e.message?.contains("already registered", ignoreCase = true) == true -> "Email sudah terdaftar"
                    e.message?.contains("weak_password", ignoreCase = true) == true -> "Password terlalu lemah (min. 6 karakter)"
                    e.message?.contains("rate_limit", ignoreCase = true) == true -> "Terlalu banyak percobaan, tunggu beberapa menit"
                    e.message?.contains("network", ignoreCase = true) == true -> "Tidak ada koneksi internet"
                    else -> "Registrasi gagal, coba lagi"
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.logout()
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }
}
