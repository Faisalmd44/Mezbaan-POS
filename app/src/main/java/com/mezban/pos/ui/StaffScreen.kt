package com.mezban.pos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mezban.pos.viewmodel.PosViewModel

@Composable
fun StaffScreen(viewModel: PosViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Staff / Counter Shift", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Logged in as: Admin (Main Counter)", fontSize = 14.sp)
    }
}
