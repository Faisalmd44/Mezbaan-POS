package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mezban.pos.data.StaffEntity
import com.mezban.pos.viewmodel.PosViewModel

@Composable
fun StaffScreen(viewModel: PosViewModel) {
    val staffList by viewModel.allStaff.collectAsState(initial = emptyList())
    var showAddStaffDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Staff Accounts", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Current Logged In: ${viewModel.currentCashier} (${viewModel.currentCashier.uppercase()})", fontSize = 11.sp, color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showAddStaffDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Staff", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(staffList) { staff ->
                val isActive = viewModel.currentCashier == staff.name
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isActive) Color(0xFF212121) else Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isActive) Color(0xFF2E3138) else Color(0xFFF5F5F5),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = if (isActive) Color(0xFFFF5722) else Color(0xFF757575), modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isActive) Color.White else Color(0xFF212121))
                                Text("Role: ${staff.role.uppercase()}", fontSize = 12.sp, color = if (isActive) Color(0xFFBDBDBD) else Color(0xFF757575))
                            }
                        }

                        if (isActive) {
                            Surface(color = Color(0xFFFFECE5), shape = RoundedCornerShape(8.dp)) {
                                Text("Active Cashier", color = Color(0xFFFF5722), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.currentCashier = staff.name },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF212121)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text("Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onSave = { name, pin, role ->
                viewModel.addNewStaff(name, pin, role)
                showAddStaffDialog = false
            }
        )
    }
}

// EXACT MATCH FOR SCREENSHOT 9 (Add Staff Member Dialog)
@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Cashier") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Staff Member", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.clickable { onDismiss() })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Full Name", fontSize = 12.sp, color = Color(0xFF424242), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = { Text("e.g. John Doe", color = Color(0xFF9E9E9E), fontSize = 13.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Login PIN", fontSize = 12.sp, color = Color(0xFF424242), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    placeholder = { Text("4-digit PIN", color = Color(0xFF9E9E9E), fontSize = 13.sp) },
                    
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Role", fontSize = 12.sp, color = Color(0xFF424242), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val roles = listOf("Cashier", "Admin")
                    roles.forEach { role ->
                        val isSel = selectedRole == role
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF212121) else Color(0xFFF5F5F5),
                            modifier = Modifier.weight(1f).clickable { selectedRole = role }
                        ) {
                            Text(
                                role,
                                color = if (isSel) Color.White else Color(0xFF424242),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF424242))
                    }
                    Button(
                        onClick = {
                            if (fullName.isNotBlank()) {
                                onSave(fullName, pin, selectedRole)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Save Staff", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
