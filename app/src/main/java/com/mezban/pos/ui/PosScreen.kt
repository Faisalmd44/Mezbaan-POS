package com.mezban.pos.ui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.mezban.pos.R
import com.mezban.pos.data.BillEntity
import com.mezban.pos.data.MenuItemEntity
import com.mezban.pos.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PosScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.allMenuItems.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("All Items") }
    var searchQuery by remember { mutableStateOf("") }
    var showCheckout by remember { mutableStateOf(false) }
    var showPrinterModal by remember { mutableStateOf(false) }
    var generatedBill by remember { mutableStateOf<BillEntity?>(null) }

    val categories = listOf("All Items", "Burgers", "Pizza", "Wraps", "Sides", "Sandwiches", "Fries", "Drinks")

    val totalItemsCount = cart.values.sum()
    val subtotalAmount = cart.entries.sumOf { entry ->
        val item = menuItems.find { it.id == entry.key }
        (item?.price ?: 0.0) * entry.value
    }

    val filteredItems = remember(menuItems, selectedCategory, searchQuery) {
        menuItems.filter { item ->
            (selectedCategory == "All Items" || item.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.Black, modifier = Modifier.size(38.dp)) {
                        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Mezbaan", contentScale = ContentScale.Fit, modifier = Modifier.padding(4.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("MEZBAAN", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = Color(0xFFFFECE5), shape = RoundedCornerShape(4.dp)) {
                        Text("POS", color = Color(0xFFFF5722), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F3F5),
                        modifier = Modifier.clickable { showPrinterModal = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF2E7D32), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🖨 ${viewModel.paperSize}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8F5E9)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF2E7D32), CircleShape))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(viewModel.currentCashier, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }
                    }
                }
            }

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search menu items...", fontSize = 13.sp, color = Color(0xFF9E9E9E)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9E9E9E)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedBorderColor = Color(0xFFEAEAEA)
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // CATEGORY CHIPS
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF212121) else Color.White,
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color(0xFF616161),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // FOOD GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val qty = cart[item.id] ?: 0
                    FoodCard(item = item, quantity = qty, onAdd = { viewModel.addItem(item) }, onRemove = { viewModel.removeItem(item) })
                }
            }
        }

        // FLOATING CART PILL
        if (totalItemsCount > 0) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFFFFECE5), modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.padding(7.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("$totalItemsCount Item${if (totalItemsCount > 1) "s" else ""} • ₹${String.format(Locale.ENGLISH, "%.2f", subtotalAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFF5722))
                    }
                    Button(
                        onClick = { showCheckout = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Cart / Checkout ->", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showCheckout) {
            CheckoutDialog(viewModel = viewModel, cart = cart, menuItems = menuItems, subtotal = subtotalAmount, onDismiss = { showCheckout = false }, onOrderPlaced = { b -> showCheckout = false; generatedBill = b })
        }

        generatedBill?.let { bill ->
            ReceiptDialog(
                bill = bill,
                paperSize = viewModel.paperSize,
                onDismiss = { generatedBill = null },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Mezbaan Bill #MZB-${bill.id}\nDate: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(bill.timestamp))}\nTotal: ₹${bill.totalAmount}\n${bill.itemsSummary}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                }
            )
        }

        if (showPrinterModal) {
            PrinterManagerDialog(viewModel = viewModel, onDismiss = { showPrinterModal = false })
        }
    }
}

@Composable
fun FoodCard(item: MenuItemEntity, quantity: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(115.dp).background(Color(0xFFEEEEEE))) {
                SubcomposeAsyncImage(
                    model = item.imageUri ?: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80",
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF212121)), contentAlignment = Alignment.Center) {
                            Text("Mezbaan", color = Color(0xFFE5A93C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Surface(shape = RoundedCornerShape(4.dp), color = Color.White, modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                    Box(modifier = Modifier.padding(3.dp).size(8.dp).background(if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFD32F2F), CircleShape))
                }

                if (quantity == 0) {
                    Surface(shape = CircleShape, color = Color(0xFF212121), modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd).size(32.dp).clickable { onAdd() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.padding(6.dp))
                    }
                } else {
                    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF212121), modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("–", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onRemove() }.padding(horizontal = 6.dp))
                            Text("$quantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onAdd() }.padding(horizontal = 6.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF212121))
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = "₹${String.format(Locale.ENGLISH, "%.2f", item.price)}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFFFF5722))
            }
        }
    }
}

