package com.mezban.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mezban.pos.ui.MenuManageScreen
import com.mezban.pos.ui.PosScreen
import com.mezban.pos.ui.SalesScreen
import com.mezban.pos.ui.StaffScreen
import com.mezban.pos.viewmodel.PosViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedTab by remember { mutableIntStateOf(0) }

            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Billing") },
                            label = { Text("Billing") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.DateRange, contentDescription = "Sales") },
                            label = { Text("Sales") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.List, contentDescription = "Menu") },
                            label = { Text("Menu") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Staff") },
                            label = { Text("Staff") }
                        )
                    }
                }
            ) { innerPadding ->
                Surface(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        0 -> PosScreen(viewModel = viewModel)
                        1 -> SalesScreen(viewModel = viewModel)
                        2 -> MenuManageScreen(viewModel = viewModel)
                        3 -> StaffScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
