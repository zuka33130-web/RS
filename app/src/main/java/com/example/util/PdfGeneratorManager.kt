package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuotePdfData(
    val clientName: String,
    val clientPhone: String = "",
    val clientAddress: String = "",
    val deviceBrand: String = "",
    val deviceModel: String = "",
    val serialNumber: String = "",
    val reportedFault: String = "",
    val repairDetail: String = "",
    val finalCost: Double = 0.0,
    val quoteCode: String = "RS-PRE-001",
    val date: Long = System.currentTimeMillis()
)

object PdfGeneratorManager {

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842

    /**
     * Genera un archivo PDF profesional formato A4 dibujado con Canvas nativo.
     */
    fun createQuotePdf(context: Context, data: QuotePdfData): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPdfContent(canvas, data)

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val quotesDir = File(context.cacheDir, "quotes").apply { mkdirs() }
        val cleanCode = data.quoteCode.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "Presupuesto_${cleanCode}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(quotesDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun drawPdfContent(canvas: Canvas, data: QuotePdfData) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Colors
        val darkHeaderBg = Color.rgb(12, 13, 23) // #0C0D17 ArcadeDark
        val neonCyan = Color.rgb(0, 240, 255)     // #00F0FF NeonCyan
        val retroGold = Color.rgb(255, 209, 0)    // #FFD100 RetroGold
        val textDark = Color.rgb(30, 30, 45)     // Dark Navy text
        val textMuted = Color.rgb(100, 100, 120)  // Gray text
        val tableBg = Color.rgb(245, 247, 250)    // Light surface table
        val borderGray = Color.rgb(210, 215, 225) // Light border

        val paint = Paint().apply { isAntiAlias = true }

        // --- 1. HEADER BANNER ---
        paint.color = darkHeaderBg
        canvas.drawRect(0f, 0f, A4_WIDTH.toFloat(), 110f, paint)

