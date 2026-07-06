package com.example.stoku.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.stoku.data.local.dao.BrandDao
import com.example.stoku.data.local.dao.CategoryDao
import com.example.stoku.data.local.dao.PriceHistoryDao
import com.example.stoku.data.local.dao.ProductDao
import com.example.stoku.data.local.dao.TransactionDao
import com.example.stoku.data.local.dao.UserDao
import com.example.stoku.data.local.entity.BrandEntity
import com.example.stoku.data.local.entity.CategoryEntity
import com.example.stoku.data.local.entity.PriceHistoryEntity
import com.example.stoku.data.local.entity.ProductEntity
import com.example.stoku.data.local.entity.TransactionEntity
import com.example.stoku.data.local.entity.UserEntity
import com.example.stoku.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        PriceHistoryEntity::class,
        BrandEntity::class,
        CategoryEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun brandDao(): BrandDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "stoku.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `brands` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `created_at` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `created_at` INTEGER NOT NULL)",
                )
            }
        }

        fun create(context: Context, applicationScope: CoroutineScope): AppDatabase {
            var instance: AppDatabase? = null
            instance = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        instance?.let { database ->
                            applicationScope.launch {
                                seed(
                                    database.userDao(),
                                    database.productDao(),
                                    database.transactionDao(),
                                    database.brandDao(),
                                    database.categoryDao(),
                                )
                            }
                        }
                    }
                })
                .build()
            return instance
        }

        private suspend fun seed(
            userDao: UserDao,
            productDao: ProductDao,
            transactionDao: TransactionDao,
            brandDao: BrandDao,
            categoryDao: CategoryDao,
        ) {
            userDao.insertAll(
                listOf(
                    UserEntity(username = "owner", passwordHash = PasswordHasher.sha256("owner123"), role = "owner"),
                    UserEntity(username = "admin", passwordHash = PasswordHasher.sha256("admin123"), role = "admin"),
                    UserEntity(username = "kasir", passwordHash = PasswordHasher.sha256("kasir123"), role = "kasir"),
                ),
            )

            val now = System.currentTimeMillis()
            productDao.insertAll(
                listOf(
                    // Device
                    ProductEntity(sku = "DEV-001", brandName = "Vaporesso", productName = "XROS 4 Mini Pod Kit", category = "Device", stock = 12, costPrice = 280_000, sellingPrice = 380_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "DEV-002", brandName = "SMOK", productName = "Nord 5 Pod System", category = "Device", stock = 8, costPrice = 320_000, sellingPrice = 430_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "DEV-003", brandName = "Uwell", productName = "Caliburn G3 Pro", category = "Device", stock = 3, costPrice = 260_000, sellingPrice = 360_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "DEV-004", brandName = "Geekvape", productName = "Aegis Legend 3 Mod", category = "Device", stock = 5, costPrice = 650_000, sellingPrice = 850_000, lowStockThreshold = 3, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "DEV-005", brandName = "Lost Vape", productName = "Ursa Nano 2 Pod", category = "Device", stock = 0, costPrice = 310_000, sellingPrice = 420_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    // Liquid
                    ProductEntity(sku = "LIQ-001", brandName = "Saltnic Lab", productName = "Taro Milk 30ml", category = "Liquid", stock = 25, costPrice = 45_000, sellingPrice = 70_000, lowStockThreshold = 10, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "LIQ-002", brandName = "Saltnic Lab", productName = "Strawberry Lychee 30ml", category = "Liquid", stock = 18, costPrice = 45_000, sellingPrice = 70_000, lowStockThreshold = 10, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "LIQ-003", brandName = "Juice Bros", productName = "Mango Ice 60ml", category = "Liquid", stock = 9, costPrice = 80_000, sellingPrice = 120_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "LIQ-004", brandName = "Juice Bros", productName = "Watermelon Mint 60ml", category = "Liquid", stock = 4, costPrice = 80_000, sellingPrice = 120_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "LIQ-005", brandName = "Naked 100", productName = "Hawaiian POG 60ml", category = "Liquid", stock = 6, costPrice = 110_000, sellingPrice = 160_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    // Coil
                    ProductEntity(sku = "COI-001", brandName = "Vaporesso", productName = "GTX Coil 0.6ohm (5pcs)", category = "Coil", stock = 30, costPrice = 35_000, sellingPrice = 55_000, lowStockThreshold = 10, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "COI-002", brandName = "SMOK", productName = "Nord DC 0.6ohm MTL (3pcs)", category = "Coil", stock = 22, costPrice = 30_000, sellingPrice = 50_000, lowStockThreshold = 10, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "COI-003", brandName = "Uwell", productName = "Caliburn G3 Coil 0.9ohm (4pcs)", category = "Coil", stock = 2, costPrice = 40_000, sellingPrice = 65_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    // Baterai
                    ProductEntity(sku = "BAT-001", brandName = "Samsung", productName = "Baterai 18650 25R 2500mAh", category = "Baterai", stock = 40, costPrice = 55_000, sellingPrice = 80_000, lowStockThreshold = 10, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "BAT-002", brandName = "Sony", productName = "Baterai 18650 VTC5A 2600mAh", category = "Baterai", stock = 15, costPrice = 65_000, sellingPrice = 95_000, lowStockThreshold = 10, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "BAT-003", brandName = "Molicel", productName = "Baterai 21700 P42A 4200mAh", category = "Baterai", stock = 3, costPrice = 90_000, sellingPrice = 130_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    // Aksesori
                    ProductEntity(sku = "AKS-001", brandName = "Generic", productName = "Drip Tip 810 Resin Wide Bore", category = "Aksesori", stock = 20, costPrice = 15_000, sellingPrice = 30_000, lowStockThreshold = 5, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "AKS-002", brandName = "Coil Master", productName = "Coil Jig 0.5–3.0mm", category = "Aksesori", stock = 7, costPrice = 50_000, sellingPrice = 80_000, lowStockThreshold = 3, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "AKS-003", brandName = "Generic", productName = "Cotton Bacon Prime (20 strips)", category = "Aksesori", stock = 35, costPrice = 18_000, sellingPrice = 30_000, lowStockThreshold = 10, createdAt = now, updatedAt = now),
                    ProductEntity(sku = "AKS-004", brandName = "Nitecore", productName = "Charger Baterai SC4 4-Slot", category = "Aksesori", stock = 4, costPrice = 180_000, sellingPrice = 250_000, lowStockThreshold = 3, createdAt = now, updatedAt = now),
                ),
            )

            seedTransactions(transactionDao, now)
            seedBrands(brandDao, now)
            seedCategories(categoryDao, now)
        }

        private suspend fun seedBrands(brandDao: BrandDao, now: Long) {
            brandDao.insertAll(
                listOf(
                    BrandEntity(name = "Coil Master", createdAt = now),
                    BrandEntity(name = "Geekvape", createdAt = now),
                    BrandEntity(name = "Generic", createdAt = now),
                    BrandEntity(name = "Juice Bros", createdAt = now),
                    BrandEntity(name = "Lost Vape", createdAt = now),
                    BrandEntity(name = "Molicel", createdAt = now),
                    BrandEntity(name = "Naked 100", createdAt = now),
                    BrandEntity(name = "Nitecore", createdAt = now),
                    BrandEntity(name = "Saltnic Lab", createdAt = now),
                    BrandEntity(name = "Samsung", createdAt = now),
                    BrandEntity(name = "SMOK", createdAt = now),
                    BrandEntity(name = "Sony", createdAt = now),
                    BrandEntity(name = "Uwell", createdAt = now),
                    BrandEntity(name = "Vaporesso", createdAt = now),
                ),
            )
        }

        private suspend fun seedCategories(categoryDao: CategoryDao, now: Long) {
            categoryDao.insertAll(
                listOf(
                    CategoryEntity(name = "Aksesori", createdAt = now),
                    CategoryEntity(name = "Baterai", createdAt = now),
                    CategoryEntity(name = "Coil", createdAt = now),
                    CategoryEntity(name = "Device", createdAt = now),
                    CategoryEntity(name = "Liquid", createdAt = now),
                ),
            )
        }

        private suspend fun seedTransactions(transactionDao: TransactionDao, now: Long) {
            // ts(daysAgo, hour, minute) → absolute millis timestamp
            fun ts(daysAgo: Int, hour: Int = 10, minute: Int = 0): Long =
                now - (daysAgo.toLong() * 86_400_000L) + (hour.toLong() * 3_600_000L) + (minute.toLong() * 60_000L)

            // userId: owner=1, admin=2, kasir=3 (insertion order above)
            transactionDao.insertAll(
                listOf(
                    // ── 30 days ago: initial restock by owner (MANUAL IN) ──────────────────
                    TransactionEntity(sku = "DEV-001", type = "IN",  source = "MANUAL", quantity = 10, costPriceSnapshot = 280_000, sellingPriceSnapshot = 380_000, userId = 1, createdAt = ts(30, 9,  0),  notes = "Restock awal"),
                    TransactionEntity(sku = "LIQ-001", type = "IN",  source = "MANUAL", quantity = 30, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 1, createdAt = ts(30, 9, 15),  notes = "Restock awal"),
                    TransactionEntity(sku = "COI-001", type = "IN",  source = "MANUAL", quantity = 20, costPriceSnapshot =  35_000, sellingPriceSnapshot =  55_000, userId = 1, createdAt = ts(30, 9, 30),  notes = "Restock awal"),
                    TransactionEntity(sku = "BAT-001", type = "IN",  source = "MANUAL", quantity = 50, costPriceSnapshot =  55_000, sellingPriceSnapshot =  80_000, userId = 1, createdAt = ts(30, 9, 45),  notes = "Restock awal"),

                    // ── 27 days ago: admin restocks more categories (MANUAL IN) ────────────
                    TransactionEntity(sku = "LIQ-002", type = "IN",  source = "MANUAL", quantity = 25, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 2, createdAt = ts(27, 10,  0)),
                    TransactionEntity(sku = "COI-002", type = "IN",  source = "MANUAL", quantity = 25, costPriceSnapshot =  30_000, sellingPriceSnapshot =  50_000, userId = 2, createdAt = ts(27, 10, 20)),
                    TransactionEntity(sku = "BAT-002", type = "IN",  source = "MANUAL", quantity = 20, costPriceSnapshot =  65_000, sellingPriceSnapshot =  95_000, userId = 2, createdAt = ts(27, 10, 40)),
                    TransactionEntity(sku = "AKS-001", type = "IN",  source = "MANUAL", quantity = 25, costPriceSnapshot =  15_000, sellingPriceSnapshot =  30_000, userId = 2, createdAt = ts(27, 11,  0)),

                    // ── 25 days ago: first sales (SCAN OUT by kasir) ──────────────────────
                    TransactionEntity(sku = "DEV-001", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot = 280_000, sellingPriceSnapshot = 380_000, userId = 3, createdAt = ts(25, 14,  0)),
                    TransactionEntity(sku = "LIQ-001", type = "OUT", source = "SCAN",   quantity =  4, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 3, createdAt = ts(25, 14, 30)),
                    TransactionEntity(sku = "COI-001", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  35_000, sellingPriceSnapshot =  55_000, userId = 3, createdAt = ts(25, 15,  0)),
                    TransactionEntity(sku = "BAT-001", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  55_000, sellingPriceSnapshot =  80_000, userId = 3, createdAt = ts(25, 16,  0)),

                    // ── 20 days ago: admin scans in new devices, kasir sells ───────────────
                    TransactionEntity(sku = "DEV-002", type = "IN",  source = "SCAN",   quantity = 10, costPriceSnapshot = 320_000, sellingPriceSnapshot = 430_000, userId = 2, createdAt = ts(20,  9,  0)),
                    TransactionEntity(sku = "DEV-003", type = "IN",  source = "SCAN",   quantity =  8, costPriceSnapshot = 260_000, sellingPriceSnapshot = 360_000, userId = 2, createdAt = ts(20,  9, 20)),
                    TransactionEntity(sku = "DEV-004", type = "IN",  source = "SCAN",   quantity =  8, costPriceSnapshot = 650_000, sellingPriceSnapshot = 850_000, userId = 2, createdAt = ts(20,  9, 40)),
                    TransactionEntity(sku = "LIQ-001", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 3, createdAt = ts(20, 14,  0)),
                    TransactionEntity(sku = "LIQ-002", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 3, createdAt = ts(20, 14, 20)),
                    TransactionEntity(sku = "AKS-001", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  15_000, sellingPriceSnapshot =  30_000, userId = 3, createdAt = ts(20, 15,  0)),

                    // ── 15 days ago: owner adds new liquid lines, kasir sells devices ──────
                    TransactionEntity(sku = "LIQ-003", type = "IN",  source = "MANUAL", quantity = 15, costPriceSnapshot =  80_000, sellingPriceSnapshot = 120_000, userId = 1, createdAt = ts(15,  8, 30),  notes = "Stok baru"),
                    TransactionEntity(sku = "LIQ-004", type = "IN",  source = "MANUAL", quantity = 10, costPriceSnapshot =  80_000, sellingPriceSnapshot = 120_000, userId = 1, createdAt = ts(15,  8, 45),  notes = "Stok baru"),
                    TransactionEntity(sku = "LIQ-005", type = "IN",  source = "MANUAL", quantity =  8, costPriceSnapshot = 110_000, sellingPriceSnapshot = 160_000, userId = 1, createdAt = ts(15,  9,  0),  notes = "Stok baru"),
                    TransactionEntity(sku = "DEV-001", type = "OUT", source = "SCAN",   quantity =  1, costPriceSnapshot = 280_000, sellingPriceSnapshot = 380_000, userId = 3, createdAt = ts(15, 14, 10)),
                    TransactionEntity(sku = "DEV-002", type = "OUT", source = "SCAN",   quantity =  1, costPriceSnapshot = 320_000, sellingPriceSnapshot = 430_000, userId = 3, createdAt = ts(15, 14, 30)),
                    TransactionEntity(sku = "COI-001", type = "OUT", source = "SCAN",   quantity =  4, costPriceSnapshot =  35_000, sellingPriceSnapshot =  55_000, userId = 3, createdAt = ts(15, 15, 20)),

                    // ── 12 days ago: admin scans in coil & battery, kasir sells ───────────
                    TransactionEntity(sku = "COI-003", type = "IN",  source = "SCAN",   quantity = 10, costPriceSnapshot =  40_000, sellingPriceSnapshot =  65_000, userId = 2, createdAt = ts(12, 10,  0)),
                    TransactionEntity(sku = "BAT-003", type = "IN",  source = "SCAN",   quantity =  5, costPriceSnapshot =  90_000, sellingPriceSnapshot = 130_000, userId = 2, createdAt = ts(12, 10, 30)),
                    TransactionEntity(sku = "BAT-001", type = "OUT", source = "SCAN",   quantity =  4, costPriceSnapshot =  55_000, sellingPriceSnapshot =  80_000, userId = 3, createdAt = ts(12, 14,  0)),
                    TransactionEntity(sku = "COI-002", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  30_000, sellingPriceSnapshot =  50_000, userId = 3, createdAt = ts(12, 15,  0)),

                    // ── 9 days ago: admin scans in accessories, kasir sells liquids ────────
                    TransactionEntity(sku = "AKS-002", type = "IN",  source = "SCAN",   quantity = 10, costPriceSnapshot =  50_000, sellingPriceSnapshot =  80_000, userId = 2, createdAt = ts( 9, 10,  0)),
                    TransactionEntity(sku = "AKS-003", type = "IN",  source = "SCAN",   quantity = 40, costPriceSnapshot =  18_000, sellingPriceSnapshot =  30_000, userId = 2, createdAt = ts( 9, 10, 15)),
                    TransactionEntity(sku = "AKS-004", type = "IN",  source = "SCAN",   quantity =  6, costPriceSnapshot = 180_000, sellingPriceSnapshot = 250_000, userId = 2, createdAt = ts( 9, 10, 30)),
                    TransactionEntity(sku = "LIQ-003", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  80_000, sellingPriceSnapshot = 120_000, userId = 3, createdAt = ts( 9, 14,  0)),
                    TransactionEntity(sku = "LIQ-004", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  80_000, sellingPriceSnapshot = 120_000, userId = 3, createdAt = ts( 9, 14, 30)),
                    TransactionEntity(sku = "LIQ-005", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot = 110_000, sellingPriceSnapshot = 160_000, userId = 3, createdAt = ts( 9, 15,  0)),

                    // ── 6 days ago: admin restocks DEV-005, kasir sells devices ───────────
                    TransactionEntity(sku = "DEV-005", type = "IN",  source = "SCAN",   quantity =  5, costPriceSnapshot = 310_000, sellingPriceSnapshot = 420_000, userId = 2, createdAt = ts( 6,  9,  0),  notes = "Restock DEV-005"),
                    TransactionEntity(sku = "DEV-001", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot = 280_000, sellingPriceSnapshot = 380_000, userId = 3, createdAt = ts( 6, 13,  0)),
                    TransactionEntity(sku = "DEV-005", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot = 310_000, sellingPriceSnapshot = 420_000, userId = 3, createdAt = ts( 6, 14,  0)),
                    TransactionEntity(sku = "AKS-001", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  15_000, sellingPriceSnapshot =  30_000, userId = 3, createdAt = ts( 6, 15, 30)),

                    // ── 4 days ago: admin reduces BAT-003 (retur cacat), kasir sells ───────
                    TransactionEntity(sku = "BAT-001", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  55_000, sellingPriceSnapshot =  80_000, userId = 3, createdAt = ts( 4, 11,  0)),
                    TransactionEntity(sku = "COI-003", type = "OUT", source = "SCAN",   quantity =  5, costPriceSnapshot =  40_000, sellingPriceSnapshot =  65_000, userId = 3, createdAt = ts( 4, 14,  0)),
                    TransactionEntity(sku = "AKS-004", type = "OUT", source = "SCAN",   quantity =  1, costPriceSnapshot = 180_000, sellingPriceSnapshot = 250_000, userId = 3, createdAt = ts( 4, 15,  0)),
                    TransactionEntity(sku = "BAT-003", type = "OUT", source = "MANUAL", quantity =  1, costPriceSnapshot =  90_000, sellingPriceSnapshot = 130_000, userId = 2, createdAt = ts( 4, 16,  0),  notes = "Retur cacat"),

                    // ── 2 days ago: owner restocks devices & liquid, kasir sells ──────────
                    TransactionEntity(sku = "DEV-001", type = "IN",  source = "MANUAL", quantity =  5, costPriceSnapshot = 280_000, sellingPriceSnapshot = 380_000, userId = 1, createdAt = ts( 2,  9,  0),  notes = "Restock"),
                    TransactionEntity(sku = "LIQ-001", type = "IN",  source = "MANUAL", quantity =  8, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 1, createdAt = ts( 2,  9, 20),  notes = "Restock"),
                    TransactionEntity(sku = "DEV-002", type = "OUT", source = "SCAN",   quantity =  1, costPriceSnapshot = 320_000, sellingPriceSnapshot = 430_000, userId = 3, createdAt = ts( 2, 14,  0)),
                    TransactionEntity(sku = "LIQ-001", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 3, createdAt = ts( 2, 14, 30)),
                    TransactionEntity(sku = "LIQ-002", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  45_000, sellingPriceSnapshot =  70_000, userId = 3, createdAt = ts( 2, 15,  0)),

                    // ── yesterday: mix of sales ───────────────────────────────────────────
                    TransactionEntity(sku = "DEV-003", type = "OUT", source = "SCAN",   quantity =  1, costPriceSnapshot = 260_000, sellingPriceSnapshot = 360_000, userId = 3, createdAt = ts( 1, 10,  0)),
                    TransactionEntity(sku = "LIQ-005", type = "OUT", source = "SCAN",   quantity =  1, costPriceSnapshot = 110_000, sellingPriceSnapshot = 160_000, userId = 3, createdAt = ts( 1, 11, 30)),
                    TransactionEntity(sku = "COI-001", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  35_000, sellingPriceSnapshot =  55_000, userId = 3, createdAt = ts( 1, 14,  0)),
                    TransactionEntity(sku = "BAT-002", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  65_000, sellingPriceSnapshot =  95_000, userId = 3, createdAt = ts( 1, 15,  0)),
                    TransactionEntity(sku = "AKS-003", type = "OUT", source = "SCAN",   quantity =  4, costPriceSnapshot =  18_000, sellingPriceSnapshot =  30_000, userId = 3, createdAt = ts( 1, 16,  0)),

                    // ── today ─────────────────────────────────────────────────────────────
                    TransactionEntity(sku = "COI-002", type = "OUT", source = "SCAN",   quantity =  2, costPriceSnapshot =  30_000, sellingPriceSnapshot =  50_000, userId = 3, createdAt = ts( 0,  9,  0)),
                    TransactionEntity(sku = "BAT-001", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  55_000, sellingPriceSnapshot =  80_000, userId = 3, createdAt = ts( 0, 10,  0)),
                    TransactionEntity(sku = "AKS-003", type = "OUT", source = "SCAN",   quantity =  3, costPriceSnapshot =  18_000, sellingPriceSnapshot =  30_000, userId = 3, createdAt = ts( 0, 11,  0)),
                ),
            )
        }
    }
}