@Composable
fun CheckoutDialog(viewModel: PosViewModel, cart: Map<Long, Int>, menuItems: List<MenuItemEntity>, subtotal: Double, onDismiss: () -> Unit, onOrderPlaced: (BillEntity) -> Unit) {
    var applyGst by remember { mutableStateOf(false) }
    var selectedPaymentMode by remember { mutableStateOf("CASH") }
    var cashReceivedText by remember { mutableStateOf(String.format(Locale.ENGLISH, "%.0f", subtotal)) }

    val gstAmount = if (applyGst) subtotal * 0.05 else 0.0
    val totalAmount = subtotal + gstAmount
    val cashReceived = cashReceivedText.toDoubleOrNull() ?: totalAmount

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Complete Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Cancel", color = Color(0xFF757575), modifier = Modifier.clickable { onDismiss() })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items (${cart.values.sum()})", color = Color(0xFF616161))
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Apply GST (5%)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(if (applyGst) "ON (+5% tax)" else "OFF (₹0.00 tax)", color = Color(0xFF9E9E9E), fontSize = 11.sp)
                            }
                            Switch(checked = applyGst, onCheckedChange = { applyGst = it })
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount Payable", fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", totalAmount)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFFFF5722))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("PAYMENT MODE", fontSize = 11.sp, color = Color(0xFF757575), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASH", "UPI", "OTHER").forEach { mode ->
                        val isSel = selectedPaymentMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF212121) else Color(0xFFF1F3F5),
                            modifier = Modifier.weight(1f).clickable { selectedPaymentMode = mode }
                        ) {
                            Text(mode, color = if (isSel) Color.White else Color(0xFF616161), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                if (selectedPaymentMode == "CASH") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Cash Received (₹)", fontSize = 12.sp, color = Color(0xFF757575))
                    OutlinedTextField(
                        value = cashReceivedText,
                        onValueChange = { cashReceivedText = it },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(totalAmount, totalAmount + 1, totalAmount + 50, totalAmount + 100).forEach { chipVal ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF1F3F5),
                                modifier = Modifier.clickable { cashReceivedText = String.format(Locale.ENGLISH, "%.0f", chipVal) }
                            ) {
                                Text("₹${String.format(Locale.ENGLISH, "%.0f", chipVal)}", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val cartList = cart.mapNotNull { (id, qty) ->
                            val item = menuItems.find { it.id == id }
                            if (item != null) Pair(item, qty) else null
                        }
                        viewModel.placeOrder(
                            items = cartList,
                            subtotal = subtotal,
                            gst = gstAmount,
                            total = totalAmount,
                            paymentMode = selectedPaymentMode,
                            cashTendered = cashReceived,
                            onSuccess = onOrderPlaced
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("✔ Place Order • ₹${String.format(Locale.ENGLISH, "%.2f", totalAmount)}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// EXACT MATCH FOR SCREENSHOT 7 (Order Receipt with 58mm/80mm, Table Layout, Cash Tendered, Change)
@Composable
fun ReceiptDialog(bill: BillEntity, paperSize: String, onDismiss: () -> Unit, onShare: () -> Unit) {
    var currentPaperSize by remember { mutableStateOf(paperSize) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Header with 58mm/80mm Toggle and Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFECE5), modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Order Receipt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("MZB-20260914-000${bill.id}", fontSize = 10.sp, color = Color(0xFF757575))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp).clickable { onShare() })
                        Surface(shape = RoundedCornerShape(6.dp), color = if (currentPaperSize == "58mm") Color(0xFF212121) else Color(0xFFF1F3F5), modifier = Modifier.clickable { currentPaperSize = "58mm" }) {
                            Text("58mm", fontSize = 10.sp, color = if (currentPaperSize == "58mm") Color.White else Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = if (currentPaperSize == "80mm") Color(0xFF212121) else Color(0xFFF1F3F5), modifier = Modifier.clickable { currentPaperSize = "80mm" }) {
                            Text("80mm", fontSize = 10.sp, color = if (currentPaperSize == "80mm") Color.White else Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp).clickable { onDismiss() })
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Printer Pill
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF2E7D32), CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Printer: MPT-II (58mm Mini Thermal)", fontSize = 11.sp, color = Color(0xFF424242))
                    }
                    Text("Change ⌄", fontSize = 11.sp, color = Color(0xFFFF5722), fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exact White Thermal Receipt Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MEZBAAN", fontWeight = FontWeight.Black, fontSize = 17.sp, fontFamily = FontFamily.Monospace)
                        Text("Fast Food & Quick Bites", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF616161))
                        Text("GSTIN: 07AAAAA0000A1Z5", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF757575))

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFEEEEEE))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bill No:", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("MZB-20260914-000${bill.id}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        val df = SimpleDateFormat("dd Sept 2026, hh:mm a", Locale.getDefault())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date:", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(df.format(Date(bill.timestamp)), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cashier:", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(bill.cashierName, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFEEEEEE))

                        // Table Headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("ITEM", modifier = Modifier.weight(2f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("QTY", modifier = Modifier.weight(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                            Text("PRICE", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                            Text("TOTAL", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEEEEEE))

                        bill.itemsSummary.split("\n").forEach { line ->
                            if (line.isNotBlank()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    val parts = line.split(" x")
                                    val name = parts.getOrNull(0) ?: "Item"
                                    val rest = parts.getOrNull(1)?.split(" = ₹")
                                    val qty = rest?.getOrNull(0) ?: "1"
                                    val total = rest?.getOrNull(1) ?: "0"
                                    val unitPrice = String.format(Locale.ENGLISH, "%.2f", (total.toDoubleOrNull() ?: 0.0) / (qty.toDoubleOrNull() ?: 1.0))

                                    Text(name, modifier = Modifier.weight(2f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(qty, modifier = Modifier.weight(0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                                    Text(unitPrice, modifier = Modifier.weight(1f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                                    Text(total, modifier = Modifier.weight(1f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFEEEEEE))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", bill.subtotal)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST (${if (bill.gstAmount > 0) "5%" else "0%"}):", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", bill.gstAmount)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL:", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", bill.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFEEEEEE))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Mode:", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(bill.paymentMode, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cash Tendered:", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", bill.cashTendered)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Change Returned:", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", bill.changeReturned)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Thank you for dining with MEZBAAN!", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("Please retain this receipt for any returns.", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF757575))
                        Text("* * * HAVE A GREAT DAY * * *", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("🖨 Print Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Done / New Sale", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// EXACT MATCH FOR SCREENSHOT 4 (Thermal Printers ESC/POS Modal with Scan / Pair, Roll size, MAC addresses)
@Composable
fun PrinterManagerDialog(viewModel: PosViewModel, onDismiss: () -> Unit) {
    var isScanning by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF212121), modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Thermal Printers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Bluetooth ESC/POS Device Manager", fontSize = 11.sp, color = Color(0xFF757575))
                        }
                    }
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.clickable { onDismiss() })
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Printer Card
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("ACTIVE PRINTER", fontSize = 10.sp, color = Color(0xFF558B2F), fontWeight = FontWeight.Bold)
                                Text(viewModel.selectedPrinter, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Surface(color = Color(0xFFC8E6C9), shape = RoundedCornerShape(4.dp)) {
                            Text("Ready", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Paper Roll Size Card
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Paper Roll Size", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Width for layout & character wrap", fontSize = 10.sp, color = Color(0xFF757575))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = if (viewModel.paperSize == "58mm") Color(0xFF212121) else Color(0xFFF1F3F5), modifier = Modifier.clickable { viewModel.paperSize = "58mm" }) {
                                    Text("58mm", fontSize = 11.sp, color = if (viewModel.paperSize == "58mm") Color.White else Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = if (viewModel.paperSize == "80mm") Color(0xFF212121) else Color(0xFFF1F3F5), modifier = Modifier.clickable { viewModel.paperSize = "80mm" }) {
                                    Text("80mm", fontSize = 11.sp, color = if (viewModel.paperSize == "80mm") Color.White else Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF5F5F5))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Auto-print on Order Completion", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Trigger Bluetooth print immediately when placed", fontSize = 10.sp, color = Color(0xFF757575))
                            }
                            Switch(checked = viewModel.autoPrintOnCompletion, onCheckedChange = { viewModel.autoPrintOnCompletion = it })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PAIRED THERMAL PRINTERS + Scan / Pair
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("PAIRED THERMAL PRINTERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF757575))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isScanning = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Scan / Pair", fontSize = 11.sp, color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val printers = listOf(
                    Triple("MPT-II (58mm Mini Thermal)", "00:11:22:33:44:55", "58mm"),
                    Triple("POS-58 Bluetooth Thermal", "66:77:88:99:AA:BB", "58mm"),
                    Triple("RPP02N Mobile POS Printer", "CC:DD:EE:FF:00:11", "58mm")
                )

                printers.forEach { (name, mac, width) ->
                    val isSel = viewModel.selectedPrinter == name
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF212121) else Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.selectedPrinter = name }
                    ) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF212121), modifier = Modifier.size(26.dp)) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.padding(5.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(color = Color(0xFFF1F3F5), shape = RoundedCornerShape(3.dp)) {
                                            Text(width, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    Text("Bluetooth ESC/POS • $mac", fontSize = 10.sp, color = Color(0xFF757575))
                                }
                            }
                            if (isSel) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            } else {
                                Box(modifier = Modifier.size(16.dp).background(Color(0xFFE0E0E0), CircleShape))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("🖨 Test Print (MEZBAAN)", fontSize = 11.sp, color = Color(0xFF212121), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.7f).height(44.dp)
                    ) {
                        Text(text = "Done", color = Color.White)
                    }
                }
            }
        }
    }
}
