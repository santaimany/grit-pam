package com.example.grit.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grit.ui.components.BottomNavTab
import com.example.grit.ui.components.GritBottomBar
import com.example.grit.ui.components.GritTopBar
import com.example.grit.ui.components.PropertyCard
import com.example.grit.ui.theme.GritGreen
import com.example.grit.viewmodel.PropertyViewModel
import com.example.grit.viewmodel.TransactionViewModel

@Composable
fun HomeScreen(
    onPropertyClick: (String) -> Unit,
    onAddProperty: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: PropertyViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val activePropertyIds by transactionViewModel.activePropertyIds.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadProperties()
        transactionViewModel.loadActivePropertyIds()
    }

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
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GritGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(properties) { property ->
                    PropertyCard(
                        property = property,
                        onClick = { onPropertyClick(property.id) },
                        isRented = property.id in activePropertyIds
                    )
                }
            }
        }
    }
}
