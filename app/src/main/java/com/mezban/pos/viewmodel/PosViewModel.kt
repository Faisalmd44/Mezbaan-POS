package com.mezban.pos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.mezban.pos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CartItem(val item: MenuItemEntity, var quantity: Int)

class PosViewModel(application: Application) : AndroidViewModel(application) {
    val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "mezbaan_pos_final.db"
    ).fallbackToDestructiveMigration().build()

    val allMenuItems: StateFlow<List<MenuItemEntity>> = database.menuItemDao().getAllItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allBills: StateFlow<List<BillEntity>> = database.billDao().getAllBills()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _cart = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val cart: StateFlow<Map<Long, Int>> = _cart

    var currentCashier by mutableStateOf("Admin")
    var selectedPrinter by mutableStateOf("MPT-II (58mm Mini Thermal)")
    var isPrinterReady by mutableStateOf(true)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            seedInitialDataIfEmpty()
        }
    }

    fun addItem(item: MenuItemEntity) {
        val current = _cart.value.toMutableMap()
        current[item.id] = (current[item.id] ?: 0) + 1
        _cart.value = current
    }

    fun removeItem(item: MenuItemEntity) {
        val current = _cart.value.toMutableMap()
        val qty = current[item.id] ?: 0
        if (qty > 1) {
            current[item.id] = qty - 1
        } else {
            current.remove(item.id)
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun toggleAvailability(item: MenuItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.menuItemDao().update(item.copy(isAvailable = !item.isAvailable))
        }
    }

    fun placeOrder(
        items: List<Pair<MenuItemEntity, Int>>,
        subtotal: Double,
        gst: Double,
        total: Double,
        paymentMode: String,
        cashTendered: Double,
        onSuccess: (BillEntity) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val summary = items.joinToString("\n") { "${it.first.name} x${it.second} = ₹${it.first.price * it.second}" }
            val bill = BillEntity(
                totalAmount = total,
                paymentMode = paymentMode,
                itemsSummary = summary,
                timestamp = System.currentTimeMillis()
            )
            val id = database.billDao().insert(bill)
            val generatedBill = bill.copy(id = id)
            clearCart()
            launch(Dispatchers.Main) {
                onSuccess(generatedBill)
            }
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val existing = database.menuItemDao().getAllItemsList()
        if (existing.isEmpty()) {
            val items = listOf(
                MenuItemEntity(name = "Veg Patty Burger", price = 59.0, category = "Burgers", isVeg = true, imageUri = "https://images.unsplash.com/photo-1585238342024-78d387f4a707?w=500&q=80"),
                MenuItemEntity(name = "Paneer Burger", price = 79.0, category = "Burgers", isVeg = true, imageUri = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&q=80"),
                MenuItemEntity(name = "Chicken Patty Burger", price = 89.0, category = "Burgers", isVeg = false, imageUri = "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=500&q=80"),
                MenuItemEntity(name = "Chicken Zinger Burger", price = 99.0, category = "Burgers", isVeg = false, imageUri = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=500&q=80"),
                MenuItemEntity(name = "American Chicken Burger", price = 130.0, category = "Burgers", isVeg = false, imageUri = "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=500&q=80"),

                MenuItemEntity(name = "Veg Classic Corn & Cheese Pizza", price = 99.0, category = "Pizza", isVeg = true, imageUri = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&q=80"),
                MenuItemEntity(name = "Farmhouse Delight Pizza", price = 119.0, category = "Pizza", isVeg = true, imageUri = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=500&q=80"),
                MenuItemEntity(name = "Mezbaan Royal Paneer Pizza", price = 129.0, category = "Pizza", isVeg = true, imageUri = "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?w=500&q=80"),
                MenuItemEntity(name = "Mezbaan Tandoori Pizza", price = 179.0, category = "Pizza", isVeg = false, imageUri = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500&q=80"),
                MenuItemEntity(name = "Mezbaan Loaded Chicken Pizza", price = 219.0, category = "Pizza", isVeg = false, imageUri = "https://images.unsplash.com/photo-1593560708920-61dd98c46a4e?w=500&q=80"),

                MenuItemEntity(name = "Chicken Wrap", price = 79.0, category = "Wraps", isVeg = false, imageUri = "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=500&q=80"),
                MenuItemEntity(name = "Chicken Cheesy Wrap", price = 89.0, category = "Wraps", isVeg = false, imageUri = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=500&q=80"),
                MenuItemEntity(name = "American Hot Cheesy Wrap", price = 99.0, category = "Wraps", isVeg = false, imageUri = "https://images.unsplash.com/photo-1529006557810-274b9b2fc783?w=500&q=80"),

                MenuItemEntity(name = "Chicken Popcorn", price = 99.0, category = "Sides", isVeg = false, imageUri = "https://images.unsplash.com/photo-1562967914-608f82629710?w=500&q=80"),
                MenuItemEntity(name = "Wings", price = 119.0, category = "Sides", isVeg = false, imageUri = "https://images.unsplash.com/photo-1527477378474-064e432c74d8?w=500&q=80"),
                MenuItemEntity(name = "Kurkure Momos", price = 100.0, category = "Sides", isVeg = false, imageUri = "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=500&q=80"),

                MenuItemEntity(name = "Veg Sandwich", price = 70.0, category = "Sandwiches", isVeg = true, imageUri = "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=500&q=80"),
                MenuItemEntity(name = "Veg Cheese Sandwich", price = 90.0, category = "Sandwiches", isVeg = true, imageUri = "https://images.unsplash.com/photo-1619860860774-1e2e17343432?w=500&q=80"),
                MenuItemEntity(name = "Chicken Grill Sandwich", price = 90.0, category = "Sandwiches", isVeg = false, imageUri = "https://images.unsplash.com/photo-1553909489-cd47e0907980?w=500&q=80"),
                MenuItemEntity(name = "Chicken Tandoori Sandwich", price = 100.0, category = "Sandwiches", isVeg = false, imageUri = "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500&q=80"),

                MenuItemEntity(name = "Salted Fries", price = 50.0, category = "Fries", isVeg = true, imageUri = "https://images.unsplash.com/photo-1576107232684-1279f3908594?w=500&q=80"),
                MenuItemEntity(name = "Peri Peri Fries", price = 60.0, category = "Fries", isVeg = true, imageUri = "https://images.unsplash.com/photo-1585109649139-366815a0d713?w=500&q=80"),
                MenuItemEntity(name = "Loaded Fries", price = 70.0, category = "Fries", isVeg = true, imageUri = "https://images.unsplash.com/photo-1585238341267-1cf923a1a5b8?w=500&q=80"),
                MenuItemEntity(name = "Chicken Loaded Fries", price = 120.0, category = "Fries", isVeg = false, imageUri = "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=500&q=80"),

                MenuItemEntity(name = "Thums Up Can", price = 30.0, category = "Drinks", isVeg = true, imageUri = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=500&q=80"),
                MenuItemEntity(name = "Coke Can", price = 30.0, category = "Drinks", isVeg = true, imageUri = "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=500&q=80"),
                MenuItemEntity(name = "Fanta Can", price = 30.0, category = "Drinks", isVeg = true, imageUri = "https://images.unsplash.com/photo-1624517452488-04869289c4ca?w=500&q=80"),
                MenuItemEntity(name = "Campa 10", price = 10.0, category = "Drinks", isVeg = true, imageUri = "https://images.unsplash.com/photo-1581098365948-6a5a912b7a49?w=500&q=80"),
                MenuItemEntity(name = "Campa 20", price = 20.0, category = "Drinks", isVeg = true, imageUri = "https://images.unsplash.com/photo-1581098365948-6a5a912b7a49?w=500&q=80"),
                MenuItemEntity(name = "Campa Energy", price = 30.0, category = "Drinks", isVeg = true, imageUri = "https://images.unsplash.com/photo-1527960471264-932f39eb5846?w=500&q=80"),
                MenuItemEntity(name = "Red Bull", price = 135.0, category = "Drinks", isVeg = true, imageUri = "https://images.unsplash.com/photo-1543253687-c931c8e01820?w=500&q=80")
            )
            items.forEach { database.menuItemDao().insert(it) }
        }
    }
}
