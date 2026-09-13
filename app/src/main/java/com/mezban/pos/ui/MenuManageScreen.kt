package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mezban.pos.viewmodel.PosViewModel
import java.util.Locale

@Composable
fun MenuManageScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.allMenuItems.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text("Menu Management", color = Color(0xFF1A1A1A), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(menuItems) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", item.price)} • ${item.category}", color = Color(0xFF757575), fontSize = 13.sp)
                        }
                        Switch(
                            checked = item.isAvailable,
                            onCheckedChange = { viewModel.toggleItemAvailability(item) }
                        )
                    }
                }
            }
        }
    }
}
