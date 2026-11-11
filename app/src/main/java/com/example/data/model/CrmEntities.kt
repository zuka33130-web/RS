package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Entidad de Cliente (CRM) para el servicio técnico Retro State RS.
 */
@Entity(
    tableName = "crm_clients",
    indices = [Index(value = ["phone"])]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val address: String = "",
    val phone: String = "", // Teléfono / WhatsApp
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

/**
 * Los 6 estados exactos del flujo de trabajo de reparación en el taller.
 */
enum class WorkOrderStatus(
    val displayName: String,
    val terminalBadge: String,
    val stageNumber: Int
) {
    TO_CHECK("A revisar", "[STAGE 1: SCAN]", 1),
    QUOTED("Presupuestada", "[STAGE 2: QUOTE]", 2),
    WAITING_PARTS("Esperando repuesto", "[STAGE 3: PARTS]", 3),
    REPAIRED("Reparada", "[STAGE 4: FIXED]", 4),
    DELIVERED("Entregada", "[STAGE 5: DEPLOYED]", 5),
    UNDER_WARRANTY("En garantía", "[STAGE 6: SHIELD]", 6)
}

/**
 * Entidad relacional de Orden de Trabajo (1 Cliente -> N Órdenes de Trabajo).
 */
@Entity(
    tableName = "work_orders",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["status"]),
        Index(value = ["orderCode"], unique = true)
    ]
)
data class WorkOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val orderCode: String, // ej. "RS-2026-0042"
    val deviceModel: String,
    val serialNumber: String = "",
    val reportedFault: String,
    val technicalDiagnosis: String = "",
    val internalNotes: String = "",
    val status: String = WorkOrderStatus.TO_CHECK.name,
    val partsCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val entryDate: Long = System.currentTimeMillis(),
    val quotedDate: Long? = null,
    val deliveryDate: Long? = null,
    val warrantyDays: Int = 90, // Cobertura estándar 90 días
    val warrantyTerms: String = "Garantía oficial Retro State RS sobre mano de obra y repuestos sustituidos. Sellos de seguridad intactos. Excluye golpes, humedad o manipulación no autorizada.",
    val photosUris: List<String> = emptyList(), // Evidencia fotográfica del estado físico
    val signatureUri: String? = null // Ruta de la firma digital del cliente
)

/**
 * Relación 1 a N: Cliente con todas sus órdenes de reparación.
 */
data class ClientWithOrders(
    @Embedded val client: ClientEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "clientId"
    )
    val orders: List<WorkOrderEntity>
)

/**
 * Relación N a 1: Orden de trabajo con los datos de su cliente asociado.
 */
data class WorkOrderWithClient(
    @Embedded val order: WorkOrderEntity,
    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: ClientEntity?
)
