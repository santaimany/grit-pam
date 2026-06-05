# Supabase Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the entire Grit app to a real Supabase backend — auth, property CRUD, user profiles, and session persistence.

**Architecture:** Repository layer talks to Supabase; ViewModels hold StateFlow state and call repositories; Screens observe state via `collectAsStateWithLifecycle`. No Hilt — ViewModels use the lambda factory (`viewModel { MyViewModel(MyRepository()) }`) and the `supabase` singleton from `utils/SupabaseClient.kt`.

**Tech Stack:** Supabase-kt 3.1.4 (postgrest-kt, auth-kt, storage-kt), Ktor 3.1.0 (Android engine), kotlinx-serialization 1.7.3, Kotlin 2.0.21, Jetpack Compose, Navigation Compose.

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `gradle/libs.versions.toml` | Add supabase, ktor, serialization versions + entries |
| Modify | `build.gradle.kts` (root) | Declare serialization plugin |
| Modify | `app/build.gradle.kts` | Apply serialization plugin, add deps, enable buildConfig, read local.properties |
| Modify | `app/src/main/AndroidManifest.xml` | Add INTERNET permission |
| Manual | `local.properties` | User adds SUPABASE_URL + SUPABASE_KEY |
| Create | `app/src/main/java/com/example/grit/utils/SupabaseClient.kt` | Supabase singleton |
| Modify | `app/src/main/java/com/example/grit/data/model/Property.kt` | Add @Serializable + @SerialName |
| Create | `app/src/main/java/com/example/grit/data/model/UserProfile.kt` | User profile DB model |
| Create | `app/src/main/java/com/example/grit/data/model/PropertyImage.kt` | Property image DB model |
| Create | `app/src/main/java/com/example/grit/data/repository/AuthRepository.kt` | Auth operations |
| Create | `app/src/main/java/com/example/grit/data/repository/PropertyRepository.kt` | Property CRUD |
| Create | `app/src/main/java/com/example/grit/viewmodel/AuthViewModel.kt` | Login/register state |
| Create | `app/src/main/java/com/example/grit/viewmodel/PropertyViewModel.kt` | Property list/detail/delete |
| Create | `app/src/main/java/com/example/grit/viewmodel/ProfileViewModel.kt` | User profile state |
| Modify | `app/src/main/java/com/example/grit/ui/screens/auth/LoginScreen.kt` | Wire AuthViewModel |
| Modify | `app/src/main/java/com/example/grit/ui/screens/auth/RegisterScreen.kt` | Wire AuthViewModel |
| Modify | `app/src/main/java/com/example/grit/ui/screens/home/HomeScreen.kt` | Wire PropertyViewModel |
| Modify | `app/src/main/java/com/example/grit/ui/screens/profile/MyFarmlandScreen.kt` | Wire PropertyViewModel |
| Modify | `app/src/main/java/com/example/grit/ui/screens/profile/ProfileScreen.kt` | Wire ProfileViewModel |
| Modify | `app/src/main/java/com/example/grit/navigation/AppNavigation.kt` | Session check + pass ViewModels |

---

## Task 1: Build Setup — Dependencies & Plugins

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts` (root)
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add versions and libraries to `gradle/libs.versions.toml`**

In the `[versions]` section, add after `coil = "2.7.0"`:
```toml
supabase = "3.1.4"
ktor = "3.1.0"
kotlinSerialization = "1.7.3"
```

In the `[libraries]` section, add after `coil-compose`:
```toml
supabase-bom          = { group = "io.github.jan-tengert", name = "supabase-bom",      version.ref = "supabase" }
supabase-postgrest    = { group = "io.github.jan-tengert", name = "postgrest-kt" }
supabase-auth         = { group = "io.github.jan-tengert", name = "auth-kt" }
supabase-storage      = { group = "io.github.jan-tengert", name = "storage-kt" }
ktor-client-android   = { group = "io.ktor",               name = "ktor-client-android", version.ref = "ktor" }
kotlinx-serialization = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinSerialization" }
```

In the `[plugins]` section, add:
```toml
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 2: Declare serialization plugin in root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

- [ ] **Step 3: Apply plugin + add dependencies in `app/build.gradle.kts`**

Add to `plugins` block:
```kotlin
alias(libs.plugins.kotlin.serialization)
```

Add `buildConfig = true` to the existing `buildFeatures` block:
```kotlin
buildFeatures {
    compose = true
    buildConfig = true
}
```

Add after `buildFeatures`, still inside `android { }`:
```kotlin
defaultConfig {
    // ... existing fields ...
    val localProps = java.util.Properties()
    localProps.load(rootProject.file("local.properties").inputStream())
    buildConfigField("String", "SUPABASE_URL", "\"${localProps["SUPABASE_URL"]}\"")
    buildConfigField("String", "SUPABASE_KEY", "\"${localProps["SUPABASE_KEY"]}\"")
}
```

