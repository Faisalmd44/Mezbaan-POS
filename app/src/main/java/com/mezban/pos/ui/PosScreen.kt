package com.mezban.pos.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mezban.pos.data.MenuItemEntity
import com.mezban.pos.printer.BluetoothPrinterService
import com.mezban.pos.viewmodel.AppScreen
import com.mezban.pos.viewmodel.CartLine
import com.mezban.pos.viewmodel.PaymentMode
import com.mezban.pos.viewmodel.PosUiState
import com.mezban.pos.viewmodel.PosViewModel
import com.mezban.pos.viewmodel.ReceiptData
import com.mezban.pos.viewmodel.ViewMode

object MezbanColors {
    val Background = Color(0xFFF8F9FA)
    val Surface = Color(0xFFFFFFFF)
    val Charcoal = Color(0xFF1E1E24)
    val BorderGray = Color(0xFFE2E4E8)
    val Accent = Color(0xFFFF6B35)
    val VegGreen = Color(0xFF2E7D32)
    val NonVegRed = Color(0xFFC62828)
    val MutedText = Color(0xFF6B6B75)
}

private val MezbanShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val MezbanColorScheme = lightColorScheme(
    primary = MezbanColors.Charcoal,
    onPrimary = Color.White,
    secondary = MezbanColors.Accent,
    onSecondary = Color.White,
    background = MezbanColors.Background,
    surface = MezbanColors.Surface,
    onBackground = MezbanColors.Charcoal,
    onSurface = MezbanColors.Charcoal,
    outline = MezbanColors.BorderGray
)

@Composable
fun MezbanPosApp() {
    MaterialTheme(colorScheme = MezbanColorScheme, shapes = MezbanShapes) {
        val viewModel: PosViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        Column(modifier = Modifier.fillMaxSize().background(MezbanColors.Background)) {
            Box(modifier = Modifier.weight(1f)) {
                when (state.currentScreen) {
                    AppScreen.POS -> PosScreen(state = state, viewModel = viewModel)
                    AppScreen.SALES -> SalesScreen(state = state, viewModel = viewModel)
                    AppScreen.MENU -> MenuManageScreen(state = state, viewModel = viewModel)
                    AppScreen.STAFF -> StaffScreen(state = state, viewModel = viewModel)
                }
            }

            // Bottom Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MezbanColors.Surface)
                    .border(1.dp, MezbanColors.BorderGray)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    AppScreen.POS to "Billing",
                    AppScreen.SALES to "Sales",
                    AppScreen.MENU to "Menu",
                    AppScreen.STAFF to "Staff"
                ).forEach { (screen, label) ->
                    val selected = state.currentScreen == screen
                    Text(
                        text = label,
                        color = if (selected) MezbanColors.Charcoal else MezbanColors.MutedText,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(screen) }
                    )
                }
            }
        }

        state.errorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } },
                title = { Text("Notice") },
                text = { Text(message) }
            )
        }

        state.lastReceipt?.let { receipt ->
            ReceiptDialog(receipt = receipt, onDismiss = { viewModel.dismissReceipt() }, viewModel = viewModel)
        }
    }
}

@Composable
fun PosScreen(state: PosUiState, viewModel: PosViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 700.dp

        if (isWide) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(0.65f).fillMaxHeight()) {
                    MenuSection(state, viewModel, Modifier.fillMaxSize())
                }
                Divider(Modifier.fillMaxHeight().width(1.dp), color = MezbanColors.BorderGray)
                Column(Modifier.weight(0.35f).fillMaxHeight().background(MezbanColors.Surface)) {
                    CartPanel(state, viewModel)
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                MenuSection(state, viewModel, Modifier.weight(1f))
                if (state.cartItemCount > 0) {
                    CartSummaryBar(state = state, onExpand = { viewModel.openCheckout() })
                }
            }
        }
    }

    if (state.isCheckoutSheetOpen) {
        CheckoutBottomSheet(state = state, viewModel = viewModel)
    }
}

