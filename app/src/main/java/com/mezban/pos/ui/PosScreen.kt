package com.mezban.pos.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import coil.compose.AsyncImage
import com.mezban.pos.R
import com.mezban.pos.data.MenuItemEntity
import com.mezban.pos.viewmodel.PosViewModel

@Composable
fun PosScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.allMenuItems.collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("All Items") }
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All Items", "Burgers", "Pizza", "Wraps", "Sides", "Sandwiches", "Fries", "Drinks")

    val filteredItems = remember(menuItems, selectedCategory, searchQuery) {
        menuItems.filter { item ->
            (selectedCategory == "All Items" || item.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // TOP HEADER BAR (Matching AI Studio)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black,
                    modifier = Modifier.size(38.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Mezbaan Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("MEZBAAN", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1A1A1A))
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = Color(0xFFFFECE5),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("POS", color = Color(0xFFFF5722), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F3F5)
                ) {
                    Text("🖨 58mm", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF2E7D32), CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                }
            }
        }

        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search menu items...", fontSize = 13.sp, color = Color(0xFF9E9E9E)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9E9E9E)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFE0E0E0),
                unfocusedBorderColor = Color(0xFFEAEAEA)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // CATEGORY CHIPS
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF212121) else Color.White,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else Color(0xFF616161),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // FOOD CARDS GRID
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems) { item ->
                FoodCard(item = item, onAdd = { viewModel.addToCart(item) })
            }
        }
    }
}

@Composable
fun FoodCard(item: MenuItemEntity, onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                AsyncImage(
                    model = item.imageUri ?: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80",
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Veg / Non-Veg Indicator Dot
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(8.dp)
                            .background(
                                color = if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                shape = CircleShape
                            )
                    )
                }

                // Plus Add Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF212121),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clickable { onAdd() }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "₹${String.format("%.2f", item.price)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(0xFFFF5722)
                )
            }
        }
    }
}
