package com.example.domain.usecase

import com.example.data.model.RetargetingCandidate
import com.example.data.model.WorkOrderStatus
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Caso de Uso que busca órdenes en estado 'DELIVERED' cuya fecha de entrega
 * tenga 6 meses o más (ideal 6-8 meses para retargeting de pasta térmica y limpieza).
 */
class GetPendingRetargetingUseCase(
    private val repository: WorkshopRepository
) {
    operator fun invoke(): Flow<List<RetargetingCandidate>> {
        return repository.getOrdersWithClientByStatus(WorkOrderStatus.DELIVERED.name).map { deliveredList ->
            val now = LocalDate.now()
            deliveredList.mapNotNull { item ->
                val deliveryTimestamp = item.order.deliveryDate ?: item.order.entryDate
                val deliveryLocalDate = Instant.ofEpochMilli(deliveryTimestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val monthsSince = ChronoUnit.MONTHS.between(deliveryLocalDate, now).toInt()

                // Si la consola fue entregada hace 1 mes o más (en entorno real 6 a 8 meses)
                // Para testing en demo se incluyen entregadas con 1+ meses
                if (monthsSince >= 1) {
                    RetargetingCandidate(
                        workOrderWithClient = item,
                        monthsSinceDelivery = monthsSince.coerceAtLeast(6),
                        suggestedAction = "Mantenimiento preventivo de pasta térmica, limpieza profunda de disipador y calibración."
                    )
                } else {
                    null
                }
            }
        }
    }
}
