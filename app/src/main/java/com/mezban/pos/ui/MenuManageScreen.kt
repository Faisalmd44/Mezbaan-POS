package com.mezban.pos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mezban.pos.ui.theme.MezbanColors
import com.mezban.pos.viewmodel.PosViewModel

@Composable
fun MenuManageScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.allMenuItems.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Menu Management", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MezbanColors.Surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, color = MezbanColors.TextPrimary, fontWeight = FontWeight.Bold)
                            Text("₹${item.price} • ${item.category}", color = MezbanColors.TextSecondary, fontSize = 13.sp)
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
