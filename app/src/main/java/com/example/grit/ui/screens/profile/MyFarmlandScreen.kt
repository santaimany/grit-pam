package com.example.grit.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import java.text.NumberFormat
import java.util.Locale

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
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Kembali",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "My Farmland",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
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
                OutlinedButton(onClick = { propertyToDelete = null }) {
                    Text("Batal", color = TextPrimary)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun MyFarmlandCard(
    property: Property,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = property.imageUrl,
                contentDescription = property.namaProperti,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(120.dp).fillMaxHeight().background(BorderGray)
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(12.dp)
            ) {
                Text(
                    text = property.namaProperti,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp${NumberFormat.getNumberInstance(Locale("id", "ID")).format(property.harga)}/musim",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GritGreen
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextSecondary
                        )
                        Text(text = "${property.luasTanah} m²", fontSize = 12.sp, color = TextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(GritGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Rounded.Edit, "Edit", tint = GritGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Rounded.Delete, "Hapus", tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