        // Header Title
        paint.color = neonCyan
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("RETRO STATE RS", 30f, 40f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("LABORATORIO DE REPARACIÓN & RESTAURACIÓN DE CONSOLAS", 30f, 58f, paint)

        paint.color = retroGold
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("Tel/WhatsApp: +54 9 11 4521-8899 • CABA, Argentina", 30f, 74f, paint)

        // Document Reference Box (Top Right)
        paint.color = retroGold
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val codeText = "PRESUPUESTO ${data.quoteCode}"
        canvas.drawText(codeText, A4_WIDTH - 210f, 40f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Fecha: ${dateFormat.format(Date(data.date))}", A4_WIDTH - 210f, 58f, paint)

        // --- 2. CLIENT & DEVICE INFORMATION BOXES ---
        var currentY = 135f

        // Section Title 1: CLIENTE
        paint.color = darkHeaderBg
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("DATOS DEL CLIENTE", 30f, currentY, paint)

        paint.color = borderGray
        paint.strokeWidth = 1f
        canvas.drawLine(30f, currentY + 4f, 280f, currentY + 4f, paint)

        currentY += 20f
        drawTextPair(canvas, "Cliente:", data.clientName.ifBlank { "Particular" }, 30f, currentY, textDark, textMuted)
        currentY += 16f
        if (data.clientPhone.isNotBlank()) {
            drawTextPair(canvas, "Teléfono:", data.clientPhone, 30f, currentY, textDark, textMuted)
            currentY += 16f
        }
        if (data.clientAddress.isNotBlank()) {
            drawTextPair(canvas, "Dirección:", data.clientAddress, 30f, currentY, textDark, textMuted)
            currentY += 16f
        }

        // Section Title 2: EQUIPO (Right Column)
        var rightY = 135f
        paint.color = darkHeaderBg
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("DATOS DEL EQUIPO", 310f, rightY, paint)

        paint.color = borderGray
        paint.strokeWidth = 1f
        canvas.drawLine(310f, rightY + 4f, A4_WIDTH - 30f, rightY + 4f, paint)

        rightY += 20f
        if (data.deviceBrand.isNotBlank()) {
            drawTextPair(canvas, "Marca:", data.deviceBrand, 310f, rightY, textDark, textMuted)
            rightY += 16f
        }
        drawTextPair(canvas, "Modelo / Equipo:", data.deviceModel.ifBlank { "Consola Clásica" }, 310f, rightY, textDark, textMuted)
        rightY += 16f
        if (data.serialNumber.isNotBlank()) {
            drawTextPair(canvas, "Número de Serie (S/N):", data.serialNumber, 310f, rightY, textDark, textMuted)
            rightY += 16f
        }

        currentY = maxOf(currentY, rightY) + 10f

        // Reported Fault Box
        if (data.reportedFault.isNotBlank()) {
            paint.color = tableBg
            val faultRect = RectF(30f, currentY, A4_WIDTH - 30f, currentY + 42f)
            canvas.drawRoundRect(faultRect, 6f, 6f, paint)

            paint.color = borderGray
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(faultRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(180, 40, 40)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("FALLA REPORTADA POR EL CLIENTE:", 40f, currentY + 16f, paint)

            paint.color = textDark
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText(data.reportedFault, 40f, currentY + 32f, paint)

            currentY += 56f
        }

        // --- 3. QUOTATION TABLE ---
        currentY += 10f
        paint.color = darkHeaderBg
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("DETALLE DE REPARACIÓN Y PRESUPUESTO", 30f, currentY, paint)

        currentY += 10f

        // Table Header Fill
        paint.color = darkHeaderBg
        val tableHeaderRect = RectF(30f, currentY, A4_WIDTH - 30f, currentY + 24f)
        canvas.drawRoundRect(tableHeaderRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("DESCRIPCIÓN DE LA REPARACIÓN A REALIZAR / INSUMOS", 40f, currentY + 16f, paint)
        canvas.drawText("IMPORTE", A4_WIDTH - 110f, currentY + 16f, paint)

        currentY += 24f

        // Table Row Fill
        paint.color = tableBg
        val rowRect = RectF(30f, currentY, A4_WIDTH - 30f, currentY + 65f)
        canvas.drawRect(rowRect, paint)

        paint.color = borderGray
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(rowRect, paint)
        paint.style = Paint.Style.FILL

        // Detail text
        paint.color = textDark
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val repairText = data.repairDetail.ifBlank { "Mantenimiento general, reparación de placa lógica e insumos de taller." }

        // Multi-line wrap for long detail
        val lines = wrapText(repairText, 50)
        var textY = currentY + 20f
        lines.take(3).forEach { line ->
            canvas.drawText(line, 40f, textY, paint)
            textY += 15f
        }

        // Amount Right Alignment
        paint.color = textDark
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(currencyFormat.format(data.finalCost), A4_WIDTH - 115f, currentY + 30f, paint)

        currentY += 65f

        // Total Box (Highlighted)
        val totalBoxRect = RectF(A4_WIDTH - 250f, currentY + 10f, A4_WIDTH - 30f, currentY + 50f)
        paint.color = darkHeaderBg
        canvas.drawRoundRect(totalBoxRect, 6f, 6f, paint)

        paint.color = retroGold
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("COSTO FINAL TOTAL:", A4_WIDTH - 240f, currentY + 34f, paint)

        paint.color = neonCyan
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(currencyFormat.format(data.finalCost), A4_WIDTH - 125f, currentY + 34f, paint)

        // --- 4. LEGAL FOOTER (MANDATORY EXACT LEYEND) ---
        val footerY = A4_HEIGHT - 65f

        paint.color = borderGray
        paint.strokeWidth = 1f
        canvas.drawLine(30f, footerY - 15f, A4_WIDTH - 30f, footerY - 15f, paint)

        paint.color = textMuted
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        // Mandatory exact legend requirement
        val legalLegend = "Presupuesto válido sujeto a modificación según el precio actual de repuestos. Todas las reparaciones tienen garantía de 30 días."
        canvas.drawText(legalLegend, 30f, footerY, paint)

        paint.color = textMuted
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Retro State RS • Laboratorio Especializado • CABA, Argentina", 30f, footerY + 14f, paint)
    }

    private fun drawTextPair(
        canvas: Canvas,
        label: String,
        value: String,
        x: Float,
        y: Float,
        textColor: Int,
        labelColor: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }
        paint.color = labelColor
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(label, x, y, paint)

        val labelWidth = paint.measureText(label)
        paint.color = textColor
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText(" $value", x + labelWidth, y, paint)
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > maxCharsPerLine) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    /**
     * Dispara el Intent de compartir el PDF generado por WhatsApp, Email, Drive o SMS.
     */
    fun sharePdfIntent(context: Context, pdfFile: File, quoteCode: String) {
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Presupuesto Técnico $quoteCode - Retro State RS")
            putExtra(
                Intent.EXTRA_TEXT,
                "Adjunto presupuesto formal en PDF ($quoteCode) emitido por el servicio técnico Retro State RS."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir Presupuesto PDF (WhatsApp / Email / Drive)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
