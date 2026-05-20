# GRIT — Platform Sewa Lahan Pertanian
## Instruksi untuk Claude Code

---

## Gambaran Proyek

GRIT adalah aplikasi Android untuk menyewa dan menyewakan lahan pertanian.
Pengguna bisa mendaftar, melihat daftar lahan, melihat detail lahan, menambahkan
lahan miliknya, menjual produk pertanian, dan mengelola akun mereka.

**Stack:**
- Bahasa: Kotlin
- UI: Jetpack Compose (deklaratif, bukan XML)
- Arsitektur: MVVM (Model-View-ViewModel)
- Backend: Supabase (Auth, PostgreSQL, Storage)
- Navigasi: Navigation Compose

---

## Struktur Folder

```
app/src/main/java/com/grit/app/
├── ui/
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   └── RegisterScreen.kt
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   ├── detail/
│   │   │   └── DetailScreen.kt
│   │   ├── form/
│   │   │   └── FormScreen.kt          ← dipakai untuk tambah & edit properti
│   │   └── profile/
│   │       ├── ProfileScreen.kt
│   │       └── MyFarmlandScreen.kt
│   └── components/                    ← komponen reusable (Card, ImagePicker, dll)
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── PropertyViewModel.kt
│   └── ProfileViewModel.kt
├── data/
│   ├── model/
│   │   ├── UserProfile.kt
│   │   ├── Property.kt
│   │   ├── PropertyImage.kt
│   │   └── Product.kt
│   └── repository/
│       ├── AuthRepository.kt
│       └── PropertyRepository.kt
├── navigation/
│   ├── AppNavigation.kt
│   └── NavRoutes.kt
└── utils/
    ├── ImageUtils.kt
    └── Constants.kt
```

---

## Skema Database (Supabase / PostgreSQL)

### Tabel `user_profiles`
Relasi 1:1 dengan `auth.users` (dikelola Supabase Auth).
```sql
CREATE TABLE user_profiles (
  id          UUID PRIMARY KEY,             -- sama dengan auth.users.id
  name        VARCHAR(100) NOT NULL,
  email       VARCHAR(150) NOT NULL UNIQUE,
  avatar_url  TEXT,
  created_at  TIMESTAMP DEFAULT NOW()
);
```

### Tabel `properties`
Lahan/properti yang disewakan. Relasi N:1 ke `user_profiles`.
```sql
CREATE TABLE properties (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
  nama_properti   VARCHAR(150) NOT NULL,
  provinsi        VARCHAR(100) NOT NULL,
  kabupaten_kota  VARCHAR(100) NOT NULL,
  harga           BIGINT NOT NULL CHECK (harga > 0),
  kategori        VARCHAR(50) NOT NULL
                    CHECK (kategori IN ('Pertanian','Perkebunan','Peternakan','Perikanan')),
  luas_tanah      INTEGER NOT NULL CHECK (luas_tanah > 0),
  deskripsi       TEXT,
  created_at      TIMESTAMP DEFAULT NOW(),
  updated_at      TIMESTAMP DEFAULT NOW()
);
```

### Tabel `property_images`
Foto properti, maks. 3 per properti. Relasi N:1 ke `properties`.
```sql
CREATE TABLE property_images (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  property_id  UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
  foto_url     TEXT NOT NULL,
  urutan       INTEGER NOT NULL DEFAULT 1 CHECK (urutan BETWEEN 1 AND 3),
  created_at   TIMESTAMP DEFAULT NOW(),
  UNIQUE (property_id, urutan)
);
```

### Tabel `products`
Produk hasil pertanian. Relasi N:1 ke `user_profiles`.
```sql
CREATE TABLE products (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
  nama_produk  VARCHAR(150) NOT NULL,
  harga        BIGINT NOT NULL CHECK (harga > 0),
  satuan       VARCHAR(20) DEFAULT 'kg',
  foto_url     TEXT,
  deskripsi    TEXT,
  created_at   TIMESTAMP DEFAULT NOW(),
  updated_at   TIMESTAMP DEFAULT NOW()
);
```

