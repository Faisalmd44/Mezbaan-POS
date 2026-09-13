package com.mezban.pos.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.mezban.pos.R
import com.mezban.pos.data.MenuItemEntity
import com.mezban.pos.viewmodel.PosViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MenuManageScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.allMenuItems.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var selectedCat by remember { mutableStateOf("All Categories") }
    var showNewItemDialog by remember { mutableStateOf(false) }

    val categories = listOf("All Categories", "Burgers (5)", "Pizza (5)", "Wraps (3)", "Sides (3)", "Sandwiches (4)", "Fries (4)", "Drinks (7)")

    val filteredItems = remember(menuItems, selectedCat) {
        if (selectedCat == "All Categories") menuItems
        else {
            val cleanCat = selectedCat.substringBefore(" (")
            menuItems.filter { it.category.equals(cleanCat, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp)) {
        Text("Menu Stock & Inventory", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Monitor real-time kitchen inventory, stock warnings, and availability", fontSize = 12.sp, color = Color(0xFF757575))

        Spacer(modifier = Modifier.height(14.dp))

        // Top 3 Action Buttons (Reset, Threshold, + New Item)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.weight(1f).clickable { coroutineScope.launch { viewModel.resetOfficialMenu() } }
            ) {
                Row(modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Official\nMenu", fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.weight(1.1f)
            ) {
                Row(modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Alert Threshold (5\nunits)", fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                }
            }

            Button(
                onClick = { showNewItemDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("+ New\nItem", fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Items", fontSize = 11.sp, color = Color(0xFFBDBDBD))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(12.dp))
                    }
                    Text("${menuItems.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                }
            }
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Low Stock (≤5) ⚠", fontSize = 10.sp, color = Color(0xFF757575))
                    Text("${menuItems.count { it.stockUnits in 1..5 }}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFF57F17))
                }
            }
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Out of Stock 🛇", fontSize = 10.sp, color = Color(0xFF757575))
                    Text("${menuItems.count { it.stockUnits == 0 }}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFD32F2F))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSel = selectedCat == cat
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSel) Color(0xFF212121) else Color.White,
                    shadowElevation = if (isSel) 2.dp else 0.dp,
                    modifier = Modifier.clickable { selectedCat = cat }
                ) {
                    Text(
                        cat,
                        color = if (isSel) Color.White else Color(0xFF616161),
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stock List with Stepper - 24 + and Available Switch
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(filteredItems, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(46.dp), color = Color(0xFFF1F3F5)) {
                                SubcomposeAsyncImage(
                                    model = item.imageUri ?: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${String.format(Locale.ENGLISH, "%.2f", item.price)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFF5722))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF5F5F5)) {
                                        Text(item.category, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF5F5F5)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${item.stockUnits} units", fontSize = 11.sp, color = Color(0xFF424242))
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF5F5F5))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            // Stepper (- 24 +)
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF8F9FA)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text("–", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { viewModel.updateStock(item, -1) }.padding(horizontal = 8.dp))
                                    Text("${item.stockUnits}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp))
                                    Text("+", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { viewModel.updateStock(item, 1) }.padding(horizontal = 8.dp))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Available", fontSize = 12.sp, color = if (item.isAvailable) Color(0xFF2E7D32) else Color(0xFF757575), fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(checked = item.isAvailable, onCheckedChange = { viewModel.toggleAvailability(item) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewItemDialog) {
        NewItemDialog(
            onDismiss = { showNewItemDialog = false },
            onSave = { name, price, cat, isVeg, stock ->
                viewModel.addNewMenuItem(name, price, cat, isVeg, stock)
                showNewItemDialog = false
            }
        )
    }
}

@Composable
fun NewItemDialog(onDismiss: () -> Unit, onSave: (String, Double, String, Boolean, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Burgers") }
    var isVeg by remember { mutableStateOf(true) }
    var stock by remember { mutableStateOf("20") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Add Menu Item", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.clickable { onDismiss() })
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Veg / Non-Veg:")
                    Spacer(modifier = Modifier.width(10.dp))
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val p = price.toDoubleOrNull() ?: 0.0
                        val s = stock.toIntOrNull() ?: 20
                        if (name.isNotBlank() && p > 0) {
                            onSave(name, p, category, isVeg, s)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Item", color = Color.White)
                }
            }
        }
    }
}
