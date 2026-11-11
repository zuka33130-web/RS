package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.FinancialTransaction
import com.example.data.model.SupplyItem
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkshopTask
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupStats(
    val ordersCount: Int,
    val transactionsCount: Int,
    val tasksCount: Int,
    val suppliesCount: Int,
    val timestamp: Long
)

object BackupManager {

    private const val APP_IDENTIFIER = "Retro State RS"
    private const val BACKUP_VERSION = 1

    /**
     * Serializa toda la base de datos a una cadena JSON estructurada.
     */
    suspend fun generateBackupJson(repository: WorkshopRepository): String = withContext(Dispatchers.IO) {
        val orders = repository.getAllOrdersSync()
        val transactions = repository.getAllTransactionsSync()
        val tasks = repository.getAllTasksSync()
        val supplies = repository.getAllSuppliesSync()

        val rootJson = JSONObject().apply {
            put("app", APP_IDENTIFIER)
            put("version", BACKUP_VERSION)
            put("timestamp", System.currentTimeMillis())

            // Orders
            val ordersArray = JSONArray()
            orders.forEach { o ->
                val obj = JSONObject().apply {
                    put("id", o.id)
                    put("clientId", o.clientId)
                    put("orderCode", o.orderCode)
                    put("deviceModel", o.deviceModel)
                    put("serialNumber", o.serialNumber)
                    put("reportedFault", o.reportedFault)
                    put("technicalDiagnosis", o.technicalDiagnosis)
                    put("internalNotes", o.internalNotes)
                    put("status", o.status)
                    put("partsCost", o.partsCost)
                    put("laborCost", o.laborCost)
                    put("totalCost", o.totalCost)
                    put("entryDate", o.entryDate)
                    if (o.quotedDate != null) put("quotedDate", o.quotedDate)
                    if (o.deliveryDate != null) put("deliveryDate", o.deliveryDate)
                    put("warrantyDays", o.warrantyDays)
                    put("warrantyTerms", o.warrantyTerms)
                }
                ordersArray.put(obj)
            }
            put("orders", ordersArray)

            // Transactions
            val txArray = JSONArray()
            transactions.forEach { t ->
                val obj = JSONObject().apply {
                    put("id", t.id)
                    put("type", t.type)
                    put("amount", t.amount)
                    put("category", t.category)
                    put("description", t.description)
                    put("date", t.date)
                    if (t.relatedOrderId != null) put("relatedOrderId", t.relatedOrderId)
                }
                txArray.put(obj)
            }
            put("transactions", txArray)

            // Tasks
            val tasksArray = JSONArray()
            tasks.forEach { tk ->
                val obj = JSONObject().apply {
                    put("id", tk.id)
                    put("title", tk.title)
                    put("priority", tk.priority)
                    put("isCompleted", tk.isCompleted)
                    put("relatedConsole", tk.relatedConsole)
                    put("createdAt", tk.createdAt)
                }
                tasksArray.put(obj)
            }
            put("tasks", tasksArray)

            // Supplies
            val suppliesArray = JSONArray()
            supplies.forEach { s ->
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("category", s.category)
                    put("quantity", s.quantity)
                    put("unit", s.unit)
                    put("isNeeded", s.isNeeded)
                    put("estimatedPrice", s.estimatedPrice)
                    put("supplier", s.supplier)
                }
                suppliesArray.put(obj)
            }
            put("supplies", suppliesArray)
        }

