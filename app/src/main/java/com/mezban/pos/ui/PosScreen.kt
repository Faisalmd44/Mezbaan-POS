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
            // 1. TOP HEADER (Mezbaan + 58mm Printer + Admin)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Mezbaan",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("MEZBAAN", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFFFECE5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
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
                            Text("🖨 58mm", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF2E7D32), CircleShape))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }
                    }
                }
            }

            // 2. SEARCH BAR
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

            // 3. CATEGORY CHIPS
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            // 4. FOOD GRID WITH STEPPER BUTTONS
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val qty = cart[item.id] ?: 0
                    FoodCard(
                        item = item,
                        quantity = qty,
                        onAdd = { viewModel.addItem(item) },
                        onRemove = { viewModel.removeItem(item) }
                    )
                }
            }
        }

        // 5. FLOATING CART BAR (Exact AI Studio Orange Pill)
        if (totalItemsCount > 0) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFECE5),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.padding(7.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$totalItemsCount Item${if (totalItemsCount > 1) "s" else ""} • ₹${String.format(Locale.ENGLISH, "%.2f", subtotalAmount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFF5722)
                        )
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

        // CHECKOUT DIALOG
        if (showCheckout) {
            CheckoutDialog(
                viewModel = viewModel,
                cart = cart,
                menuItems = menuItems,
                subtotal = subtotalAmount,
                onDismiss = { showCheckout = false },
                onOrderPlaced = { bill ->
                    showCheckout = false
                    generatedBill = bill
                }
            )
        }

        // THERMAL RECEIPT SLIP MODAL
        generatedBill?.let { bill ->
            ReceiptDialog(
                bill = bill,
                onDismiss = { generatedBill = null },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Mezbaan Bill #${bill.id}\nTotal: ₹${bill.totalAmount}\n${bill.itemsSummary}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                }
            )
        }

        // PRINTER MANAGER MODAL
        if (showPrinterModal) {
            PrinterManagerDialog(
                viewModel = viewModel,
                onDismiss = { showPrinterModal = false }
            )
        }
    }
}

@Composable
fun FoodCard(item: MenuItemEntity, quantity: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
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

                // Veg / Non-Veg Indicator
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                ) {
                    Box(
                        modifier = Modifier.padding(3.dp).size(8.dp)
                            .background(if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFD32F2F), CircleShape)
                    )
                }

                // STEPPER / PLUS BUTTON (Matches AI Studio)
                if (quantity == 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd).size(32.dp).clickable { onAdd() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.padding(6.dp))
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
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
fun CheckoutDialog(
    viewModel: PosViewModel,
    cart: Map<Long, Int>,
    menuItems: List<MenuItemEntity>,
    subtotal: Double,
    onDismiss: () -> Unit,
    onOrderPlaced: (BillEntity) -> Unit
) {
    var applyGst by remember { mutableStateOf(false) }
    var selectedPaymentMode by remember { mutableStateOf("CASH") }
    var cashReceivedText by remember { mutableStateOf(String.format(Locale.ENGLISH, "%.0f", subtotal)) }

    val gstAmount = if (applyGst) subtotal * 0.05 else 0.0
    val totalAmount = subtotal + gstAmount
    val cashReceived = cashReceivedText.toDoubleOrNull() ?: totalAmount

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
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

@Composable
fun ReceiptDialog(bill: BillEntity, onDismiss: () -> Unit, onShare: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Order Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.clickable { onShare() })
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.clickable { onDismiss() })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thermal Receipt Preview Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFAFAFA),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MEZBAAN", fontWeight = FontWeight.Black, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
                        Text("Fast Food & Quick Bites", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF616161))
                        Text("GSTIN: 07AAAAA0000A1Z5", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF757575))
                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        val df = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        Text("Bill No: MZB-${bill.id}    Date: ${df.format(Date(bill.timestamp))}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("Cashier: Admin", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        Text(bill.itemsSummary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL:", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("₹${String.format(Locale.ENGLISH, "%.2f", bill.totalAmount)}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Text("Payment Mode: ${bill.paymentMode}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("*** THANK YOU FOR DINING WITH MEZBAAN ***", fontSize = 9.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🖨 Print Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done / New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PrinterManagerDialog(viewModel: PosViewModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Thermal Printers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Bluetooth ESC/POS Device Manager", fontSize = 11.sp, color = Color(0xFF757575))
                    }
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.clickable { onDismiss() })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("ACTIVE PRINTER", fontSize = 10.sp, color = Color(0xFF558B2F), fontWeight = FontWeight.Bold)
                            Text(viewModel.selectedPrinter, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Surface(color = Color(0xFFC8E6C9), shape = RoundedCornerShape(4.dp)) {
                            Text("Ready", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("PAIRED THERMAL PRINTERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF757575))
                Spacer(modifier = Modifier.height(8.dp))

                listOf("MPT-II (58mm Mini Thermal)", "POS-58 Bluetooth Thermal", "RPP02N Mobile POS Printer").forEach { pName ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.selectedPrinter = pName }
                    ) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(pName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            if (viewModel.selectedPrinter == pName) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Done", color = Color.White)
                }
            }
        }
    }
}
