package com.mezban.pos.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mezban.pos.data.BillWithItems
import com.mezban.pos.data.CategoryEntity
import com.mezban.pos.data.MenuItemEntity
import com.mezban.pos.data.MezbanDatabase
import com.mezban.pos.data.StaffEntity
import com.mezban.pos.printer.BluetoothPrinterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ViewMode { GRID, LIST }
enum class AppScreen { POS, SALES, MENU, STAFF }

enum class PaymentMode(val label: String) {
    CASH("Cash"), UPI("UPI"), OTHER("Other")
}

data class CartLine(
    val itemId: Long,
    val name: String,
    val unitPrice: Double,
    val quantity: Int,
    val isVeg: Boolean
) {
    val lineTotal: Double get() = unitPrice * quantity
}

data class ReceiptLine(val name: String, val qty: Int, val unitPrice: Double, val lineTotal: Double)

data class ReceiptData(
    val billNumber: String,
    val timestampMillis: Long,
    val lines: List<ReceiptLine>,
    val subtotal: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val total: Double,
    val paymentMode: PaymentMode,
    val cashReceived: Double?,
    val changeGiven: Double?,
    val staffName: String = "Admin"
)

data class PosUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.GRID,
    val currentScreen: AppScreen = AppScreen.POS,
    val allMenuItems: List<MenuItemEntity> = emptyList(),
    val cart: Map<Long, CartLine> = emptyMap(),
    val discountPercent: Double = 0.0,
    val isCheckoutSheetOpen: Boolean = false,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val cashReceivedText: String = "",
    val isProcessing: Boolean = false,
    val lastReceipt: ReceiptData? = null,
    val errorMessage: String? = null,
    val billsHistory: List<BillWithItems> = emptyList(),
    val staffList: List<StaffEntity> = emptyList(),
    val currentStaff: StaffEntity = StaffEntity(name = "Admin", pin = "1234", role = "ADMIN"),
    val printerStatus: String = "Ready"
) {
    val filteredMenuItems: List<MenuItemEntity>
        get() {
            val byCategory = if (selectedCategoryId == null) allMenuItems
            else allMenuItems.filter { it.categoryId == selectedCategoryId }
            return if (searchQuery.isBlank()) byCategory
            else byCategory.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

    val cartLines: List<CartLine> get() = cart.values.sortedBy { it.name }
    val cartItemCount: Int get() = cart.values.sumOf { it.quantity }
    val subtotal: Double get() = cart.values.sumOf { it.lineTotal }
    val discountAmount: Double get() = subtotal * (discountPercent / 100.0)
    private val taxableAmount: Double get() = (subtotal - discountAmount).coerceAtLeast(0.0)
    val taxRate: Double get() = 5.0
    val taxAmount: Double get() = taxableAmount * (taxRate / 100.0)
    val total: Double get() = (taxableAmount + taxAmount).coerceAtLeast(0.0)
    val cashReceived: Double? get() = cashReceivedText.toDoubleOrNull()

    val changeDue: Double?
        get() {
            if (paymentMode != PaymentMode.CASH) return null
            val received = cashReceived ?: return null
            return (received - total).coerceAtLeast(0.0)
        }

    val canPlaceOrder: Boolean
        get() {
            if (cart.isEmpty()) return false
            if (paymentMode == PaymentMode.CASH) {
                val received = cashReceived ?: return false
                return received >= total
            }
            return true
        }

    val todaySalesTotal: Double get() = billsHistory.sumOf { it.bill.totalAmount }
    val todayCashTotal: Double get() = billsHistory.filter { it.bill.paymentMode == "CASH" }.sumOf { it.bill.totalAmount }
    val todayUpiTotal: Double get() = billsHistory.filter { it.bill.paymentMode == "UPI" }.sumOf { it.bill.totalAmount }
}

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MezbanDatabase.getInstance(application)
    private val categoryDao = db.categoryDao()
    private val menuItemDao = db.menuItemDao()
    private val billDao = db.billDao()
    private val billCounterDao = db.billCounterDao()
    private val staffDao = db.staffDao()

    private val _currentScreen = MutableStateFlow(AppScreen.POS)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    private val _cart = MutableStateFlow<Map<Long, CartLine>>(emptyMap())
    private val _discountPercent = MutableStateFlow(0.0)
    private val _isCheckoutSheetOpen = MutableStateFlow(false)
    private val _paymentMode = MutableStateFlow(PaymentMode.CASH)
    private val _cashReceivedText = MutableStateFlow("")
    private val _isProcessing = MutableStateFlow(false)
    private val _lastReceipt = MutableStateFlow<ReceiptData?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _currentStaff = MutableStateFlow(StaffEntity(name = "Admin", pin = "1234", role = "ADMIN"))
    private val _printerStatus = MutableStateFlow("Ready")

    val uiState: StateFlow<PosUiState> = combine(
        categoryDao.observeAll(),
        menuItemDao.observeAll(),
        billDao.observeAllBillsWithItems(),
        staffDao.observeActiveStaff(),
        _currentScreen
    ) { categories, menuItems, bills, staff, screen ->
        PosUiState(
            categories = categories,
            allMenuItems = menuItems,
            billsHistory = bills,
            staffList = staff,
            currentScreen = screen,
            currentStaff = _currentStaff.value,
            selectedCategoryId = _selectedCategoryId.value,
            searchQuery = _searchQuery.value,
            viewMode = _viewMode.value,
            cart = _cart.value,
            discountPercent = _discountPercent.value,
            isCheckoutSheetOpen = _isCheckoutSheetOpen.value,
            paymentMode = _paymentMode.value,
            cashReceivedText = _cashReceivedText.value,
            isProcessing = _isProcessing.value,
            lastReceipt = _lastReceipt.value,
            errorMessage = _errorMessage.value,
            printerStatus = _printerStatus.value
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PosUiState())

    fun navigateTo(screen: AppScreen) { _currentScreen.value = screen }
    fun selectCategory(categoryId: Long?) { _selectedCategoryId.value = categoryId }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun toggleViewMode() { _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }

    fun incrementItem(item: MenuItemEntity) {
        if (!item.isAvailable) return
        val current = _cart.value
        val existing = current[item.id]
        val updated = existing?.copy(quantity = existing.quantity + 1)
            ?: CartLine(item.id, item.name, item.price, 1, item.isVeg)
        _cart.value = current + (item.id to updated)
    }

    fun incrementCartLine(line: CartLine) {
        _cart.value = _cart.value + (line.itemId to line.copy(quantity = line.quantity + 1))
    }

    fun decrementItem(itemId: Long) {
        val current = _cart.value
        val existing = current[itemId] ?: return
        _cart.value = if (existing.quantity <= 1) current - itemId
        else current + (itemId to existing.copy(quantity = existing.quantity - 1))
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _discountPercent.value = 0.0
        _cashReceivedText.value = ""
        _paymentMode.value = PaymentMode.CASH
    }

    fun setDiscountPercent(percent: Double) { _discountPercent.value = percent.coerceIn(0.0, 100.0) }
    fun openCheckout() { if (_cart.value.isNotEmpty()) _isCheckoutSheetOpen.value = true }
    fun closeCheckout() { _isCheckoutSheetOpen.value = false }
    fun setPaymentMode(mode: PaymentMode) {
        _paymentMode.value = mode
        if (mode != PaymentMode.CASH) _cashReceivedText.value = ""
    }
    fun setCashReceivedText(text: String) { _cashReceivedText.value = text }
    fun quickCash(amount: Double) { _cashReceivedText.value = amount.toInt().toString() }
    fun clearError() { _errorMessage.value = null }
    fun dismissReceipt() { _lastReceipt.value = null; clearCart(); _isCheckoutSheetOpen.value = false }

    fun toggleItemAvailability(item: MenuItemEntity) {
        viewModelScope.launch { menuItemDao.setAvailability(item.id, !item.isAvailable) }
    }

    fun addMenuItem(categoryId: Long, name: String, price: Double, isVeg: Boolean) {
        viewModelScope.launch {
            menuItemDao.insert(MenuItemEntity(categoryId = categoryId, name = name, price = price, isVeg = isVeg))
        }
    }

    fun addStaff(name: String, pin: String, role: String) {
        viewModelScope.launch { staffDao.insert(StaffEntity(name = name, pin = pin, role = role)) }
    }

    fun switchStaff(staff: StaffEntity) { _currentStaff.value = staff }

    fun reprintBill(billWithItems: BillWithItems) {
        val b = billWithItems.bill
        val mode = when (b.paymentMode) {
            "UPI" -> PaymentMode.UPI
            "OTHER" -> PaymentMode.OTHER
            else -> PaymentMode.CASH
        }
        _lastReceipt.value = ReceiptData(
            billNumber = b.billNumber,
            timestampMillis = b.timestamp,
            lines = billWithItems.items.map { ReceiptLine(it.itemName, it.quantity, it.unitPrice, it.lineTotal) },
            subtotal = b.subtotal,
            taxRate = b.taxRate,
            taxAmount = b.taxAmount,
            discountAmount = b.discountAmount,
            total = b.totalAmount,
            paymentMode = mode,
            cashReceived = b.cashReceived,
            changeGiven = b.changeGiven,
            staffName = b.staffName
        )
    }

    fun printBluetoothReceipt(device: BluetoothDevice, receipt: ReceiptData) {
        viewModelScope.launch {
            _printerStatus.value = "Printing..."
            val res = BluetoothPrinterService.printReceipt(device, receipt)
            _printerStatus.value = if (res.isSuccess) "Printed Successfully" else "Print Failed"
        }
    }

    fun placeOrder() {
        val state = uiState.value
        if (!state.canPlaceOrder) return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val cartLines = state.cartLines
                val cartTriples = cartLines.map { Triple(it.itemId, it.name, it.unitPrice to it.quantity) }

                val billNumber = billDao.createBillTransaction(
                    counterDao = billCounterDao,
                    dateKey = dateKey,
                    subtotal = state.subtotal,
                    taxRate = state.taxRate,
                    taxAmount = state.taxAmount,
                    discountAmount = state.discountAmount,
                    totalAmount = state.total,
                    paymentMode = state.paymentMode.name,
                    cashReceived = state.cashReceived,
                    changeGiven = state.changeDue,
                    staffName = state.currentStaff.name,
                    cartLines = cartTriples
                )

                _lastReceipt.value = ReceiptData(
                    billNumber = billNumber,
                    timestampMillis = System.currentTimeMillis(),
                    lines = cartLines.map { ReceiptLine(it.name, it.quantity, it.unitPrice, it.lineTotal) },
                    subtotal = state.subtotal,
                    taxRate = state.taxRate,
                    taxAmount = state.taxAmount,
                    discountAmount = state.discountAmount,
                    total = state.total,
                    paymentMode = state.paymentMode,
                    cashReceived = state.cashReceived,
                    changeGiven = state.changeDue,
                    staffName = state.currentStaff.name
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