        rootJson.toString(2)
    }

    /**
     * Escribe el backup en un URI seleccionado por el usuario mediante SAF (CreateDocument).
     */
    suspend fun exportBackupToUri(
        context: Context,
        repository: WorkshopRepository,
        destinationUri: Uri
    ): Result<BackupStats> = withContext(Dispatchers.IO) {
        try {
            val jsonString = generateBackupJson(repository)
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            } ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo de destino para escritura."))

            val orders = repository.getAllOrdersSync().size
            val transactions = repository.getAllTransactionsSync().size
            val tasks = repository.getAllTasksSync().size
            val supplies = repository.getAllSuppliesSync().size

            Result.success(
                BackupStats(
                    ordersCount = orders,
                    transactionsCount = transactions,
                    tasksCount = tasks,
                    suppliesCount = supplies,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lee un archivo JSON seleccionado por el usuario mediante SAF (OpenDocument) y restaura la BD.
     */
    suspend fun importBackupFromUri(
        context: Context,
        repository: WorkshopRepository,
        sourceUri: Uri
    ): Result<BackupStats> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo para lectura."))

            val rootJson = JSONObject(content)

            val app = rootJson.optString("app", "")
            if (!app.contains("Retro State RS") && !app.contains("ConsoleLab") && !rootJson.has("orders")) {
                return@withContext Result.failure(Exception("El archivo seleccionado no es una copia de seguridad válida."))
            }

            // Parse Orders
            val ordersList = mutableListOf<WorkOrderEntity>()
            val ordersArray = rootJson.optJSONArray("orders") ?: JSONArray()
            for (i in 0 until ordersArray.length()) {
                val obj = ordersArray.getJSONObject(i)
                ordersList.add(
                    WorkOrderEntity(
                        id = obj.optLong("id", 0L),
                        clientId = obj.optLong("clientId", 1L),
                        orderCode = obj.optString("orderCode", "RS-2026-0001"),
                        deviceModel = obj.optString("deviceModel", ""),
                        serialNumber = obj.optString("serialNumber", ""),
                        reportedFault = obj.optString("reportedFault", ""),
                        technicalDiagnosis = obj.optString("technicalDiagnosis", obj.optString("diagnosis", "")),
                        internalNotes = obj.optString("internalNotes", obj.optString("notes", "")),
                        status = obj.optString("status", "TO_CHECK"),
                        partsCost = obj.optDouble("partsCost", obj.optDouble("estimatedCost", 0.0)),
                        laborCost = obj.optDouble("laborCost", 0.0),
                        totalCost = obj.optDouble("totalCost", obj.optDouble("finalPrice", 0.0)),
                        entryDate = obj.optLong("entryDate", System.currentTimeMillis()),
                        quotedDate = if (obj.has("quotedDate") && !obj.isNull("quotedDate")) obj.optLong("quotedDate") else null,
                        deliveryDate = if (obj.has("deliveryDate") && !obj.isNull("deliveryDate")) obj.optLong("deliveryDate") else if (obj.has("exitDate") && !obj.isNull("exitDate")) obj.optLong("exitDate") else null,
                        warrantyDays = obj.optInt("warrantyDays", 90)
                    )
                )
            }

            // Parse Transactions
            val transactionsList = mutableListOf<FinancialTransaction>()
            val txArray = rootJson.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                transactionsList.add(
                    FinancialTransaction(
                        id = obj.optLong("id", 0L),
                        type = obj.optString("type", "INCOME"),
                        amount = obj.optDouble("amount", 0.0),
                        category = obj.optString("category", "Otros"),
                        description = obj.optString("description", ""),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        relatedOrderId = if (obj.has("relatedOrderId") && !obj.isNull("relatedOrderId")) obj.optLong("relatedOrderId") else null
                    )
                )
            }

            // Parse Tasks
            val tasksList = mutableListOf<WorkshopTask>()
            val tasksArray = rootJson.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tasksArray.length()) {
                val obj = tasksArray.getJSONObject(i)
                tasksList.add(
                    WorkshopTask(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", ""),
                        priority = obj.optString("priority", "MEDIUM"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        relatedConsole = obj.optString("relatedConsole", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Supplies
            val suppliesList = mutableListOf<SupplyItem>()
            val suppliesArray = rootJson.optJSONArray("supplies") ?: JSONArray()
            for (i in 0 until suppliesArray.length()) {
                val obj = suppliesArray.getJSONObject(i)
                suppliesList.add(
                    SupplyItem(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", ""),
                        category = obj.optString("category", "Repuestos"),
                        quantity = obj.optInt("quantity", 1),
                        unit = obj.optString("unit", "u"),
                        isNeeded = obj.optBoolean("isNeeded", true),
                        estimatedPrice = obj.optDouble("estimatedPrice", 0.0),
                        supplier = obj.optString("supplier", "")
                    )
                )
            }

            repository.restoreAllData(ordersList, transactionsList, tasksList, suppliesList)

            Result.success(
                BackupStats(
                    ordersCount = ordersList.size,
                    transactionsCount = transactionsList.size,
                    tasksCount = tasksList.size,
                    suppliesCount = suppliesList.size,
                    timestamp = rootJson.optLong("timestamp", System.currentTimeMillis())
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createShareBackupIntent(
        context: Context,
        repository: WorkshopRepository
    ): Intent = withContext(Dispatchers.IO) {
        val jsonString = generateBackupJson(repository)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "RetroStateRS_Backup_${dateFormat.format(Date())}.json"

        val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val backupFile = File(backupDir, fileName)
        backupFile.writeText(jsonString, Charsets.UTF_8)

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Backup Base de Datos - Retro State RS")
            putExtra(
                Intent.EXTRA_TEXT,
                "Adjunto respaldo de datos de Retro State RS (${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())})."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Intent.createChooser(shareIntent, "Respaldar en la Nube (Drive / Correo / WhatsApp)")
    }
}
