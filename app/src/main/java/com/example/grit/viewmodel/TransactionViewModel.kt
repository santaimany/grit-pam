package com.example.grit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grit.data.model.Transaction
import com.example.grit.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(private val repo: TransactionRepository = TransactionRepository()) : ViewModel() {

    private val _myTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val myTransactions: StateFlow<List<Transaction>> = _myTransactions.asStateFlow()

    private val _activePropertyIds = MutableStateFlow<Set<String>>(emptySet())
    val activePropertyIds: StateFlow<Set<String>> = _activePropertyIds.asStateFlow()

    private val _isPropertyRented = MutableStateFlow(false)
    val isPropertyRented: StateFlow<Boolean> = _isPropertyRented.asStateFlow()

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    val currentUserId: String? get() = repo.currentUserId()

    fun loadMyTransactions() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                _myTransactions.value = repo.getMyTransactions()
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadActivePropertyIds() {
        viewModelScope.launch {
            try {
                _activePropertyIds.value = repo.getActivePropertyIds()
            } catch (_: Exception) { }
        }
    }

    fun checkPropertyRented(propertyId: String) {
        viewModelScope.launch {
            try {
                _isPropertyRented.value = repo.isPropertyRented(propertyId)
            } catch (_: Exception) {
                _isPropertyRented.value = false
            }
        }
    }

    fun createTransaction(propertyId: String, harga: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                repo.create(propertyId, harga)
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Gagal membuat transaksi"
            } finally {
                isLoading.value = false
            }
        }
    }
}
