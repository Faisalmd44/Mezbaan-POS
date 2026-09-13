package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mezban.pos.viewmodel.PosUiState
import com.mezban.pos.viewmodel.PosViewModel

@Composable
fun StaffScreen(state: PosUiState, viewModel: PosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MezbanColors.Background)
            .padding(16.dp)
    ) {
        Text("Staff Accounts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MezbanColors.Charcoal)
        Text("Current Logged In: ${state.currentStaff.name} (${state.currentStaff.role})", color = MezbanColors.Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.staffList, key = { it.id }) { s ->
                val isCurrent = s.id == state.currentStaff.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrent) MezbanColors.Charcoal else MezbanColors.Surface)
                        .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(12.dp))
                        .clickable { viewModel.switchStaff(s) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(s.name, fontWeight = FontWeight.Bold, color = if (isCurrent) androidx.compose.ui.graphics.Color.White else MezbanColors.Charcoal)
                        Text(s.role, fontSize = 12.sp, color = if (isCurrent) androidx.compose.ui.graphics.Color.White.copy(0.7f) else MezbanColors.MutedText)
                    }
                    if (isCurrent) {
                        Text("Active Cashier", color = MezbanColors.Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
