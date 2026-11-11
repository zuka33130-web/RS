package com.example.data.model

/**
 * Conteo de ingresos agrupado por modelo de consola.
 */
data class DeviceModelCount(
    val deviceModel: String,
    val count: Int
)

/**
 * Estadísticas de rentabilidad agrupadas por tipo de falla o reparación.
 */
data class FaultProfitStat(
    val reportedFault: String,
    val count: Int,
    val totalProfit: Double
)

/**
 * Resumen de importes financieros agrupados por tipo (INCOME vs EXPENSE).
 */
data class FinancialTypeSummary(
    val type: String,
    val totalAmount: Double
)

/**
 * Oportunidad de Retargeting Post-Venta (Side Quest de Mantenimiento Preventivo).
 */
data class RetargetingCandidate(
    val workOrderWithClient: WorkOrderWithClient,
    val monthsSinceDelivery: Int,
    val suggestedAction: String
)
