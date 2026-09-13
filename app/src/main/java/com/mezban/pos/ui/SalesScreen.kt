package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mezban.pos.viewmodel.PosUiState
import com.mezban.pos.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesScreen(state: PosUiState, viewModel: PosViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MezbanColors.Background)
            .padding(16.dp)
    ) {
        Text("Today's Sales", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MezbanColors.Charcoal)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard("Total Revenue", "₹${formatPrice(state.todaySalesTotal)}", Modifier.weight(1f))
            SummaryCard("Bills", "${state.billsHistory.size}", Modifier.weight(0.7f))
            SummaryCard("Cash / UPI", "₹${formatPrice(state.todayCashTotal)} / ₹${formatPrice(state.todayUpiTotal)}", Modifier.weight(1.3f))
        }

        Spacer(Modifier.height(20.dp))
        Text("Recent Bills (Tap to Reprint)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MezbanColors.Charcoal)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.billsHistory, key = { it.bill.id }) { item ->
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MezbanColors.Surface)
                        .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(12.dp))
                        .clickable { viewModel.reprintBill(item) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.bill.billNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${sdf.format(Date(item.bill.timestamp))} • By ${item.bill.staffName}", fontSize = 12.sp, color = MezbanColors.MutedText)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${formatPrice(item.bill.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MezbanColors.Accent)
                        Text(item.bill.paymentMode, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MezbanColors.Charcoal)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MezbanColors.Surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = MezbanColors.MutedText)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MezbanColors.Charcoal)
        }
    }
}
