package com.example.grit.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grit.ui.theme.GritGreen
import com.example.grit.ui.theme.TextPrimary
import com.example.grit.ui.theme.TextSecondary
import com.example.grit.viewmodel.PropertyViewModel
import com.example.grit.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun DetailScreen(
    propertyId: String,
    onBack: () -> Unit,
    onSewa: () -> Unit = {},
    viewModel: PropertyViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val property by viewModel.selectedProperty.collectAsStateWithLifecycle()
    val images by viewModel.propertyImages.collectAsStateWithLifecycle()
    val ownerName by viewModel.ownerName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPropertyRented by transactionViewModel.isPropertyRented.collectAsStateWithLifecycle()
    val txLoading by transactionViewModel.isLoading.collectAsStateWithLifecycle()
    val txError by transactionViewModel.errorMessage.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    var showSewaDialog by remember { mutableStateOf(false) }

    LaunchedEffect(propertyId) {
        viewModel.loadProperty(propertyId)
        transactionViewModel.checkPropertyRented(propertyId)
    }

    val isOwner = property != null && property!!.userId == transactionViewModel.currentUserId

    if (showSewaDialog && property != null) {
        val p = property!!
        val tanggalMulai = LocalDate.now()
        val tanggalSelesai = tanggalMulai.plusMonths(3)
        AlertDialog(
            onDismissRequest = { showSewaDialog = false },
            title = { Text("Konfirmasi Sewa", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Sewa ${p.namaProperti} selama 1 musim (3 bulan)\n\n" +
                    "Mulai: $tanggalMulai\nSelesai: $tanggalSelesai\n\n" +
                    "Harga: Rp${formatHarga(p.harga)}/musim"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSewaDialog = false
                        transactionViewModel.createTransaction(p.id, p.harga) { onBack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GritGreen)
                ) { Text("Sewa Sekarang") }
            },
            dismissButton = {
                TextButton(onClick = { showSewaDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            when {
                isOwner -> {
                    // Pemilik tidak bisa sewa lahannya sendiri
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ini properti Anda", fontSize = 14.sp, color = TextSecondary)
                    }
                }
                isPropertyRented -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE0E0E0))
                        ) {
                            Text("Sedang Disewakan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        }
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (txError != null) {
                            Text(txError!!, color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = { showSewaDialog = true },
                            enabled = !txLoading,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GritGreen)
                        ) {
                            if (txLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Sewa Sekarang", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                        Box(
                            modifier = Modifier.size(52.dp).border(1.dp, GritGreen, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MoreHoriz, "Lainnya", tint = GritGreen)
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (isLoading && property == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GritGreen)
            }
            return@Scaffold
        }

        val p = property ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Cover image
            Box(modifier = Modifier.fillMaxWidth()) {
                val coverUrl = images.firstOrNull { it.urutan == 1 }?.fotoUrl ?: p.imageUrl
                AsyncImage(
                    model = coverUrl.ifEmpty { null },
                    contentDescription = p.namaProperti,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color(0xFFF0F0F0))
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Kembali",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .offset(y = (-20).dp)
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Nama + harga
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = p.namaProperti,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Normal)) { append("Rp") }
                            withStyle(SpanStyle(color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)) { append(formatHarga(p.harga)) }
                            withStyle(SpanStyle(color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Normal)) { append("/musim") }
                        }
                    )
                }

                // Lokasi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Text("${p.kabupatenKota}, ${p.provinsi}", fontSize = 13.sp, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats: kategori + luas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.GridView, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Text(p.kategori, fontSize = 13.sp, color = TextSecondary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE8E8E8)))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CropFree, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                        Text("${p.luasTanah} m²", fontSize = 13.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pemilik
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(Color(0xFF90A4AE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        text = ownerName?.takeIf { it.isNotEmpty() } ?: "Memuat...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Deskripsi
                Text("Tentang Properti", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                val desc = p.deskripsi
                val needsTruncate = desc.length > 120
                if (expanded || !needsTruncate) {
                    Text(desc, fontSize = 13.sp, color = TextSecondary, lineHeight = 22.sp)
                } else {
                    Text(
                        text = buildAnnotatedString {
                            append("${desc.take(120)}... ")
                            withStyle(SpanStyle(color = GritGreen, fontWeight = FontWeight.Medium)) { append("See More...") }
                        },
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp,
                        modifier = Modifier.clickable { expanded = true }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Galeri
                if (images.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Galeri Properti", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Semua", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = GritGreen)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        val slots = images.take(3)
                        slots.forEach { img -> GallerySlot(url = img.fotoUrl) }
                        repeat(3 - slots.size) { GallerySlot(url = null) }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.GallerySlot(url: String?) {
    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
        )
    } else {
        Box(modifier = Modifier.weight(1f).height(80.dp))
    }
}


private fun formatHarga(harga: Long): String =
    NumberFormat.getNumberInstance(Locale("id", "ID")).format(harga)
