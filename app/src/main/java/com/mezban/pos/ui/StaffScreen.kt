package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun StaffScreen(viewModel: PosViewModel) {
    val staffList = listOf("Admin" to "Role: ADMIN", "Cashier 1" to "Role: CASHIER", "Cashier 2" to "Role: CASHIER")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp)) {
        Text("Staff Accounts", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Current Logged In: ${viewModel.currentCashier} (${viewModel.currentCashier.uppercase()})", fontSize = 12.sp, color = Color(0xFFFF5722), fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(16.dp))

        staffList.forEach { (name, role) ->
            val isActive = viewModel.currentCashier == name
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isActive) Color(0xFF212121) else Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else Color(0xFF212121))
                        Text(role, fontSize = 12.sp, color = if (isActive) Color(0xFFBDBDBD) else Color(0xFF757575))
                    }
                    if (isActive) {
                        Surface(color = Color(0xFFFFECE5), shape = RoundedCornerShape(6.dp)) {
                            Text("Active Cashier", color = Color(0xFFFF5722), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.currentCashier = name },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Switch", fontSize = 11.sp, color = Color(0xFF212121))
                        }
                    }
                }
            }
        }
    }
}