Add to `dependencies { }` block:
```kotlin
implementation(platform(libs.supabase.bom))
implementation(libs.supabase.postgrest)
implementation(libs.supabase.auth)
implementation(libs.supabase.storage)
implementation(libs.ktor.client.android)
implementation(libs.kotlinx.serialization)
```

- [ ] **Step 4: Sync Gradle**

In Android Studio: **File → Sync Project with Gradle Files**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts
git commit -m "build: add supabase, ktor, and serialization dependencies"
```

---

## Task 2: INTERNET Permission + Secrets Setup

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Manual: `local.properties`

- [ ] **Step 1: Add INTERNET permission to `AndroidManifest.xml`**

Add above `<application`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

- [ ] **Step 2: Add Supabase credentials to `local.properties`**

Open `local.properties` (root of project, already in `.gitignore`) and add:
```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_KEY=your-anon-public-key-here
```

Get these from Supabase Dashboard → Project Settings → API.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "feat: add INTERNET permission for Supabase network calls"
```

(`local.properties` is git-ignored — do NOT add it.)

---

## Task 3: Supabase Singleton

**Files:**
- Create: `app/src/main/java/com/example/grit/utils/SupabaseClient.kt`

- [ ] **Step 1: Create `SupabaseClient.kt`**

```kotlin
package com.example.grit.utils

import com.example.grit.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}
```

- [ ] **Step 2: Build the project to verify no errors**

In Android Studio: **Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/grit/utils/SupabaseClient.kt
git commit -m "feat: add Supabase client singleton"
```

---

## Task 4: Data Models

**Files:**
- Modify: `app/src/main/java/com/example/grit/data/model/Property.kt`
- Create: `app/src/main/java/com/example/grit/data/model/UserProfile.kt`
- Create: `app/src/main/java/com/example/grit/data/model/PropertyImage.kt`

- [ ] **Step 1: Update `Property.kt` — add @Serializable and @SerialName**

The `imageUrl` and `rating` fields don't exist in the DB. Mark them `@kotlinx.serialization.Transient` so they're ignored during deserialization but still usable in UI.

```kotlin
package com.example.grit.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val id: String = "",
    @SerialName("user_id")        val userId: String = "",
    @SerialName("nama_properti")  val namaProperti: String = "",
    val provinsi: String = "",
    @SerialName("kabupaten_kota") val kabupatenKota: String = "",
    val harga: Long = 0,
    val kategori: String = "",
    @SerialName("luas_tanah")     val luasTanah: Int = 0,
    val deskripsi: String = "",
    @SerialName("created_at")     val createdAt: String = "",
    @kotlinx.serialization.Transient val imageUrl: String = "",
    @kotlinx.serialization.Transient val rating: Float = 0f
)
```

- [ ] **Step 2: Create `UserProfile.kt`**

```kotlin
package com.example.grit.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    @SerialName("avatar_url")  val avatarUrl: String? = null,
    @SerialName("created_at")  val createdAt: String = ""
)
```

- [ ] **Step 3: Create `PropertyImage.kt`**

```kotlin
package com.example.grit.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PropertyImage(
    val id: String = "",
    @SerialName("property_id") val propertyId: String = "",
    @SerialName("foto_url")    val fotoUrl: String = "",
    val urutan: Int = 1,
    @SerialName("created_at")  val createdAt: String = ""
)
```

- [ ] **Step 4: Build to verify no errors**

**Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/grit/data/model/
git commit -m "feat: annotate data models with @Serializable for Supabase"
```

---

## Task 5: AuthRepository

**Files:**
- Create: `app/src/main/java/com/example/grit/data/repository/AuthRepository.kt`

- [ ] **Step 1: Create `AuthRepository.kt`**

```kotlin
package com.example.grit.data.repository

import com.example.grit.data.model.UserProfile
import com.example.grit.utils.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

class AuthRepository {

    suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun register(email: String, password: String, name: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // Insert profile row after signup
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        supabase.from("user_profiles").insert(
            UserProfile(id = userId, name = name, email = email)
        )
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
```

- [ ] **Step 2: Build to verify no errors**

**Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/grit/data/repository/AuthRepository.kt
git commit -m "feat: add AuthRepository for Supabase auth operations"
```

---

## Task 6: PropertyRepository

**Files:**
- Create: `app/src/main/java/com/example/grit/data/repository/PropertyRepository.kt`

- [ ] **Step 1: Create `PropertyRepository.kt`**

```kotlin
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
```

- [ ] **Step 2: Build to verify no errors**

**Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/grit/data/repository/PropertyRepository.kt
git commit -m "feat: add PropertyRepository for Supabase property CRUD"
```

---

## Task 7: AuthViewModel

**Files:**
- Create: `app/src/main/java/com/example/grit/viewmodel/AuthViewModel.kt`

