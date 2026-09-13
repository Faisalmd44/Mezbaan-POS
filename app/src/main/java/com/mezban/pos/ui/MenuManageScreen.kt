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
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp)
    ) {
        Text("Menu Stock & Inventory", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Monitor real-time kitchen inventory & stock warnings", fontSize = 12.sp, color = Color(0xFF757575))
        Spacer(modifier = Modifier.height(14.dp))

        // 3 Summary Cards (Total 31, Low Stock, Out of Stock)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Items", fontSize = 11.sp, color = Color(0xFFBDBDBD))
                    Text("${menuItems.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                }
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Low Stock (≤5)", fontSize = 11.sp, color = Color(0xFF757575))
                    Text("0", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFF57F17))
                }
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Out of Stock", fontSize = 11.sp, color = Color(0xFF757575))
                    Text("0", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFD32F2F))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(menuItems) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", item.price)} • ${item.category}", color = Color(0xFFFF5722), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(if (item.isAvailable) "Available" else "Off", fontSize = 12.sp, color = if (item.isAvailable) Color(0xFF2E7D32) else Color(0xFF757575), fontWeight = FontWeight.Medium)
                            Switch(checked = item.isAvailable, onCheckedChange = { viewModel.toggleAvailability(item) })
                        }
                    }
                }
            }
        }
    }
}
