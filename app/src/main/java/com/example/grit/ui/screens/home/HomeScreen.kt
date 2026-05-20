package com.example.grit.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.grit.data.model.Property
import com.example.grit.ui.components.BottomNavTab
import com.example.grit.ui.components.GritBottomBar
import com.example.grit.ui.components.GritTopBar
import com.example.grit.ui.components.PropertyCard

private val dummyProperties = listOf(
    Property(
        id = "1",
        namaProperti = "Lahan Perkebunan Teh",
        provinsi = "Banten",
        kabupatenKota = "Tangerang Selatan",
        harga = 70_000_000,
        kategori = "Perkebunan",
        luasTanah = 1100,
        imageUrl = "",
        rating = 4.9f
    ),
    Property(
        id = "2",
        namaProperti = "Peternakan Unggas",
        provinsi = "Jawa Timur",
        kabupatenKota = "Surabaya",
        harga = 80_000_000,
        kategori = "Peternakan",
        luasTanah = 700,
        imageUrl = "",
        rating = 4.9f
    ),
    Property(
        id = "3",
        namaProperti = "Lahan Pertanian",
        provinsi = "Jawa Barat",
        kabupatenKota = "Bandung",
        harga = 150_000_000,
        kategori = "Pertanian",
        luasTanah = 2000,
        imageUrl = "",
        rating = 4.9f
    ),
)

@Composable
fun HomeScreen(
    onPropertyClick: (String) -> Unit,
    onAddProperty: () -> Unit,
    onProfileClick: () -> Unit
) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyProperties) { property ->
                PropertyCard(
                    property = property,
                    onClick = { onPropertyClick(property.id) }
                )
            }
        }
    }
}