- [ ] **Step 1: Create `AuthViewModel.kt`**

```kotlin
package com.example.grit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grit.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                errorMessage.value = e.message ?: "Login gagal"
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
                errorMessage.value = e.message ?: "Registrasi gagal"
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/grit/viewmodel/AuthViewModel.kt
git commit -m "feat: add AuthViewModel"
```

---

## Task 8: PropertyViewModel

**Files:**
- Create: `app/src/main/java/com/example/grit/viewmodel/PropertyViewModel.kt`

- [ ] **Step 1: Create `PropertyViewModel.kt`**

```kotlin
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
                // Fetch primary image for each property
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/grit/viewmodel/PropertyViewModel.kt
git commit -m "feat: add PropertyViewModel"
```

---

## Task 9: ProfileViewModel

**Files:**
- Create: `app/src/main/java/com/example/grit/viewmodel/ProfileViewModel.kt`

- [ ] **Step 1: Create `ProfileViewModel.kt`**

```kotlin
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/grit/viewmodel/ProfileViewModel.kt
git commit -m "feat: add ProfileViewModel"
```

---

## Task 10: Wire LoginScreen & RegisterScreen

**Files:**
- Modify: `app/src/main/java/com/example/grit/ui/screens/auth/LoginScreen.kt`
- Modify: `app/src/main/java/com/example/grit/ui/screens/auth/RegisterScreen.kt`

- [ ] **Step 1: Update `LoginScreen.kt`**

Add ViewModel and loading/error state. Replace the `onClick = onLoginSuccess` button with a real call:

```kotlin
package com.example.grit.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grit.ui.components.GritLogo
import com.example.grit.ui.theme.*
import com.example.grit.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GritLogo()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Silahkan masuk menggunakan Email dan\nPassword Anda.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Masukkan Email", color = TextSecondary.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderGray, focusedBorderColor = GritGreen),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Masukkan Password", color = TextSecondary.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderGray, focusedBorderColor = GritGreen),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = androidx.compose.ui.graphics.Color(0xFFE53935), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = { viewModel.login(email, password, onLoginSuccess) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GritGreen)
        ) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = buildAnnotatedString {
                append("Belum memiliki akun? ")
                withStyle(SpanStyle(color = GritGreen, fontWeight = FontWeight.SemiBold)) { append("Buat Akun") }
            },
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.clickable { onNavigateToRegister() }
        )
    }
}
```

- [ ] **Step 2: Update `RegisterScreen.kt`**

```kotlin
package com.example.grit.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grit.ui.components.GritLogo
import com.example.grit.ui.theme.*
import com.example.grit.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        GritLogo()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Silahkan daftar menggunakan Email dan\nPassword Anda.",
            fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text("Nama", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = nama, onValueChange = { nama = it },
            placeholder = { Text("Masukkan Nama", color = TextSecondary.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderGray, focusedBorderColor = GritGreen),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            placeholder = { Text("Masukkan Email", color = TextSecondary.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderGray, focusedBorderColor = GritGreen),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            placeholder = { Text("Masukkan Password", color = TextSecondary.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = BorderGray, focusedBorderColor = GritGreen),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = androidx.compose.ui.graphics.Color(0xFFE53935), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = { viewModel.register(email, password, nama, onRegisterSuccess) },
            enabled = !isLoading && nama.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GritGreen)
        ) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Buat akun", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = buildAnnotatedString {
                append("Sudah memiliki akun? ")
                withStyle(SpanStyle(color = GritGreen, fontWeight = FontWeight.SemiBold)) { append("Masuk") }
            },
            fontSize = 14.sp, color = TextSecondary,
            modifier = Modifier.clickable { onNavigateToLogin() }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}
```

- [ ] **Step 3: Build and verify**

**Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/grit/ui/screens/auth/
git commit -m "feat: wire AuthViewModel into LoginScreen and RegisterScreen"
```

---

## Task 11: Wire HomeScreen

**Files:**
- Modify: `app/src/main/java/com/example/grit/ui/screens/home/HomeScreen.kt`

- [ ] **Step 1: Update `HomeScreen.kt`**

Remove `dummyProperties`. Accept `PropertyViewModel` and load real data:

```kotlin
package com.example.grit.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grit.ui.components.*
import com.example.grit.viewmodel.PropertyViewModel

@Composable
fun HomeScreen(
    onPropertyClick: (String) -> Unit,
    onAddProperty: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: PropertyViewModel = viewModel()
) {
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadProperties() }

    Scaffold(
        topBar = { GritTopBar() },
        bottomBar = {
            GritBottomBar(
                selectedTab = BottomNavTab.HOME,
                onHomeClick = {},
                onAddClick = onAddProperty,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (isLoading && properties.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = com.example.grit.ui.theme.GritGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(properties) { property ->
                    PropertyCard(property = property, onClick = { onPropertyClick(property.id) })
                }
            }
        }
    }
}
```

- [ ] **Step 2: Build and verify**

**Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/grit/ui/screens/home/HomeScreen.kt
git commit -m "feat: wire PropertyViewModel into HomeScreen"
```

