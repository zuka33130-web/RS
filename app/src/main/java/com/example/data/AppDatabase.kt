package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ClientDao
import com.example.data.dao.FinancialTransactionDao
import com.example.data.dao.SupplyItemDao
import com.example.data.dao.WorkOrderDao
import com.example.data.dao.WorkshopTaskDao
import com.example.data.model.ClientEntity
import com.example.data.model.FinancialTransaction
import com.example.data.model.SupplyItem
import com.example.data.model.TaskPriority
import com.example.data.model.TransactionType
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkshopTask
import androidx.room.TypeConverters
import com.example.data.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FinancialTransaction::class,
        WorkshopTask::class,
        SupplyItem::class,
        ClientEntity::class,
        WorkOrderEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun workshopTaskDao(): WorkshopTaskDao
    abstract fun supplyItemDao(): SupplyItemDao
    abstract fun clientDao(): ClientDao
    abstract fun workOrderDao(): WorkOrderDao

    companion object {
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Las versiones anteriores podían crear más de un cobro para una orden.
                db.execSQL(
                    """DELETE FROM financial_transactions
                       WHERE relatedOrderId IS NOT NULL AND id NOT IN (
                           SELECT MIN(id) FROM financial_transactions
                           WHERE relatedOrderId IS NOT NULL GROUP BY relatedOrderId
                       )""".trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_financial_transactions_relatedOrderId " +
                        "ON financial_transactions(relatedOrderId)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "retro_state_rs_database"
                )
                    .addMigrations(MIGRATION_5_6)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedRetroData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.workOrderDao().getOrderCount() == 0) {
                            seedRetroData(database)
                        }
                    }
                }
            }
        }

        suspend fun seedRetroData(database: AppDatabase) = database.withTransaction {
            val transactionDao = database.financialTransactionDao()
            val taskDao = database.workshopTaskDao()
            val supplyDao = database.supplyItemDao()
            val clientDao = database.clientDao()
            val workOrderDao = database.workOrderDao()

            if (clientDao.getClientCount() == 0) {
                val c1 = ClientEntity(firstName = "Carlos", lastName = "Giménez", address = "Av. Rivadavia 4521, CABA", phone = "+54 9 11 4521-8899", notes = "Coleccionista SNES/Sega")
                val c2 = ClientEntity(firstName = "Lucas", lastName = "Rossi", address = "Calle Corrientes 1290, Lanús", phone = "+54 9 11 6321-4411", notes = "Speedrunner Sega Genesis")
                val c3 = ClientEntity(firstName = "Martín", lastName = "Vega", address = "Belgrano 884, Quilmes", phone = "+54 9 11 3899-7722", notes = "Torneos PS1 y RPGs")
                val c4 = ClientEntity(firstName = "Valeria", lastName = "Morales", address = "Juramento 2100, CABA", phone = "+54 9 11 5012-9933", notes = "Exclusivas Nintendo 64")
                val id1 = clientDao.insertClient(c1)
                val id2 = clientDao.insertClient(c2)
                val id3 = clientDao.insertClient(c3)
                val id4 = clientDao.insertClient(c4)

                val now = System.currentTimeMillis()
                val day = 86400000L

                val sampleWorkOrders = listOf(
                    WorkOrderEntity(
                        clientId = id1,
                        orderCode = "RS-2026-0041",
                        deviceModel = "Super Nintendo (SNES)",
                        serialNumber = "UN18290192",
                        reportedFault = "Luz roja enciende pero no da video ni audio en TV CRT.",
                        technicalDiagnosis = "Capacitores SMD de video y audio sulfatados (C59, C60). Requiere recap completo Nichicon.",
                        internalNotes = "Cliente solicitó conservar chasis original sin blanquear.",
                        status = WorkOrderStatus.TO_CHECK.name,
                        partsCost = 15000.0,
                        laborCost = 20000.0,
                        totalCost = 35000.0,
                        entryDate = now - day * 2
                    ),
                    WorkOrderEntity(
                        clientId = id2,
                        orderCode = "RS-2026-0042",
                        deviceModel = "Sega Genesis Model 1",
                        serialNumber = "1601-092144",
                        reportedFault = "Distorsión en audio sintetizador Yamaha FM y regulador hirviendo.",
                        technicalDiagnosis = "Falla regulador 7805 secundario y pista desoldada en DIN-8.",
                        internalNotes = "Presupuesto aprobado. Esperando repuestos reguladores.",
                        status = WorkOrderStatus.WAITING_PARTS.name,
                        partsCost = 8000.0,
                        laborCost = 20000.0,
                        totalCost = 28000.0,
                        entryDate = now - day * 3,
                        quotedDate = now - day * 2
                    ),
                    WorkOrderEntity(
                        clientId = id3,
                        orderCode = "RS-2026-0043",
                        deviceModel = "Sony PlayStation 1 (PSOne SCPH-5501)",
                        serialNumber = "SCPH-5501-A42",
                        reportedFault = "No lee CDs. Engranajes hacen ruido 'click-click'.",
                        technicalDiagnosis = "Lente óptico KSM-440ACM descalibrado. Instalado lector láser nuevo y calibrado en banco.",
                        internalNotes = "Testeado durante 4 horas continuas con Gran Turismo y Silent Hill. 100% OK.",
                        status = WorkOrderStatus.REPAIRED.name,
                        partsCost = 22000.0,
                        laborCost = 20000.0,
                        totalCost = 42000.0,
                        entryDate = now - day * 4,
                        quotedDate = now - day * 3
                    ),
                    WorkOrderEntity(
                        clientId = id4,
                        orderCode = "RS-2026-0044",
                        deviceModel = "Nintendo 64 (Jungle Green)",
                        serialNumber = "NS10492819",
                        reportedFault = "Instalar Mod RGB Tim Worthington y stick metálico.",
                        technicalDiagnosis = "Instalado Mod RGB con salida C-Sync y reemplazado mecanismo del joystick.",
                        internalNotes = "Entregado a la cliente con cable SCART RGB. Pago total recibido.",
                        status = WorkOrderStatus.DELIVERED.name,
                        partsCost = 35000.0,
                        laborCost = 30000.0,
                        totalCost = 65000.0,
                        entryDate = now - day * 10,
                        deliveryDate = now - day * 2,
                        warrantyDays = 90
                    ),
                    WorkOrderEntity(
                        clientId = id1,
                        orderCode = "RS-2026-0038",
                        deviceModel = "Game Boy Color (Atomic Purple)",
                        serialNumber = "CG-882194",
                        reportedFault = "Pila salvapartidas agotada en Pokémon Silver.",
                        technicalDiagnosis = "Reemplazada batería por zócalo extraplano + pila CR1616 nueva Maxell.",
                        internalNotes = "En período de garantía activa (90 días).",
                        status = WorkOrderStatus.UNDER_WARRANTY.name,
                        partsCost = 4000.0,
                        laborCost = 8000.0,
                        totalCost = 12000.0,
                        entryDate = now - day * 25,
                        deliveryDate = now - day * 15,
                        warrantyDays = 90
                    ),
                    WorkOrderEntity(
                        clientId = id2,
                        orderCode = "RS-2026-0045",
                        deviceModel = "Game Boy Advance SP",
                        serialNumber = "AGS-001-21",
                        reportedFault = "Pantalla frontal con polvo interno y botón R trabado.",
                        technicalDiagnosis = "Presupuestada instalación de pantalla IPS V5 y reemplazo de microswitch táctil.",
                        internalNotes = "Presupuesto enviado por WhatsApp. Esperando respuesta.",
                        status = WorkOrderStatus.QUOTED.name,
                        partsCost = 45000.0,
                        laborCost = 25000.0,
                        totalCost = 70000.0,
                        entryDate = now - day * 1,
                        quotedDate = now - day * 1
                    )
                )
                workOrderDao.insertOrders(sampleWorkOrders)
            }

            if (transactionDao.getTransactionCount() == 0) {
                val now = System.currentTimeMillis()
                val day = 86400000L
                val sampleTx = listOf(
                    FinancialTransaction(
                        type = TransactionType.INCOME.name,
                        amount = 42000.0,
                        category = "Reparación Retro",
                        description = "Cambio lector láser PS1 - Cliente Diego A.",
                        date = now - day * 1
                    ),
                    FinancialTransaction(
                        type = TransactionType.INCOME.name,
                        amount = 195000.0,
                        category = "Venta Consola Clásica",
                        description = "Venta Game Boy Advance SP IPS - Coleccionista Marcos",
                        date = now - day * 2
                    ),
                    FinancialTransaction(
                        type = TransactionType.EXPENSE.name,
                        amount = 32000.0,
                        category = "Capacitores & ICs",
                        description = "Pack 50x Capacitores Nichicon Audio + 10x Reguladores 7805",
                        date = now - day * 3
                    ),
                    FinancialTransaction(
                        type = TransactionType.EXPENSE.name,
                        amount = 26500.0,
                        category = "Lentes Ópticos",
                        description = "4x Unidades láser KSM-440 para PlayStation 1",
                        date = now - day * 4
                    ),
                    FinancialTransaction(
                        type = TransactionType.INCOME.name,
                        amount = 65000.0,
                        category = "Modding Retro",
                        description = "Instalación RGB y HDMI N64 - Cliente Valeria",
                        date = now - day * 5
                    ),
                    FinancialTransaction(
                        type = TransactionType.EXPENSE.name,
                        amount = 18000.0,
                        category = "Insumos Taller",
                        description = "Alcohol isopropílico 99% + Flux Amtech + Malla desoldante",
                        date = now - day * 6
                    )
                )
                transactionDao.insertTransactions(sampleTx)
            }

            if (taskDao.getTaskCount() == 0) {
                val sampleTasks = listOf(
                    WorkshopTask(
                        title = "Recap total de capacitores de audio a SNES de Carlos",
                        priority = TaskPriority.HIGH.name,
                        isCompleted = false,
                        relatedConsole = "Super Nintendo"
                    ),
                    WorkshopTask(
                        title = "Cambiar regulador 7805 y calibrar audio Sega Genesis de Lucas",
                        priority = TaskPriority.HIGH.name,
                        isCompleted = false,
                        relatedConsole = "Sega Genesis"
                    ),
                    WorkshopTask(
                        title = "Instalar batería CR2032 con zócalo en cartucho Pokémon Gold",
                        priority = TaskPriority.MEDIUM.name,
                        isCompleted = false,
                        relatedConsole = "Game Boy Color"
                    ),
                    WorkshopTask(
                        title = "Limpieza con ultrasonido y lubricación slot cartuchos N64",
                        priority = TaskPriority.LOW.name,
                        isCompleted = true,
                        relatedConsole = "Nintendo 64"
                    )
                )
                taskDao.insertTasks(sampleTasks)
            }

            if (supplyDao.getSupplyCount() == 0) {
                val sampleSupplies = listOf(
                    SupplyItem(
                        name = "Capacitores Electrolíticos Nichicon para SNES (Kits)",
                        category = "Repuestos",
                        quantity = 6,
                        unit = "kits",
                        isNeeded = true,
                        estimatedPrice = 18000.0,
                        supplier = "Console5 USA / Importador"
                    ),
                    SupplyItem(
                        name = "Lentes ópticos KSM-440BAM para PS1",
                        category = "Repuestos",
                        quantity = 3,
                        unit = "unidades",
                        isNeeded = true,
                        estimatedPrice = 34000.0,
                        supplier = "Retro Parts BA"
                    ),
                    SupplyItem(
                        name = "Baterías botón CR2032 con pines para soldar",
                        category = "Consumibles",
                        quantity = 20,
                        unit = "unidades",
                        isNeeded = true,
                        estimatedPrice = 12000.0,
                        supplier = "Electrónica Liniers"
                    ),
                    SupplyItem(
                        name = "Reguladores de voltaje L7805CV (TO-220)",
                        category = "Repuestos",
                        quantity = 10,
                        unit = "unidades",
                        isNeeded = false,
                        estimatedPrice = 6500.0,
                        supplier = "Microelectrónica"
                    ),
                    SupplyItem(
                        name = "Alcohol Isopropílico 99.9% de alta pureza (5 Litros)",
                        category = "Químicos",
                        quantity = 1,
                        unit = "bidón",
                        isNeeded = false,
                        estimatedPrice = 24000.0,
                        supplier = "Química Central"
                    )
                )
                supplyDao.insertSupplies(sampleSupplies)
            }
        }
    }
}
