package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ClientEntity
import com.example.data.model.WorkOrderEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador nativo de la imagen del Ticket Digital de Ingreso en formato PNG para Retro State RS.
 * Crea una imagen optimizada de 720px de ancho con estética cyber-retro y la comparte por FileProvider.
 */
object TicketImageGenerator {

    /**
     * Dibuja el Ticket Digital de Ingreso directamente sobre un Bitmap nativo de 720px de ancho.
     */
    fun generateDigitalTicketBitmap(order: WorkOrderEntity, client: ClientEntity?): Bitmap {
        val width = 720
        val height = 1040
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fondo oscuro Arcade (#0C0D17)
        canvas.drawColor(0xFF0C0D17.toInt())

        // Marco neón con bordes redondeados (#00F0FF)
        val borderPaint = Paint().apply {
            color = 0xFF00F0FF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        val rect = RectF(16f, 16f, width - 16f, height - 16f)
        canvas.drawRoundRect(rect, 24f, 24f, borderPaint)

        // Pinturas de Texto con Fuente Monospace
        val monospaceTypeface = Typeface.MONOSPACE

        val headerPaint = Paint().apply {
            color = 0xFF00F0FF.toInt()
            textSize = 36f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val subheaderPaint = Paint().apply {
            color = 0xFF9CA3AF.toInt()
            textSize = 20f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = 0xFFFFD100.toInt() // Gold Coin
            textSize = 26f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = 0xFF9CA3AF.toInt()
            textSize = 19f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = 0xFFF1F5F9.toInt()
            textSize = 23f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val cyanValuePaint = Paint().apply {
            color = 0xFF00F0FF.toInt()
            textSize = 25f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val magentaValuePaint = Paint().apply {
            color = 0xFFF72585.toInt()
            textSize = 25f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val footerTextPaint = Paint().apply {
            color = 0xFFF1F5F9.toInt()
            textSize = 22f
            typeface = Typeface.create(monospaceTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val lineDividerPaint = Paint().apply {
            color = 0xFF38355C.toInt()
            strokeWidth = 3f
            isAntiAlias = true
        }

        var y = 70f

        // Header Logo
        canvas.drawText("=== RETRO STATE RS ===", width / 2f, y, headerPaint)
        y += 35f
        canvas.drawText("TALLER & LABORATORIO TÉCNICO", width / 2f, y, subheaderPaint)
        y += 40f

        canvas.drawLine(40f, y, width - 40f, y, lineDividerPaint)
        y += 40f

        canvas.drawText("TICKET DIGITAL DE INGRESO", width / 2f, y, titlePaint)
        y += 40f

        canvas.drawLine(40f, y, width - 40f, y, lineDividerPaint)
        y += 50f

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(order.entryDate))

        val startX = 50f

        fun drawField(label: String, value: String, paint: Paint = valuePaint) {
            canvas.drawText(label, startX, y, labelPaint)
            y += 30f
            canvas.drawText(value, startX, y, paint)
            y += 42f
        }

        drawField("N° ORDEN DE TRABAJO:", order.orderCode, cyanValuePaint)
        drawField("FECHA DE INGRESO:", formattedDate)
        drawField("CLIENTE:", client?.fullName ?: "Consola en Salón")
        drawField("TELÉFONO / WHATSAPP:", client?.phone?.ifBlank { "N/D" } ?: "N/D")
        drawField("CONSOLA / EQUIPO:", order.deviceModel, magentaValuePaint)
        if (order.serialNumber.isNotBlank()) {
            drawField("N° DE SERIE (S/N):", order.serialNumber)
        }

        val faultSummary = if (order.reportedFault.length > 40) order.reportedFault.take(40) + "..." else order.reportedFault
        drawField("FALLA REPORTADA:", faultSummary)

        y += 10f
        canvas.drawLine(40f, y, width - 40f, y, lineDividerPaint)
        y += 40f

        canvas.drawText("========================================", width / 2f, y, headerPaint)
        y += 35f
        canvas.drawText("CONSERVE ESTE TICKET DIGITAL", width / 2f, y, footerTextPaint)
        y += 30f
        canvas.drawText("PARA RETIRAR SU EQUIPO", width / 2f, y, footerTextPaint)
        y += 35f
        canvas.drawText("========================================", width / 2f, y, headerPaint)
        y += 40f

        canvas.drawText("Garantía oficial Retro State RS", width / 2f, y, subheaderPaint)

        return bitmap
    }

    /**
     * Guarda la imagen del ticket en context.cacheDir/tickets/ y devuelve su objeto File.
     */
    fun saveTicketImageToCache(context: Context, bitmap: Bitmap, orderCode: String): File {
        val ticketsDir = File(context.cacheDir, "tickets")
        if (!ticketsDir.exists()) {
            ticketsDir.mkdirs()
        }
        val ticketFile = File(ticketsDir, "ticket_$orderCode.png")
        FileOutputStream(ticketFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return ticketFile
    }

    /**
     * Genera la imagen del ticket y dispara el Share Intent (WhatsApp / ShareSheet).
     */
    fun shareDigitalTicket(context: Context, order: WorkOrderEntity, client: ClientEntity?) {
        val bitmap = generateDigitalTicketBitmap(order, client)
        val imageFile = saveTicketImageToCache(context, bitmap, order.orderCode)

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        val message = "Hola ${client?.fullName ?: ""}! Te adjuntamos tu Ticket Digital de Ingreso (${order.orderCode}) para ${order.deviceModel}. ¡Gracias por confiar en Retro State RS!"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Enviar Ticket Digital PNG (WhatsApp / Email)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
