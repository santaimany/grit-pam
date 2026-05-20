package com.example.grit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grit.ui.theme.GritGreen
import com.example.grit.ui.theme.TextSecondary

enum class BottomNavTab { HOME, PROFILE }

@Composable
fun GritBottomBar(
    selectedTab: BottomNavTab,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 8.dp
        ) {
            NavigationBarItem(
                selected = selectedTab == BottomNavTab.HOME,
                onClick = onHomeClick,
                icon = { Icon(Icons.Default.Home, contentDescription = "Utama") },
                label = { Text("Utama", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GritGreen,
                    selectedTextColor = GritGreen,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
            // Slot kosong untuk FAB
            NavigationBarItem(
                selected = false,
                onClick = {},
                enabled = false,
                icon = {},
                label = {}
            )
            NavigationBarItem(
                selected = selectedTab == BottomNavTab.PROFILE,
                onClick = onProfileClick,
                icon = { Icon(Icons.Default.Person, contentDescription = "Akun") },
                label = { Text("Akun", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GritGreen,
                    selectedTextColor = GritGreen,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }

        // FAB mengambang di tengah, offset ke atas
        FloatingActionButton(
            onClick = onAddClick,
            shape = CircleShape,
            containerColor = GritGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Properti")
        }
    }
}
