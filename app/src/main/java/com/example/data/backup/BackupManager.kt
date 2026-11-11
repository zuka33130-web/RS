package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ClientEntity
import com.example.data.model.FinancialTransaction
import com.example.data.model.SupplyItem
import com.example.data.model.TransactionType
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkshopTask
import com.example.data.repository.WorkshopRepository
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupStats(
    val ordersCount: Int,
    val transactionsCount: Int,
    val tasksCount: Int,
    val suppliesCount: Int,
    val timestamp: Long
)

object BackupManager {

    private const val APP_IDENTIFIER = "Retro State RS"
    private const val BACKUP_VERSION = 2

    /** Serializa clientes, órdenes y el resto de los datos sin perder sus relaciones. */
    suspend fun generateBackupJson(repository: WorkshopRepository): String = withContext(Dispatchers.IO) {
        val clients = repository.getAllClientsSync()
        val orders = repository.getAllOrdersSync()
        val transactions = repository.getAllTransactionsSync()
        val tasks = repository.getAllTasksSync()
        val supplies = repository.getAllSuppliesSync()

        JSONObject().apply {
            put("app", APP_IDENTIFIER)
            put("version", BACKUP_VERSION)
            put("timestamp", System.currentTimeMillis())

            put("clients", JSONArray().apply {
                clients.forEach { client ->
                    put(JSONObject().apply {
                        put("id", client.id)
                        put("firstName", client.firstName)
                        put("lastName", client.lastName)
                        put("address", client.address)
                        put("phone", client.phone)
                        put("notes", client.notes)
                        put("createdAt", client.createdAt)
                    })
                }
            })

            put("orders", JSONArray().apply {
                orders.forEach { order ->
                    put(JSONObject().apply {
                        put("id", order.id)
                        put("clientId", order.clientId)
                        put("orderCode", order.orderCode)
                        put("deviceModel", order.deviceModel)
                        put("serialNumber", order.serialNumber)
                        put("reportedFault", order.reportedFault)
                        put("technicalDiagnosis", order.technicalDiagnosis)
                        put("internalNotes", order.internalNotes)
                        put("status", order.status)
                        put("partsCost", order.partsCost)
                        put("laborCost", order.laborCost)
                        put("totalCost", order.totalCost)
                        put("entryDate", order.entryDate)
                        putNullable("quotedDate", order.quotedDate)
                        putNullable("deliveryDate", order.deliveryDate)
                        put("warrantyDays", order.warrantyDays)
                        put("warrantyTerms", order.warrantyTerms)
                        put("photosUris", JSONArray(order.photosUris))
                        putNullable("signatureUri", order.signatureUri)
                    })
                }
            })

            put("transactions", JSONArray().apply {
                transactions.forEach { transaction ->
                    put(JSONObject().apply {
                        put("id", transaction.id)
                        put("type", transaction.type)
                        put("amount", transaction.amount)
                        put("category", transaction.category)
                        put("description", transaction.description)
                        put("date", transaction.date)
                        putNullable("relatedOrderId", transaction.relatedOrderId)
                    })
                }
            })

            put("tasks", JSONArray().apply {
                tasks.forEach { task ->
                    put(JSONObject().apply {
                        put("id", task.id)
                        put("title", task.title)
                        put("priority", task.priority)
                        put("isCompleted", task.isCompleted)
                        put("relatedConsole", task.relatedConsole)
                        put("createdAt", task.createdAt)
                    })
                }
            })

            put("supplies", JSONArray().apply {
                supplies.forEach { supply ->
                    put(JSONObject().apply {
                        put("id", supply.id)
                        put("name", supply.name)
                        put("category", supply.category)
                        put("quantity", supply.quantity)
                        put("unit", supply.unit)
                        put("isNeeded", supply.isNeeded)
                        put("estimatedPrice", supply.estimatedPrice)
                        put("supplier", supply.supplier)
                    })
                }
            })
        }.toString(2)
    }

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    suspend fun exportBackupToUri(
        context: Context,
        repository: WorkshopRepository,
        destinationUri: Uri
    ): Result<BackupStats> = withContext(Dispatchers.IO) {
        runCatching {
            val clients = repository.getAllClientsSync()
            val orders = repository.getAllOrdersSync()
            val transactions = repository.getAllTransactionsSync()
            val tasks = repository.getAllTasksSync()
            val supplies = repository.getAllSuppliesSync()
            val jsonString = generateBackupJson(repository)

            // "wt" trunca el destino incluso en proveedores SAF que no lo hacen con "w".
            context.contentResolver.openOutputStream(destinationUri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            } ?: error("No se pudo abrir el archivo de destino para escritura.")

            BackupStats(
                ordersCount = orders.size,
                transactionsCount = transactions.size,
                tasksCount = tasks.size,
                suppliesCount = supplies.size,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    suspend fun importBackupFromUri(
        context: Context,
        repository: WorkshopRepository,
        sourceUri: Uri
    ): Result<BackupStats> = withContext(Dispatchers.IO) {
        runCatching {
            val content = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader -> reader.readText() }
            } ?: error("No se pudo abrir el archivo para lectura.")

            val root = JSONObject(content)
            require(root.optString("app") in setOf(APP_IDENTIFIER, "ConsoleLab")) {
                "El archivo seleccionado no es una copia de seguridad de Retro State RS."
            }
            require(root.optInt("version", 1) in 1..BACKUP_VERSION) {
                "La versión de esta copia de seguridad no es compatible."
            }
            require(root.has("orders")) { "La copia no contiene la sección de órdenes." }

            val orders = parseOrders(root.optJSONArray("orders") ?: JSONArray())
            val clients = parseClients(root.optJSONArray("clients"), orders)
            val transactions = parseTransactions(root.optJSONArray("transactions") ?: JSONArray())
            val tasks = parseTasks(root.optJSONArray("tasks") ?: JSONArray())
            val supplies = parseSupplies(root.optJSONArray("supplies") ?: JSONArray())

            repository.restoreAllData(clients, orders, transactions, tasks, supplies)

            BackupStats(
                ordersCount = orders.size,
                transactionsCount = transactions.size,
                tasksCount = tasks.size,
                suppliesCount = supplies.size,
                timestamp = root.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }

    private fun parseClients(ordersArray: JSONArray?, orders: List<WorkOrderEntity>): List<ClientEntity> {
        val clients = mutableListOf<ClientEntity>()
        if (ordersArray != null) {
            for (index in 0 until ordersArray.length()) {
                val json = ordersArray.getJSONObject(index)
                val id = json.getLong("id")
                require(id > 0) { "Hay un cliente con un identificador inválido." }
                clients += ClientEntity(
                    id = id,
                    firstName = json.optString("firstName", "Cliente restaurado"),
                    lastName = json.optString("lastName"),
                    address = json.optString("address"),
                    phone = json.optString("phone"),
                    notes = json.optString("notes"),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis())
                )
            }
        }

        // Las copias v1 no incluían clientes. Se crean marcadores válidos para no violar las FK.
        val existingIds = clients.mapTo(mutableSetOf()) { it.id }
        orders.map { it.clientId }.distinct().filter { it !in existingIds }.forEach { clientId ->
            clients += ClientEntity(
                id = clientId,
                firstName = "Cliente restaurado",
                lastName = "#$clientId",
                notes = "Complete los datos de este cliente: la copia original no los incluía."
            )
        }
        return clients
    }

    private fun parseOrders(array: JSONArray): List<WorkOrderEntity> = buildList {
        for (index in 0 until array.length()) {
            val json = array.getJSONObject(index)
            val id = json.getLong("id")
            val clientId = json.getLong("clientId")
            val orderCode = json.getString("orderCode").trim()
            val status = json.optString("status", WorkOrderStatus.TO_CHECK.name)
            val partsCost = json.optDouble("partsCost", json.optDouble("estimatedCost", 0.0))
            val laborCost = json.optDouble("laborCost", 0.0)
            val totalCost = json.optDouble("totalCost", json.optDouble("finalPrice", partsCost + laborCost))
            require(id > 0 && clientId > 0 && orderCode.isNotEmpty()) { "Hay una orden incompleta o inválida." }
            require(WorkOrderStatus.entries.any { it.name == status }) { "Estado de orden desconocido: $status" }
            require(partsCost.isFinite() && laborCost.isFinite() && totalCost.isFinite()) { "Hay costos inválidos." }
            require(partsCost >= 0.0 && laborCost >= 0.0 && totalCost >= 0.0) { "Los costos no pueden ser negativos." }
            add(
                WorkOrderEntity(
                    id = id,
                    clientId = clientId,
                    orderCode = orderCode,
                    deviceModel = json.optString("deviceModel"),
                    serialNumber = json.optString("serialNumber"),
                    reportedFault = json.optString("reportedFault"),
                    technicalDiagnosis = json.optString("technicalDiagnosis", json.optString("diagnosis")),
                    internalNotes = json.optString("internalNotes", json.optString("notes")),
                    status = status,
                    partsCost = partsCost,
                    laborCost = laborCost,
                    totalCost = totalCost,
                    entryDate = json.optLong("entryDate", System.currentTimeMillis()),
                    quotedDate = json.optionalLong("quotedDate"),
                    deliveryDate = json.optionalLong("deliveryDate") ?: json.optionalLong("exitDate"),
                    warrantyDays = json.optInt("warrantyDays", 90).coerceAtLeast(0),
                    warrantyTerms = json.optString("warrantyTerms", WorkOrderEntity(0, clientId, orderCode, "", reportedFault = "").warrantyTerms),
                    photosUris = json.optJSONArray("photosUris").toStringList(),
                    signatureUri = json.optionalString("signatureUri")
                )
            )
        }
    }

    private fun parseTransactions(array: JSONArray): List<FinancialTransaction> = buildList {
        for (index in 0 until array.length()) {
            val json = array.getJSONObject(index)
            val type = json.optString("type", TransactionType.INCOME.name)
            val amount = json.optDouble("amount", 0.0)
            require(TransactionType.entries.any { it.name == type }) { "Tipo de movimiento desconocido: $type" }
            require(amount.isFinite() && amount >= 0.0) { "Hay un movimiento con importe inválido." }
            add(
                FinancialTransaction(
                    id = json.optLong("id", 0L),
                    type = type,
                    amount = amount,
                    category = json.optString("category", "Otros"),
                    description = json.optString("description"),
                    date = json.optLong("date", System.currentTimeMillis()),
                    relatedOrderId = json.optionalLong("relatedOrderId")
                )
            )
        }
    }

    private fun parseTasks(array: JSONArray): List<WorkshopTask> = buildList {
        for (index in 0 until array.length()) {
            val json = array.getJSONObject(index)
            add(
                WorkshopTask(
                    id = json.optLong("id", 0L),
                    title = json.optString("title"),
                    priority = json.optString("priority", "MEDIUM"),
                    isCompleted = json.optBoolean("isCompleted", false),
                    relatedConsole = json.optString("relatedConsole"),
                    createdAt = json.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
    }

    private fun parseSupplies(array: JSONArray): List<SupplyItem> = buildList {
        for (index in 0 until array.length()) {
            val json = array.getJSONObject(index)
            val quantity = json.optInt("quantity", 1)
            val price = json.optDouble("estimatedPrice", 0.0)
            require(quantity > 0 && price.isFinite() && price >= 0.0) { "Hay un insumo inválido." }
            add(
                SupplyItem(
                    id = json.optLong("id", 0L),
                    name = json.optString("name"),
                    category = json.optString("category", "Repuestos"),
                    quantity = quantity,
                    unit = json.optString("unit", "u"),
                    isNeeded = json.optBoolean("isNeeded", true),
                    estimatedPrice = price,
                    supplier = json.optString("supplier")
                )
            )
        }
    }

    private fun JSONObject.optionalLong(key: String): Long? =
        if (has(key) && !isNull(key)) optLong(key) else null

    private fun JSONObject.optionalString(key: String): String? =
        if (has(key) && !isNull(key)) optString(key).takeIf { it.isNotBlank() } else null

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return List(length()) { index -> getString(index) }
    }

    suspend fun createShareBackupIntent(
        context: Context,
        repository: WorkshopRepository
    ): Intent = withContext(Dispatchers.IO) {
        val jsonString = generateBackupJson(repository)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "RetroStateRS_Backup_${dateFormat.format(Date())}.json"

        val backupDir = File(context.cacheDir, "backups").apply {
            check(exists() || mkdirs()) { "No se pudo crear el directorio de respaldos." }
        }
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

        Intent.createChooser(shareIntent, "Respaldar en la Nube (Drive / Correo / WhatsApp)").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
