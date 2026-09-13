package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mezban.pos.viewmodel.PosUiState
import com.mezban.pos.viewmodel.PosViewModel

@Composable
fun MenuManageScreen(state: PosUiState, viewModel: PosViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MezbanColors.Background)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Menu Stock & Items", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MezbanColors.Charcoal)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MezbanColors.Charcoal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("+ New Item", fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.allMenuItems, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MezbanColors.Surface)
                        .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("₹${formatPrice(item.price)}", fontSize = 12.sp, color = MezbanColors.Accent, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (item.isAvailable) "Available" else "Out of Stock", fontSize = 12.sp, color = if (item.isAvailable) MezbanColors.VegGreen else MezbanColors.NonVegRed)
                        Switch(
                            checked = item.isAvailable,
                            onCheckedChange = { viewModel.toggleItemAvailability(item) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MezbanColors.Charcoal)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var priceText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Quick Item") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price (₹)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && p > 0) {
                        viewModel.addMenuItem(state.categories.firstOrNull()?.id ?: 1L, name, p, false)
                        showAddDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}
