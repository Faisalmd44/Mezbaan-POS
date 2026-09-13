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
    val imageUri: String? = null
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val paymentMode: String = "Cash",
    val itemsSummary: String = ""
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

    @Delete
    suspend fun delete(item: MenuItemEntity)
}

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Insert
    suspend fun insert(bill: BillEntity): Long
}

@Database(entities = [MenuItemEntity::class, BillEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
    abstract fun billDao(): BillDao
}
