package com.example.grit.ui.screens.form

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grit.data.model.Property
import com.example.grit.ui.theme.BorderGray
import com.example.grit.ui.theme.GritGreen
import com.example.grit.ui.theme.TextPrimary
import com.example.grit.ui.theme.TextSecondary
import com.example.grit.viewmodel.PropertyViewModel


private val kategoriOptions = listOf("Pertanian", "Perkebunan", "Peternakan", "Perikanan")

@Composable
fun FormScreen(
    propertyId: String? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: PropertyViewModel = viewModel()
) {
    val isEditing = !propertyId.isNullOrEmpty()
    val selectedProperty by viewModel.selectedProperty.collectAsStateWithLifecycle()
    val propertyImages by viewModel.propertyImages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var namaProperti by remember { mutableStateOf("") }
    var provinsi by remember { mutableStateOf("") }
    var kabupatenKota by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var luasTanah by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // imageUris[0..2] = new picks from gallery (null = belum pilih / gunakan existing)
    val imageUris = remember { mutableStateOf(arrayOfNulls<Uri>(3)) }
    // existingUrls[0..2] = URL yang sudah ada di DB (hanya dipakai saat edit)
    val existingUrls = remember { mutableStateOf(arrayOf("", "", "")) }

    var activeSlot by remember { mutableStateOf(0) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUris.value = imageUris.value.copyOf().also { arr -> arr[activeSlot] = it }
        }
    }

    // Load existing data if editing
    LaunchedEffect(propertyId) {
        if (isEditing) viewModel.loadProperty(propertyId!!)
    }
    LaunchedEffect(selectedProperty) {
        if (isEditing && selectedProperty != null) {
            selectedProperty!!.let { p ->
                namaProperti = p.namaProperti
                provinsi = p.provinsi
                kabupatenKota = p.kabupatenKota
                harga = if (p.harga > 0) p.harga.toString() else ""
                kategori = p.kategori
                luasTanah = if (p.luasTanah > 0) p.luasTanah.toString() else ""
                deskripsi = p.deskripsi
            }
        }
    }
    LaunchedEffect(propertyImages) {
        if (isEditing && propertyImages.isNotEmpty()) {
            val updated = arrayOf("", "", "")
            propertyImages.forEach { img ->
                val i = img.urutan - 1
                if (i in 0..2) updated[i] = img.fotoUrl
            }
            existingUrls.value = updated
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = TextPrimary)
            }
            Text(
                text = if (isEditing) "Edit Properti" else "Tambah Properti",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto properti
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Foto Properti", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text("Maks. 3 foto. Foto pertama tampil sebagai cover.", fontSize = 12.sp, color = TextSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ImageSlot(0, imageUris.value[0], existingUrls.value[0]) { activeSlot = 0; imagePicker.launch("image/*") }
                    ImageSlot(1, imageUris.value[1], existingUrls.value[1]) { activeSlot = 1; imagePicker.launch("image/*") }
                    ImageSlot(2, imageUris.value[2], existingUrls.value[2]) { activeSlot = 2; imagePicker.launch("image/*") }
                }
            }

            FormField(label = "Nama Properti", value = namaProperti, onValueChange = { namaProperti = it }, placeholder = "Contoh: Lahan Perkebunan Teh")
            FormField(label = "Provinsi", value = provinsi, onValueChange = { provinsi = it }, placeholder = "Contoh: Jawa Barat")
            FormField(label = "Kabupaten / Kota", value = kabupatenKota, onValueChange = { kabupatenKota = it }, placeholder = "Contoh: Bandung")
            FormField(
                label = "Harga per Musim (Rp)",
                value = harga,
                onValueChange = { if (it.all { c -> c.isDigit() }) harga = it },
                placeholder = "Contoh: 5000000",
                keyboardType = KeyboardType.Number
            )

            // Kategori dropdown
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Kategori", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (kategori.isEmpty()) "Pilih kategori" else kategori,
                            fontSize = 14.sp,
                            color = if (kategori.isEmpty()) TextSecondary.copy(alpha = 0.6f) else TextPrimary
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        kategoriOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    kategori = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            FormField(
                label = "Luas Tanah (m²)",
                value = luasTanah,
                onValueChange = { if (it.all { c -> c.isDigit() }) luasTanah = it },
                placeholder = "Contoh: 1000",
                keyboardType = KeyboardType.Number
            )
            FormField(
                label = "Deskripsi",
                value = deskripsi,
                onValueChange = { deskripsi = it },
                placeholder = "Deskripsikan kondisi lahan, fasilitas, dan informasi lainnya...",
                singleLine = false,
                minLines = 4
            )

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color(0xFFE53935), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val property = Property(
                        id = if (isEditing) selectedProperty?.id ?: "" else "",
                        userId = if (isEditing) selectedProperty?.userId ?: "" else "",
                        namaProperti = namaProperti.trim(),
                        provinsi = provinsi.trim(),
                        kabupatenKota = kabupatenKota.trim(),
                        harga = harga.toLongOrNull() ?: 0,
                        kategori = kategori,
                        luasTanah = luasTanah.toIntOrNull() ?: 0,
                        deskripsi = deskripsi.trim()
                    )
                    viewModel.saveProperty(property, imageUris.value.toList(), context, onSaved)
                },
                enabled = !isLoading && namaProperti.isNotBlank() && provinsi.isNotBlank()
                        && kabupatenKota.isNotBlank() && harga.isNotBlank()
                        && kategori.isNotBlank() && luasTanah.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GritGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isEditing) "Simpan Perubahan" else "Tambah Properti",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RowScope.ImageSlot(
    index: Int,
    uri: Uri?,
    existingUrl: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (uri != null || existingUrl.isNotEmpty()) GritGreen else BorderGray, RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            uri != null -> AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            existingUrl.isNotEmpty() -> AsyncImage(
                model = existingUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                Text("Foto ${index + 1}", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp) },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderGray,
                focusedBorderColor = GritGreen,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