@Composable
private fun MenuSection(state: PosUiState, viewModel: PosViewModel, modifier: Modifier = Modifier) {
    Column(modifier.background(MezbanColors.Background)) {
        SearchAndToggleBar(state, viewModel)
        CategoryPillRow(state, viewModel)
        Box(Modifier.weight(1f)) {
            when {
                state.filteredMenuItems.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items found", color = MezbanColors.MutedText)
                    }
                }
                state.viewMode == ViewMode.GRID -> MenuGrid(state, viewModel)
                else -> MenuList(state, viewModel)
            }
        }
    }
}

@Composable
private fun SearchAndToggleBar(state: PosUiState, viewModel: PosViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search menu") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MezbanColors.Charcoal,
                unfocusedBorderColor = MezbanColors.BorderGray,
                focusedContainerColor = MezbanColors.Surface,
                unfocusedContainerColor = MezbanColors.Surface
            )
        )
        Spacer(Modifier.width(10.dp))
        ViewModeToggle(state.viewMode) { viewModel.toggleViewMode() }
    }
}

@Composable
private fun ViewModeToggle(viewMode: ViewMode, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(14.dp))
            .background(MezbanColors.Surface)
    ) {
        ToggleIconButton(Icons.Filled.GridView, selected = viewMode == ViewMode.GRID) {
            if (viewMode != ViewMode.GRID) onToggle()
        }
        ToggleIconButton(Icons.Filled.ViewList, selected = viewMode == ViewMode.LIST) {
            if (viewMode != ViewMode.LIST) onToggle()
        }
    }
}

@Composable
private fun ToggleIconButton(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MezbanColors.Charcoal else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) Color.White else MezbanColors.MutedText)
    }
}

@Composable
private fun CategoryPillRow(state: PosUiState, viewModel: PosViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        item {
            CategoryPill(text = "All", selected = state.selectedCategoryId == null) {
                viewModel.selectCategory(null)
            }
        }
        items(state.categories, key = { it.id }) { category ->
            CategoryPill(text = category.name, selected = state.selectedCategoryId == category.id) {
                viewModel.selectCategory(category.id)
            }
        }
    }
}

@Composable
private fun CategoryPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MezbanColors.Charcoal else MezbanColors.Surface)
            .border(1.dp, if (selected) MezbanColors.Charcoal else MezbanColors.BorderGray, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else MezbanColors.Charcoal,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MenuGrid(state: PosUiState, viewModel: PosViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        gridItems(state.filteredMenuItems, key = { it.id }) { menuItem ->
            val quantity = state.cart[menuItem.id]?.quantity ?: 0
            FoodItemCard(
                item = menuItem,
                quantity = quantity,
                onIncrement = { viewModel.incrementItem(menuItem) },
                onDecrement = { viewModel.decrementItem(menuItem.id) }
            )
        }
    }
}

@Composable
private fun MenuList(state: PosUiState, viewModel: PosViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.filteredMenuItems, key = { it.id }) { menuItem ->
            val quantity = state.cart[menuItem.id]?.quantity ?: 0
            FoodItemListRow(
                item = menuItem,
                quantity = quantity,
                onIncrement = { viewModel.incrementItem(menuItem) },
                onDecrement = { viewModel.decrementItem(menuItem.id) }
            )
        }
    }
}

@Composable
private fun FoodItemCard(item: MenuItemEntity, quantity: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MezbanColors.Surface)
            .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp).background(placeholderColorFor(item.categoryId)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(36.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (item.isVeg) MezbanColors.VegGreen else MezbanColors.NonVegRed)
            )
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                QuantityStepper(quantity = quantity, onIncrement = onIncrement, onDecrement = onDecrement)
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MezbanColors.Charcoal, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text("₹${formatPrice(item.price)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MezbanColors.Accent)
        }
    }
}

