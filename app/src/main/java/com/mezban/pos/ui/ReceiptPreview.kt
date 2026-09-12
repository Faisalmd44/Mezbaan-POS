package com.mezban.pos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mezban.pos.viewmodel.PaymentMode
import com.mezban.pos.viewmodel.ReceiptData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaperWidth(val widthDp: Int, val fontSizeSp: Int, val dashCount: Int) {
    MM80(300, 13, 42),
    MM58(220, 11, 30)
}

fun formatPrice(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(Locale.US, "%.2f", value)
}

private fun dashLine(count: Int): String = "-".repeat(count)

@Composable
fun ReceiptPreview(
    receipt: ReceiptData,
    paperWidth: PaperWidth = PaperWidth.MM80,
    modifier: Modifier = Modifier
) {
    val mono = FontFamily.Monospace
    val fontSize = paperWidth.fontSizeSp.sp
    val dashCount = paperWidth.dashCount

    Column(
        modifier = modifier
            .width(paperWidth.widthDp.dp)
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text(
            text = "MEZBAN",
            fontFamily = mono,
            fontWeight = FontWeight.Bold,
            fontSize = (paperWidth.fontSizeSp + 8).sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Fast Food • Burgers • Pizza • Wraps",
            fontFamily = mono,
            fontSize = (paperWidth.fontSizeSp - 2).sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(dashLine(dashCount), fontFamily = mono, fontSize = fontSize, color = Color.Black)

        val sdf = SimpleDateFormat("dd/MM/yyyy  hh:mm a", Locale.getDefault())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Bill No:", fontFamily = mono, fontSize = fontSize, color = Color.Black)
            Text(receipt.billNumber, fontFamily = mono, fontSize = fontSize, color = Color.Black, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Date:", fontFamily = mono, fontSize = fontSize, color = Color.Black)
            Text(sdf.format(Date(receipt.timestampMillis)), fontFamily = mono, fontSize = fontSize, color = Color.Black)
        }
        Text(dashLine(dashCount), fontFamily = mono, fontSize = fontSize, color = Color.Black)

        ReceiptRow(mono, fontSize, "ITEM", "QTY", "RATE", "AMOUNT", isHeader = true)
        Text(dashLine(dashCount), fontFamily = mono, fontSize = fontSize, color = Color.Black)

        receipt.lines.forEach { line ->
            Text(line.name, fontFamily = mono, fontSize = fontSize, color = Color.Black, fontWeight = FontWeight.Medium)
            ReceiptRow(mono, fontSize, "", "x${line.qty}", "Rs.${formatPrice(line.unitPrice)}", "Rs.${formatPrice(line.lineTotal)}")
        }

        Text(dashLine(dashCount), fontFamily = mono, fontSize = fontSize, color = Color.Black)

        SummaryRow(mono, fontSize, "Subtotal", "Rs.${formatPrice(receipt.subtotal)}")
        if (receipt.discountAmount > 0.0) {
            SummaryRow(mono, fontSize, "Discount", "-Rs.${formatPrice(receipt.discountAmount)}")
        }
        SummaryRow(mono, fontSize, "GST (${receipt.taxRate.toInt()}%)", "Rs.${formatPrice(receipt.taxAmount)}")
        Text(dashLine(dashCount), fontFamily = mono, fontSize = fontSize, color = Color.Black)
        SummaryRow(mono, (fontSize.value + 2).sp, "TOTAL", "Rs.${formatPrice(receipt.total)}", bold = true)
        Text(dashLine(dashCount), fontFamily = mono, fontSize = fontSize, color = Color.Black)

        SummaryRow(mono, fontSize, "Payment Mode", receipt.paymentMode.label)
        if (receipt.paymentMode == PaymentMode.CASH) {
            receipt.cashReceived?.let { SummaryRow(mono, fontSize, "Cash Received", "Rs.${formatPrice(it)}") }
            receipt.changeGiven?.let { SummaryRow(mono, fontSize, "Change Returned", "Rs.${formatPrice(it)}") }
        }

        Spacer(Modifier.height(10.dp))
        Text("Thank you for visiting MEZBAN!", fontFamily = mono, fontSize = fontSize, color = Color.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Text("Order again soon :)", fontFamily = mono, fontSize = fontSize, color = Color.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("*".repeat(dashCount), fontFamily = mono, fontSize = fontSize, color = Color.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ReceiptRow(font: FontFamily, fontSize: TextUnit, name: String, qty: String, rate: String, amount: String, isHeader: Boolean = false) {
    val weight = if (isHeader) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth()) {
        Text(name, fontFamily = font, fontSize = fontSize, modifier = Modifier.weight(2f), fontWeight = weight, color = Color.Black)
        Text(qty, fontFamily = font, fontSize = fontSize, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = weight, color = Color.Black)
        Text(rate, fontFamily = font, fontSize = fontSize, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End, fontWeight = weight, color = Color.Black)
        Text(amount, fontFamily = font, fontSize = fontSize, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End, fontWeight = weight, color = Color.Black)
    }
}

@Composable
private fun SummaryRow(font: FontFamily, fontSize: TextUnit, label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = font, fontSize = fontSize, color = Color.Black, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontFamily = font, fontSize = fontSize, color = Color.Black, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