---

## Task 12: Wire MyFarmlandScreen & ProfileScreen

**Files:**
- Modify: `app/src/main/java/com/example/grit/ui/screens/profile/MyFarmlandScreen.kt`
- Modify: `app/src/main/java/com/example/grit/ui/screens/profile/ProfileScreen.kt`

- [ ] **Step 1: Update `MyFarmlandScreen.kt`**

Remove `dummyMyProperties`. Accept `PropertyViewModel`:

Replace the top of the file (imports + composable signature + state):
```kotlin
// add these imports
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grit.viewmodel.PropertyViewModel
```

Change the composable signature and body:
```kotlin
@Composable
fun MyFarmlandScreen(
    onBack: () -> Unit,
    onEditProperty: (String) -> Unit,
    onDeleteProperty: (String) -> Unit = {},
    viewModel: PropertyViewModel = viewModel()
) {
    val myProperties by viewModel.myProperties.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var propertyToDelete by remember { mutableStateOf<Property?>(null) }

    LaunchedEffect(Unit) { viewModel.loadMyProperties() }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7)).statusBarsPadding()
    ) {
        // top bar — unchanged
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, "Kembali", tint = TextPrimary, modifier = Modifier.size(18.dp))
            }
            Text("My Farmland", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        if (isLoading && myProperties.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GritGreen)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myProperties) { property ->
                    MyFarmlandCard(
                        property = property,
                        onEdit = { onEditProperty(property.id) },
                        onDelete = { propertyToDelete = property }
                    )
                }
            }
        }
    }

    propertyToDelete?.let { property ->
        AlertDialog(
            onDismissRequest = { propertyToDelete = null },
            title = { Text("Hapus Properti", fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus \"${property.namaProperti}\"? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProperty(property.id)
                        propertyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Hapus", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { propertyToDelete = null }) { Text("Batal", color = TextPrimary) }
            },
            containerColor = Color.White
        )
    }
}
```

- [ ] **Step 2: Update `ProfileScreen.kt`**

Accept `ProfileViewModel`, show real user name/email:

Add import and parameter:
```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grit.viewmodel.ProfileViewModel
import com.example.grit.viewmodel.AuthViewModel
```

Change composable signature:
```kotlin
@Composable
fun ProfileScreen(
    onMyFarmlandClick: () -> Unit,
    onTransaksiClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val userProfile by profileViewModel.userProfile.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { profileViewModel.loadProfile() }
```

Replace the hardcoded name/email in the profile card with:
```kotlin
Text(
    text = userProfile?.name ?: "—",
    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary
)
Text(
    text = userProfile?.email ?: "—",
    fontSize = 13.sp, color = TextSecondary
)
```

Update the logout `SettingsMenuItem` onClick:
```kotlin
SettingsMenuItem(
    icon = Icons.AutoMirrored.Filled.ExitToApp,
    label = "Keluar Aplikasi",
    iconTint = Color(0xFFE53935),
    labelColor = Color(0xFFE53935),
    showChevron = false,
    onClick = { authViewModel.logout(onLogoutClick) }
)
```

- [ ] **Step 3: Build and verify**

**Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/grit/ui/screens/profile/
git commit -m "feat: wire PropertyViewModel and ProfileViewModel into profile screens"
```

---

## Task 13: Session Persistence in AppNavigation

**Files:**
- Modify: `app/src/main/java/com/example/grit/navigation/AppNavigation.kt`

- [ ] **Step 1: Add session check on app start**

Add these imports:
```kotlin
import androidx.compose.runtime.LaunchedEffect
import com.example.grit.utils.supabase
import io.github.jan.supabase.auth.auth
```

Inside `AppNavigation()`, before `NavHost`, add:
```kotlin
LaunchedEffect(Unit) {
    supabase.auth.awaitInitialization()
    if (supabase.auth.currentSessionOrNull() != null) {
        navController.navigate(NavRoutes.HOME) {
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
        }
    }
}
```

- [ ] **Step 2: Build and verify**

**Build → Make Project**. Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Final integration test on device/emulator**

Run the app. Verify:
1. First launch → lands on LoginScreen
2. Register a new account → navigates to HomeScreen
3. Kill and reopen app → stays on HomeScreen (session persisted)
4. Tap Keluar Aplikasi → goes back to LoginScreen
5. Reopen app → back to LoginScreen (session cleared)
6. Login → HomeScreen shows properties from Supabase

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/grit/navigation/AppNavigation.kt
git commit -m "feat: add session persistence check on app start"
```