@Composable
private fun FoodItemListRow(item: MenuItemEntity, quantity: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MezbanColors.Surface)
            .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(placeholderColorFor(item.categoryId)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape)
                        .background(if (item.isVeg) MezbanColors.VegGreen else MezbanColors.NonVegRed)
                )
                Spacer(Modifier.width(6.dp))
                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MezbanColors.Charcoal)
            }
            Spacer(Modifier.height(2.dp))
            Text("₹${formatPrice(item.price)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MezbanColors.Accent)
        }
        QuantityStepper(quantity = quantity, onIncrement = onIncrement, onDecrement = onDecrement)
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    if (quantity == 0) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(MezbanColors.Charcoal).clickable(onClick = onIncrement),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    } else {
        Row(
            modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(MezbanColors.Charcoal).height(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(32.dp).clickable(onClick = onDecrement), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Remove, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Text(text = "$quantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp))
            Box(modifier = Modifier.size(32.dp).clickable(onClick = onIncrement), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun placeholderColorFor(categoryId: Long): Color {
    val palette = listOf(
        Color(0xFFFFB199), Color(0xFFFFCD94), Color(0xFFAED9C2),
        Color(0xFFF3B7C2), Color(0xFFC7B8EA), Color(0xFF9EC5DA), Color(0xFFE8C07D)
    )
    return palette[(categoryId % palette.size).toInt()]
}

@Composable
private fun CartSummaryBar(state: PosUiState, onExpand: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MezbanColors.Charcoal)
            .clickable(onClick = onExpand)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "${state.cartItemCount} item${if (state.cartItemCount > 1) "s" else ""}",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
            Text("₹${formatPrice(state.total)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("View Cart", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun CartPanel(state: PosUiState, viewModel: PosViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Current Order", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MezbanColors.Charcoal)
            if (state.cart.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text("Clear", color = MezbanColors.NonVegRed)
                }
            }
        }
        Divider(color = MezbanColors.BorderGray)

        if (state.cart.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = MezbanColors.MutedText, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Cart is empty", color = MezbanColors.MutedText)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.cartLines, key = { it.itemId }) { line ->
                    CartLineRow(
                        line = line,
                        onIncrement = { viewModel.incrementCartLine(line) },
                        onDecrement = { viewModel.decrementItem(line.itemId) }
                    )
                }
            }

            Divider(color = MezbanColors.BorderGray)
            Column(Modifier.padding(16.dp)) {
                SummaryLine("Subtotal", "₹${formatPrice(state.subtotal)}")
                if (state.discountAmount > 0) {
                    SummaryLine("Discount", "-₹${formatPrice(state.discountAmount)}")
                }
                SummaryLine("GST (${state.taxRate.toInt()}%)", "₹${formatPrice(state.taxAmount)}")
                Spacer(Modifier.height(6.dp))
                Divider(color = MezbanColors.BorderGray)
                Spacer(Modifier.height(6.dp))
                SummaryLine("Total", "₹${formatPrice(state.total)}", bold = true)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.openCheckout() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MezbanColors.Charcoal)
                ) {
                    Text("Proceed to Pay • ₹${formatPrice(state.total)}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CartLineRow(line: CartLine, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MezbanColors.Background)
            .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(line.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MezbanColors.Charcoal, maxLines = 1)
            Text("₹${formatPrice(line.unitPrice)} × ${line.quantity}", fontSize = 12.sp, color = MezbanColors.MutedText)
        }
        Text(
            "₹${formatPrice(line.lineTotal)}",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MezbanColors.Accent,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        QuantityStepper(quantity = line.quantity, onIncrement = onIncrement, onDecrement = onDecrement)
    }
}

@Composable
private fun SummaryLine(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            color = if (bold) MezbanColors.Charcoal else MezbanColors.MutedText,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 16.sp else 13.sp
        )
        Text(
            value,
            color = MezbanColors.Charcoal,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = if (bold) 16.sp else 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutBottomSheet(state: PosUiState, viewModel: PosViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { viewModel.closeCheckout() },
        sheetState = sheetState,
        containerColor = MezbanColors.Surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MezbanColors.Charcoal)
            Spacer(Modifier.height(12.dp))

            SummaryLine("Subtotal", "₹${formatPrice(state.subtotal)}")
            DiscountRow(state, viewModel)
            SummaryLine("GST (${state.taxRate.toInt()}%)", "₹${formatPrice(state.taxAmount)}")
            Divider(Modifier.padding(vertical = 8.dp), color = MezbanColors.BorderGray)
            SummaryLine("Amount Payable", "₹${formatPrice(state.total)}", bold = true)

            Spacer(Modifier.height(16.dp))
            Text("Payment Mode", fontWeight = FontWeight.SemiBold, color = MezbanColors.Charcoal)
            Spacer(Modifier.height(8.dp))
            PaymentModeSelector(state, viewModel)

            if (state.paymentMode == PaymentMode.CASH) {
                Spacer(Modifier.height(16.dp))
                CashCalculator(state, viewModel)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.placeOrder() },
                enabled = state.canPlaceOrder && !state.isProcessing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MezbanColors.Charcoal)
            ) {
                if (state.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Place Order • ₹${formatPrice(state.total)}", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DiscountRow(state: PosUiState, viewModel: PosViewModel) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Discount %", color = MezbanColors.MutedText, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            listOf(0.0, 5.0, 10.0).forEach { percent ->
                val selected = state.discountPercent == percent
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) MezbanColors.Charcoal else MezbanColors.Background)
                        .border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(10.dp))
                        .clickable { viewModel.setDiscountPercent(percent) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("${percent.toInt()}%", fontSize = 12.sp, color = if (selected) Color.White else MezbanColors.Charcoal)
                }
            }
        }
    }
}

