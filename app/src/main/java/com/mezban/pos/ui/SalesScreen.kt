package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SalesScreen(viewModel: PosViewModel) {
    val bills by viewModel.allBills.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("Today") }
    val filters = listOf("Today", "Yesterday", "Last 7 Days", "This Month", "All Time")

    val filteredBills = remember(bills, selectedFilter) {
        when (selectedFilter) {
            "Today" -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                bills.filter { it.timestamp >= cal.timeInMillis }
            }
            "Yesterday" -> {
                val calStart = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
                bills.filter { it.timestamp in calStart.timeInMillis..calEnd.timeInMillis }
            }
            "Last 7 Days" -> {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                bills.filter { it.timestamp >= cal.timeInMillis }
            }
            "This Month" -> {
                val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
                bills.filter { it.timestamp >= cal.timeInMillis }
            }
            else -> bills
        }
    }

    val totalRevenue = filteredBills.sumOf { it.totalAmount }
    val cashTotal = filteredBills.filter { it.paymentMode.equals("Cash", true) }.sumOf { it.totalAmount }
    val upiTotal = filteredBills.filter { it.paymentMode.equals("UPI", true) }.sumOf { it.totalAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text("Sales & Reports", color = Color(0xFF1A1A1A), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Quick Date Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF212121) else Color.White,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.clickable { selectedFilter = filter }
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.White else Color(0xFF616161),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Revenue (${selectedFilter})", color = Color(0xFF757575), fontSize = 13.sp)
                Text("₹${String.format(Locale.ENGLISH, "%.2f", totalRevenue)}", color = Color(0xFFFF5722), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Orders", color = Color(0xFF757575), fontSize = 12.sp)
                        Text("${filteredBills.size}", color = Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Cash", color = Color(0xFF757575), fontSize = 12.sp)
                        Text("₹${String.format(Locale.ENGLISH, "%.0f", cashTotal)}", color = Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("UPI", color = Color(0xFF757575), fontSize = 12.sp)
                        Text("₹${String.format(Locale.ENGLISH, "%.0f", upiTotal)}", color = Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Transaction History", color = Color(0xFF1A1A1A), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredBills) { bill ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Bill #${bill.id}", color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
                            val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            Text(df.format(Date(bill.timestamp)), color = Color(0xFF757575), fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", bill.totalAmount)}", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
                            Text(bill.paymentMode, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
