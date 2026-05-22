package com.example.grit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grit.data.model.Property
import com.example.grit.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PropertyViewModel(private val repo: PropertyRepository = PropertyRepository()) : ViewModel() {

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private val _myProperties = MutableStateFlow<List<Property>>(emptyList())
    val myProperties: StateFlow<List<Property>> = _myProperties.asStateFlow()

    private val _selectedProperty = MutableStateFlow<Property?>(null)
    val selectedProperty: StateFlow<Property?> = _selectedProperty.asStateFlow()

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    fun loadProperties() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val list = repo.getAll()
                val withImages = list.map { property ->
                    val img = runCatching { repo.getPrimaryImage(property.id) }.getOrNull()
                    property.copy(imageUrl = img?.fotoUrl ?: "")
                }
                _properties.value = withImages
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadMyProperties() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val userId = repo.currentUserId() ?: return@launch
                val list = repo.getByUserId(userId)
                val withImages = list.map { property ->
                    val img = runCatching { repo.getPrimaryImage(property.id) }.getOrNull()
                    property.copy(imageUrl = img?.fotoUrl ?: "")
                }
                _myProperties.value = withImages
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadProperty(id: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val property = repo.getById(id)
                val img = runCatching { repo.getPrimaryImage(id) }.getOrNull()
                _selectedProperty.value = property.copy(imageUrl = img?.fotoUrl ?: "")
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun deleteProperty(id: String) {
        viewModelScope.launch {
            try {
                repo.delete(id)
                _myProperties.value = _myProperties.value.filter { it.id != id }
            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }
}