### Relasi ringkas
| Relasi | Jenis | Foreign Key |
|---|---|---|
| `auth.users` → `user_profiles` | 1:1 | `user_profiles.id` |
| `user_profiles` → `properties` | 1:N | `properties.user_id` |
| `user_profiles` → `products` | 1:N | `products.user_id` |
| `properties` → `property_images` | 1:N | `property_images.property_id` |

---

## Aturan Arsitektur — WAJIB DIIKUTI

1. **Screen (Composable) tidak boleh langsung memanggil Supabase.**
   Screen hanya boleh memanggil fungsi di ViewModel.

2. **ViewModel tidak boleh tahu soal Supabase secara langsung.**
   ViewModel memanggil fungsi di Repository, lalu meng-update state.

3. **Repository adalah satu-satunya tempat akses Supabase.**
   Semua query `supabase.from(...).select/insert/update/delete` ada di sini.

4. **State di ViewModel menggunakan `StateFlow` + `viewModelScope.launch`.**
   Jangan pakai `LiveData` — proyek ini full Compose.

5. **Navigasi menggunakan string route yang didefinisikan di `NavRoutes.kt`.**
   Jangan hardcode string route langsung di screen.

---

## Pola Kode yang Harus Diikuti

### Data class (model)
```kotlin
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
    val createdAt: String = ""
)
```

### Repository
```kotlin
class PropertyRepository(private val supabase: SupabaseClient) {
    suspend fun getAll(): List<Property> =
        supabase.from("properties").select().decodeList()

    suspend fun insert(property: Property) =
        supabase.from("properties").insert(property)

    suspend fun update(property: Property) =
        supabase.from("properties").update(property) {
            filter { eq("id", property.id) }
        }

    suspend fun delete(id: String) =
        supabase.from("properties").delete {
            filter { eq("id", id) }
        }
}
```

### ViewModel
```kotlin
class PropertyViewModel(private val repo: PropertyRepository) : ViewModel() {
    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    fun loadProperties() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                _properties.value = repo.getAll()
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }
}
```

### Screen (Composable)
```kotlin
@Composable
fun HomeScreen(viewModel: PropertyViewModel = viewModel()) {
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadProperties() }

    // render UI berdasarkan state
}
```

---

## Navigasi

Semua route didefinisikan di `NavRoutes.kt`:
```kotlin
object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val DETAIL = "detail/{propertyId}"
    const val FORM = "form?propertyId={propertyId}"   // optional param untuk edit
    const val PROFILE = "profile"
    const val MY_FARMLAND = "my_farmland"
}
```

---

## Halaman Aplikasi

| Screen | Route | Fungsi |
|---|---|---|
| `LoginScreen` | `login` | Login via Supabase Auth |
| `RegisterScreen` | `register` | Daftar akun baru |
| `HomeScreen` | `home` | Grid semua properti (LazyVerticalGrid) |
| `DetailScreen` | `detail/{propertyId}` | Detail properti lengkap |
| `FormScreen` | `form?propertyId=...` | Tambah properti (tanpa param) / Edit (dengan param) |
| `ProfileScreen` | `profile` | Info akun + navigasi ke lahan saya |
| `MyFarmlandScreen` | `my_farmland` | Daftar properti milik user, opsi edit/hapus |

---

## Supabase Storage

- Bucket untuk foto properti: `property-images`
- Bucket untuk avatar: `avatars`
- Format path upload: `{userId}/{propertyId}/{urutan}.jpg`
- Setelah upload, simpan URL publik ke kolom `foto_url` / `property_images.foto_url`

---

## Catatan Penting

- Kategori properti yang valid: `Pertanian`, `Perkebunan`, `Peternakan`, `Perikanan`
- Satuan produk default: `kg` (bisa diubah ke `ikat`, `buah`, dll)
- Satu properti maksimal 3 foto (urutan 1, 2, 3) — urutan 1 adalah foto utama
- `ON DELETE CASCADE` sudah diterapkan: hapus user → hapus semua properti & produknya; hapus properti → hapus semua fotonya
- Gunakan `coroutines` untuk semua operasi async, jangan blocking call di main thread