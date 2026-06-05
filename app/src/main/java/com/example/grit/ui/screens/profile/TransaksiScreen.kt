package com.example.grit.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grit.data.model.Transaction
import com.example.grit.ui.components.BottomNavTab
import com.example.grit.ui.components.GritBottomBar
import com.example.grit.ui.theme.BorderGray
import com.example.grit.ui.theme.GritGreen
import com.example.grit.ui.theme.TextPrimary
import com.example.grit.ui.theme.TextSecondary
import com.example.grit.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))

private fun formatTanggal(dateStr: String): String = try {
    LocalDate.parse(dateStr).format(dateFormatter)
} catch (_: Exception) { dateStr }

private fun formatHarga(harga: Long): String =
    NumberFormat.getNumberInstance(Locale("id", "ID")).format(harga)

@Composable
fun TransaksiScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {
    val transactions by viewModel.myTransactions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Semua") }
    val tabs = listOf("Semua", "Aktif", "Selesai")

    LaunchedEffect(Unit) { viewModel.loadMyTransactions() }

    val filtered = when (selectedTab) {
        "Aktif" -> transactions.filter { it.status == "active" }
        "Selesai" -> transactions.filter { it.status == "completed" }
        else -> transactions
    }

    Scaffold(
        bottomBar = {
            GritBottomBar(
                selectedTab = BottomNavTab.PROFILE,
                onHomeClick = onHomeClick,
                onAddClick = onAddClick,
                onProfileClick = {}
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text(
                    text = "Transaksi Saya",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Status tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    if (index > 0) {
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(BorderGray))
                    }
                    TransaksiTabItem(
                        label = tab,
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = GritGreen) }

                filtered.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada transaksi",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { tx -> TransaksiCard(tx) }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TransaksiCard(tx: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tx.namaProperti.ifEmpty { "Properti" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            val isActive = tx.status == "active"
            Text(
                text = if (isActive) "Aktif" else "Selesai",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) GritGreen else TextSecondary,
                modifier = Modifier
                    .background(
                        if (isActive) GritGreen.copy(alpha = 0.12f) else BorderGray,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tx.kategori.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Apps, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text(tx.kategori, fontSize = 12.sp, color = TextSecondary)
                }
            }
            if (tx.lokasi.isNotEmpty() && tx.lokasi != ", ") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text(tx.lokasi, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderGray))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Periode Sewa", fontSize = 11.sp, color = TextSecondary)
                Text(
                    "${formatTanggal(tx.tanggalMulai)} – ${formatTanggal(tx.tanggalSelesai)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Total Harga", fontSize = 11.sp, color = TextSecondary)
                Text(
                    "Rp${formatHarga(tx.harga)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GritGreen
                )
            }
        }
    }
}

@Composable
private fun TransaksiTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() }.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) GritGreen else TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(if (selected) 40.dp else 0.dp)
                .background(if (selected) GritGreen else Color.Transparent)
        )
    }
}
