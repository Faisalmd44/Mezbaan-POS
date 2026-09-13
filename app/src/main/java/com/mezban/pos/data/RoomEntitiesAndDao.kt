package com.mezban.pos.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int
)

@Entity(
    tableName = "menu_items",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("categoryId")]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val price: Double,
    val isVeg: Boolean,
    val isAvailable: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pin: String,
    val role: String = "CASHIER", // ADMIN or CASHIER
    val isActive: Boolean = true
)

@Entity(tableName = "bill_counter")
data class BillCounterEntity(
    @PrimaryKey val dateKey: String,
    val lastSequence: Int
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billNumber: String,
    val timestamp: Long,
    val subtotal: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val paymentMode: String,
    val cashReceived: Double? = null,
    val changeGiven: Double? = null,
    val itemCount: Int,
    val staffName: String = "Admin"
)

@Entity(
    tableName = "bill_items",
    foreignKeys = [ForeignKey(
        entity = BillEntity::class,
        parentColumns = ["id"],
        childColumns = ["billId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("billId")]
)
data class BillItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val itemId: Long,
    val itemName: String,
    val unitPrice: Double,
    val quantity: Int,
    val lineTotal: Double
)

data class BillWithItems(
    @Embedded val bill: BillEntity,
    @Relation(parentColumn = "id", entityColumn = "billId")
    val items: List<BillItemEntity>
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun getAllOnce(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<MenuItemEntity>)

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setAvailability(id: Long, isAvailable: Boolean)

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun count(): Int
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE isActive = 1")
    fun observeActiveStaff(): Flow<List<StaffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(staff: StaffEntity)

    @Query("SELECT COUNT(*) FROM staff")
    suspend fun count(): Int
}

@Dao
interface BillCounterDao {
    @Query("SELECT * FROM bill_counter WHERE dateKey = :dateKey")
    suspend fun get(dateKey: String): BillCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(counter: BillCounterEntity)
}

@Dao
interface BillDao {
    @Insert
    suspend fun insertBill(bill: BillEntity): Long

    @Insert
    suspend fun insertBillItems(items: List<BillItemEntity>)

    @Transaction
    @Query("SELECT * FROM bills ORDER BY timestamp DESC")
    fun observeAllBillsWithItems(): Flow<List<BillWithItems>>

    @Transaction
    suspend fun createBillTransaction(
        counterDao: BillCounterDao,
        dateKey: String,
        subtotal: Double,
        taxRate: Double,
        taxAmount: Double,
        discountAmount: Double,
        totalAmount: Double,
        paymentMode: String,
        cashReceived: Double?,
        changeGiven: Double?,
        staffName: String,
        cartLines: List<Triple<Long, String, Pair<Double, Int>>>
    ): String {
        val existing = counterDao.get(dateKey)
        val nextSeq = (existing?.lastSequence ?: 0) + 1
        val billNumber = "MZB-$dateKey-" + nextSeq.toString().padStart(4, '0')

        val bill = BillEntity(
            billNumber = billNumber,
            timestamp = System.currentTimeMillis(),
            subtotal = subtotal,
            taxRate = taxRate,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            paymentMode = paymentMode,
            cashReceived = cashReceived,
            changeGiven = changeGiven,
            itemCount = cartLines.sumOf { it.third.second },
            staffName = staffName
        )
        val billId = insertBill(bill)

        val billItems = cartLines.map { (itemId, name, priceQty) ->
            val (unitPrice, qty) = priceQty
            BillItemEntity(
                billId = billId,
                itemId = itemId,
                itemName = name,
                unitPrice = unitPrice,
                quantity = qty,
                lineTotal = unitPrice * qty
            )
        }
        insertBillItems(billItems)
        counterDao.upsert(BillCounterEntity(dateKey, nextSeq))
        return billNumber
    }
}

@Database(
    entities = [
        CategoryEntity::class,
        MenuItemEntity::class,
        BillCounterEntity::class,
        BillEntity::class,
        BillItemEntity::class,
        StaffEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MezbanDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun billCounterDao(): BillCounterDao
    abstract fun billDao(): BillDao
    abstract fun staffDao(): StaffDao

    companion object {
        @Volatile
        private var INSTANCE: MezbanDatabase? = null

        fun getInstance(context: Context): MezbanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MezbanDatabase::class.java,
                    "mezban_pos.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    seedMenu(database)
                                    seedStaff(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedStaff(db: MezbanDatabase) {
            if (db.staffDao().count() > 0) return
            db.staffDao().insert(StaffEntity(name = "Admin", pin = "1234", role = "ADMIN"))
            db.staffDao().insert(StaffEntity(name = "Counter Cashier", pin = "0000", role = "CASHIER"))
        }

        suspend fun seedMenu(db: MezbanDatabase) {
            if (db.categoryDao().count() > 0) return

            val categoryNames = listOf(
                "Burgers", "Pizza", "Wraps", "Sides & Momos",
                "Sandwiches", "Fries", "Drinks & Combos"
            )
            db.categoryDao().insertAll(
                categoryNames.mapIndexed { index, name -> CategoryEntity(name = name, sortOrder = index) }
            )

            val savedCategories = db.categoryDao().getAllOnce().associateBy { it.name }
            val items = mutableListOf<MenuItemEntity>()

            fun addItems(categoryName: String, entries: List<Triple<String, Double, Boolean>>) {
                val categoryId = savedCategories.getValue(categoryName).id
                entries.forEachIndexed { index, (name, price, veg) ->
                    items += MenuItemEntity(
                        categoryId = categoryId,
                        name = name,
                        price = price,
                        isVeg = veg,
                        sortOrder = index
                    )
                }
            }

            addItems("Burgers", listOf(
                Triple("Veg Patty Burger", 59.0, true),
                Triple("Paneer Burger", 79.0, true),
                Triple("Chicken Patty Burger", 89.0, false),
                Triple("Chicken Zinger Burger", 99.0, false),
                Triple("American Chicken Burger", 130.0, false)
            ))
            addItems("Pizza", listOf(
                Triple("Veg Classic Corn & Cheese Pizza", 99.0, true),
                Triple("Farmhouse Delight Pizza", 119.0, true),
                Triple("Mezbaan Royal Paneer Pizza", 129.0, true),
                Triple("Mezbaan Tandoori Pizza", 179.0, false),
                Triple("Mezbaan Loaded Chicken Pizza", 219.0, false)
            ))
            addItems("Wraps", listOf(
                Triple("Chicken Wrap", 79.0, false),
                Triple("Chicken Cheesy Wrap", 89.0, false),
                Triple("American Hot Cheesy Wrap", 99.0, false)
            ))
            addItems("Sides & Momos", listOf(
                Triple("Chicken Popcorn", 99.0, false),
                Triple("Wings", 119.0, false),
                Triple("Kurkure Momos", 100.0, false)
            ))
            addItems("Sandwiches", listOf(
                Triple("Veg Sandwich", 70.0, true),
                Triple("Veg Cheese Sandwich", 90.0, true),
                Triple("Chicken Grill Sandwich", 90.0, false),
                Triple("Chicken Tandoori Sandwich", 100.0, false)
            ))
            addItems("Fries", listOf(
                Triple("Salted Fries", 50.0, true),
                Triple("Peri Peri Fries", 60.0, true),
                Triple("Loaded Fries", 70.0, true),
                Triple("Chicken Loaded Fries", 120.0, false)
            ))
            addItems("Drinks & Combos", listOf(
                Triple("Mojito (Regular)", 99.0, true),
                Triple("Mojito (Special)", 119.0, true),
                Triple("Combo Masti Loaded", 349.0, false)
            ))

            db.menuItemDao().insertAll(items)
        }
    }
}
