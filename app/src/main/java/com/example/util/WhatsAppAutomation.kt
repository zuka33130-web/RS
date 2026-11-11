package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.ClientEntity
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador de documentos formales técnicos con estética retro-profesional
 * para el servicio técnico RETRO STATE RS.
 */
object RetroDocumentGenerator {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Documento 1: Comprobante de Recepción / Orden de Ingreso.
     */
    fun generateIntakeReceipt(order: WorkOrderEntity, client: ClientEntity?): String {
        val clientName = client?.fullName ?: "Cliente Particular"
        val clientPhone = client?.phone ?: "No registrado"
        val clientAddress = client?.address?.takeIf { it.isNotBlank() } ?: "Retiro en Taller"
        val entryDateStr = dateFormat.format(Date(order.entryDate))

        return buildString {
            appendLine("════════════════════════════════════")
            appendLine("  👾 RETRO STATE RS • TALLER TÉCNICO")
            appendLine("   ORDEN DE INGRESO: ${order.orderCode}")
            appendLine("════════════════════════════════════")
            appendLine("📅 Fecha de Recepción: $entryDateStr")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 DATOS DEL CLIENTE:")
            appendLine("• Nombre: $clientName")
            appendLine("• Teléfono: $clientPhone")
            appendLine("• Dirección: $clientAddress")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🎮 DATOS DEL EQUIPO RETRO:")
            appendLine("• Modelo: ${order.deviceModel}")
            if (order.serialNumber.isNotBlank()) {
                appendLine("• N/S (Serial): ${order.serialNumber}")
            }
            appendLine("• Estado Actual: ${WorkOrderStatus.values().find { it.name == order.status }?.displayName ?: order.status}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🔍 FALLA REPORTADA POR EL CLIENTE:")
            appendLine("> ${order.reportedFault.trim()}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📌 TÉRMINOS DE BANCO DE TRABAJO:")
            appendLine("1. El equipo ingresa a diagnóstico técnico integral.")
            appendLine("2. Presupuesto sin cargo en 24 a 48 hs hábiles.")
            appendLine("3. No se inician reparaciones sin previa autorización.")
            appendLine("════════════════════════════════════")
            appendLine("🕹️ Retro State RS • Cuidamos tus clásicos.")
        }
    }

    /**
     * Documento 2: Presupuesto Profesional y Detallado de Reparación.
     */
    fun generateQuoteDocument(order: WorkOrderEntity, client: ClientEntity?): String {
        val clientName = client?.fullName ?: "Estimado/a Jugador/a"
        val total = if (order.totalCost > 0) order.totalCost else (order.partsCost + order.laborCost)
        val quoteDateStr = dateFormat.format(Date(order.quotedDate ?: System.currentTimeMillis()))

        return buildString {
            appendLine("════════════════════════════════════")
            appendLine("  ⚡ RETRO STATE RS • PRESUPUESTO TÉCNICO")
            appendLine("       REF: ${order.orderCode}")
            appendLine("════════════════════════════════════")
            appendLine("📅 Fecha Presupuesto: $quoteDateStr")
            appendLine("👤 Cliente: $clientName")
            appendLine("🎮 Equipo: ${order.deviceModel}")
            if (order.serialNumber.isNotBlank()) {
                appendLine("🔢 N/S: ${order.serialNumber}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🔬 INFORME Y DIAGNÓSTICO TÉCNICO:")
            val diag = order.technicalDiagnosis.takeIf { it.isNotBlank() }
                ?: "Revisión en banco finalizada. Requiere mantenimiento y componentes."
            appendLine("> $diag")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("💰 DESGLOSE DE COSTOS:")
            if (order.partsCost > 0) {
                appendLine("• Repuestos / Insumos: ${currencyFormat.format(order.partsCost)}")
            }
            if (order.laborCost > 0) {
                appendLine("• Mano de Obra Especializada: ${currencyFormat.format(order.laborCost)}")
            }
            appendLine("────────────────────────────────────")
            appendLine("⭐ TOTAL PRESUPUESTADO: ${currencyFormat.format(total)}")
            appendLine("────────────────────────────────────")
            appendLine("⏱️ Tiempo Estimado: 48 a 72 hs tras confirmación.")
            appendLine("⏳ Validez del Presupuesto: 7 días corridos.")
            appendLine("🛡️ Cobertura Incluida: ${order.warrantyDays} días de garantía.")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("👉 Por favor responde a este mensaje con 'ACEPTO EL PRESUPUESTO' para dar inicio a la reparación.")
            appendLine("════════════════════════════════════")
            appendLine("Retro State RS • Laboratorio Especializado")
        }
    }

    /**
     * Documento 3: Certificado de Garantía Oficial.
     */
    fun generateWarrantyCertificate(order: WorkOrderEntity, client: ClientEntity?): String {
        val clientName = client?.fullName ?: "Cliente Titular"
        val deliveryDate = order.deliveryDate ?: System.currentTimeMillis()
        val deliveryStr = dateOnlyFormat.format(Date(deliveryDate))
        val expirationMillis = deliveryDate + (order.warrantyDays.toLong() * 86400000L)
        val expirationStr = dateOnlyFormat.format(Date(expirationMillis))
        val total = if (order.totalCost > 0) order.totalCost else (order.partsCost + order.laborCost)

        return buildString {
            appendLine("════════════════════════════════════")
            appendLine("  🛡️ RETRO STATE RS • CERTIFICADO DE GARANTÍA")
            appendLine("        PÓLIZA N°: GAR-${order.orderCode}")
            appendLine("════════════════════════════════════")
            appendLine("👤 Titular: $clientName")
            appendLine("🎮 Equipo: ${order.deviceModel}")
            if (order.serialNumber.isNotBlank()) {
                appendLine("🔢 N/S: ${order.serialNumber}")
            }
            appendLine("📅 Fecha de Entrega: $deliveryStr")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🔧 TRABAJO REALIZADO & LIQUIDADO:")
            val workDone = order.technicalDiagnosis.takeIf { it.isNotBlank() }
                ?: order.reportedFault
            appendLine("> $workDone")
            if (total > 0) {
                appendLine("• Importe Abonado: ${currencyFormat.format(total)}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🛡️ COBERTURA DE GARANTÍA:")
            appendLine("• Plazo: ${order.warrantyDays} DÍAS CORRIDOS")
            appendLine("• Vencimiento: $expirationStr")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📜 CONDICIONES Y EXCLUSIONES:")
            appendLine("1. La garantía cubre fallas en los repuestos instalados y en la mano de obra del taller.")
            appendLine("2. Los sellos de seguridad térmicos de Retro State RS deben permanecer intactos.")
            appendLine("3. Quedan excluidos daños por golpes, humedad, sobretensión de la red eléctrica o manipulación externa.")
            appendLine("════════════════════════════════════")
            appendLine("¡Tu consola clásica lista para muchas horas de juego! 👾")
        }
    }
}

/**
 * Disparador de WhatsApp mediante Intent de Android.
 */
object WhatsAppSender {

    fun sendToWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.filter { it.isDigit() }
        
        try {
            val uri = if (cleanPhone.isNotBlank()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
            }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic ACTION_SEND share sheet
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(shareIntent)
            } catch (fallbackError: Exception) {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message)
                    },
                    "Enviar Documento Retro State RS"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        }
    }
}
