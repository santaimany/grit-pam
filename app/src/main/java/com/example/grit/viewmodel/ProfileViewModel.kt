package com.example.grit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grit.data.model.UserProfile
import com.example.grit.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val isLoading = MutableStateFlow(false)

    fun loadProfile() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                _userProfile.value = repo.currentProfile()
            } catch (e: Exception) {
                // silently ignore — UI falls back to placeholder
            } finally {
                isLoading.value = false
            }
        }
    }
}
