package com.mezban.pos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mezban.pos.data.CategoryEntity
import com.mezban.pos.data.MenuItemEntity
import com.mezban.pos.data.MezbanDatabase
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
    val changeGiven: Double?
)

data class PosUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null, // null == "All"
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.GRID,
    val allMenuItems: List<MenuItemEntity> = emptyList(),
    val cart: Map<Long, CartLine> = emptyMap(),
    val discountPercent: Double = 0.0,
    val isCheckoutSheetOpen: Boolean = false,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val cashReceivedText: String = "",
    val isProcessing: Boolean = false,
    val lastReceipt: ReceiptData? = null,
    val errorMessage: String? = null
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
    val taxRate: Double get() = 5.0 // flat GST slab for quick-service billing
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
}

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MezbanDatabase.getInstance(application)
    private val categoryDao = db.categoryDao()
    private val menuItemDao = db.menuItemDao()
    private val billDao = db.billDao()
    private val billCounterDao = db.billCounterDao()

    // ---- raw control state ----
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

    private data class Controls(
        val selectedCategoryId: Long?,
        val searchQuery: String,
        val viewMode: ViewMode,
        val cart: Map<Long, CartLine>,
        val discountPercent: Double
    )

    private data class CheckoutCore(
        val isOpen: Boolean,
        val paymentMode: PaymentMode,
        val cashText: String
    )

    private data class CheckoutExtra(
        val processing: Boolean,
        val lastReceipt: ReceiptData?,
        val error: String?
    )

    private val controlsFlow = combine(
        _selectedCategoryId, _searchQuery, _viewMode, _cart, _discountPercent
    ) { selectedCategoryId, searchQuery, viewMode, cart, discountPercent ->
        Controls(selectedCategoryId, searchQuery, viewMode, cart, discountPercent)
    }

    private val checkoutCoreFlow = combine(
        _isCheckoutSheetOpen, _paymentMode, _cashReceivedText
    ) { isOpen, mode, cashText -> CheckoutCore(isOpen, mode, cashText) }

    private val checkoutExtraFlow = combine(
        _isProcessing, _lastReceipt, _errorMessage
    ) { processing, receipt, error -> CheckoutExtra(processing, receipt, error) }

    private val checkoutFlow = combine(checkoutCoreFlow, checkoutExtraFlow) { core, extra ->
        core to extra
    }

    val uiState: StateFlow<PosUiState> = combine(
        categoryDao.observeAll(),
        menuItemDao.observeAll(),
        controlsFlow,
        checkoutFlow
    ) { categories, menuItems, controls, checkout ->
        val (core, extra) = checkout
        PosUiState(
            categories = categories,
            selectedCategoryId = controls.selectedCategoryId,
            searchQuery = controls.searchQuery,
            viewMode = controls.viewMode,
            allMenuItems = menuItems,
            cart = controls.cart,
            discountPercent = controls.discountPercent,
            isCheckoutSheetOpen = core.isOpen,
            paymentMode = core.paymentMode,
            cashReceivedText = core.cashText,
            isProcessing = extra.processing,
            lastReceipt = extra.lastReceipt,
            errorMessage = extra.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PosUiState()
    )

    // ---- menu / category / view actions ----

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
    }

    // ---- cart actions ----

    fun incrementItem(item: MenuItemEntity) {
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
        _cart.value = if (existing.quantity <= 1) {
            current - itemId
        } else {
            current + (itemId to existing.copy(quantity = existing.quantity - 1))
        }
    }

    fun removeItem(itemId: Long) {
        _cart.value = _cart.value - itemId
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _discountPercent.value = 0.0
        _cashReceivedText.value = ""
        _paymentMode.value = PaymentMode.CASH
    }

    fun setDiscountPercent(percent: Double) {
        _discountPercent.value = percent.coerceIn(0.0, 100.0)
    }

    // ---- checkout actions ----

    fun openCheckout() {
        if (_cart.value.isEmpty()) return
        _isCheckoutSheetOpen.value = true
    }

    fun closeCheckout() {
        _isCheckoutSheetOpen.value = false
    }

    fun setPaymentMode(mode: PaymentMode) {
        _paymentMode.value = mode
        if (mode != PaymentMode.CASH) _cashReceivedText.value = ""
    }

    fun setCashReceivedText(text: String) {
        // allow empty, or a plain decimal with up to 2 fraction digits
        if (text.isEmpty() || text.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _cashReceivedText.value = text
        }
    }

    fun quickCash(amount: Double) {
        _cashReceivedText.value = if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            amount.toString()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun dismissReceipt() {
        _lastReceipt.value = null
        clearCart()
        _isCheckoutSheetOpen.value = false
    }

    /** Persists the current cart as a bill inside a single Room ACID transaction. */
    fun placeOrder() {
        val state = uiState.value
        if (!state.canPlaceOrder) {
            _errorMessage.value = "Cannot place order — check the cart and payment details."
            return
        }
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val cartLines = state.cartLines
                val cartTriples = cartLines.map { line ->
                    Triple(line.itemId, line.name, line.unitPrice to line.quantity)
                }

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
                    changeGiven = state.changeDue
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save bill: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
