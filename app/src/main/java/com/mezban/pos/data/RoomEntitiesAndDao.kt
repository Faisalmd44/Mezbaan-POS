package com.mezban.pos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val category: String,
    val isVeg: Boolean,
    val isAvailable: Boolean = true,
    val stockUnits: Int = 20,
    val imageUri: String? = null
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val subtotal: Double = totalAmount,
    val gstAmount: Double = 0.0,
    val cashTendered: Double = totalAmount,
    val changeReturned: Double = 0.0,
    val paymentMode: String = "CASH",
    val itemsSummary: String = "",
    val cashierName: String = "Admin"
)

@Entity(tableName = "staff_members")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pin: String,
    val role: String = "Cashier"
)

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items ORDER BY id ASC")
    fun getAllItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items")
    suspend fun getAllItemsList(): List<MenuItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MenuItemEntity): Long

    @Update
    suspend fun update(item: MenuItemEntity)

    @Query("DELETE FROM menu_items")
    suspend fun clearAll()
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Insert
    suspend fun insert(bill: BillEntity): Long
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff_members")
    fun getAllStaff(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff_members")
    suspend fun getAllStaffList(): List<StaffEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staff: StaffEntity): Long
}

@Database(entities = [MenuItemEntity::class, BillEntity::class, StaffEntity::class], version = 8, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
    abstract fun billDao(): BillDao
    abstract fun staffDao(): StaffDao
}