@Composable
private fun PaymentModeSelector(state: PosUiState, viewModel: PosViewModel) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, MezbanColors.BorderGray, RoundedCornerShape(14.dp))
    ) {
        PaymentMode.values().forEach { mode ->
            val selected = state.paymentMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selected) MezbanColors.Charcoal else MezbanColors.Surface)
                    .clickable { viewModel.setPaymentMode(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(mode.label, color = if (selected) Color.White else MezbanColors.Charcoal, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CashCalculator(state: PosUiState, viewModel: PosViewModel) {
    Column {
        OutlinedTextField(
            value = state.cashReceivedText,
            onValueChange = { viewModel.setCashReceivedText(it) },
            label = { Text("Cash received") },
            leadingIcon = { Text("₹", color = MezbanColors.Charcoal, fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            quickCashSuggestions(state.total).forEach { amount ->
                OutlinedButton(
                    onClick = { viewModel.quickCash(amount) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MezbanColors.BorderGray)
                ) {
                    Text("₹${formatPrice(amount)}", fontSize = 12.sp, color = MezbanColors.Charcoal)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        state.changeDue?.let { change ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Change to return", color = MezbanColors.MutedText)
                Text("₹${formatPrice(change)}", fontWeight = FontWeight.Bold, color = MezbanColors.Accent)
            }
        }
    }
}

private fun quickCashSuggestions(total: Double): List<Double> {
    if (total <= 0.0) return emptyList()
    val rounded = kotlin.math.ceil(total / 10.0) * 10.0
    return listOf(total, rounded, rounded + 50.0, rounded + 100.0).distinct().take(4)
}

@Composable
private fun ReceiptDialog(receipt: ReceiptData, onDismiss: () -> Unit, viewModel: PosViewModel) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(MezbanColors.Background).padding(16.dp)
        ) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White).padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                ReceiptPreview(receipt = receipt, paperWidth = PaperWidth.MM80)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val devices = BluetoothPrinterService.getPairedDevices()
                        val printer = devices.firstOrNull()
                        if (printer != null) {
                            viewModel.printBluetoothReceipt(printer, receipt)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MezbanColors.Accent)
                ) {
                    Text("BT Print", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        val shareText = buildShareableReceiptText(receipt)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MezbanColors.Charcoal)
                ) {
                    Text("Done", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun buildShareableReceiptText(receipt: ReceiptData): String {
    val sb = StringBuilder()
    sb.appendLine("MEZBAN")
    sb.appendLine("Bill No: ${receipt.billNumber}")
    sb.appendLine("--------------------------------")
    receipt.lines.forEach { line ->
        sb.appendLine("${line.name}  x${line.qty}  Rs.${formatPrice(line.lineTotal)}")
    }
    sb.appendLine("--------------------------------")
    sb.appendLine("Subtotal: Rs.${formatPrice(receipt.subtotal)}")
    if (receipt.discountAmount > 0) sb.appendLine("Discount: -Rs.${formatPrice(receipt.discountAmount)}")
    sb.appendLine("GST: Rs.${formatPrice(receipt.taxAmount)}")
    sb.appendLine("TOTAL: Rs.${formatPrice(receipt.total)}")
    sb.appendLine("Payment: ${receipt.paymentMode.label}")
    return sb.toString()
}
