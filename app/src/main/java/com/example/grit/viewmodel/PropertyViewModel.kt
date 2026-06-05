package com.example.grit.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grit.data.model.Property
import com.example.grit.data.model.PropertyImage
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

    private val _propertyImages = MutableStateFlow<List<PropertyImage>>(emptyList())
    val propertyImages: StateFlow<List<PropertyImage>> = _propertyImages.asStateFlow()

    private val _ownerName = MutableStateFlow<String?>(null)
    val ownerName: StateFlow<String?> = _ownerName.asStateFlow()

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
                val images = runCatching { repo.getImages(id) }.getOrDefault(emptyList())
                val primaryUrl = images.firstOrNull { it.urutan == 1 }?.fotoUrl ?: ""
                _selectedProperty.value = property.copy(imageUrl = primaryUrl)
                _propertyImages.value = images.sortedBy { it.urutan }
                _ownerName.value = runCatching { repo.getOwnerName(property.userId) }.getOrNull()
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun saveProperty(
        property: Property,
        imageUris: List<Uri?>,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val propertyId = if (property.id.isEmpty()) {
                    repo.insert(property)
                } else {
                    repo.update(property)
                    property.id
                }
                val userId = repo.currentUserId() ?: error("Tidak terautentikasi")
                imageUris.forEachIndexed { index, uri ->
                    if (uri != null) {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: return@forEachIndexed
                        val url = repo.uploadImage(userId, propertyId, index + 1, bytes)
                        repo.upsertImage(propertyId, url, index + 1)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Gagal menyimpan properti"
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
