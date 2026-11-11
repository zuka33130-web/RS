package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkOrderWithClient
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.PixelGreen1UP
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroGoldCoin
import com.example.ui.theme.RetroMuted
import com.example.ui.theme.RetroWhite
import com.example.util.PdfGeneratorManager
import com.example.util.QuotePdfData
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun QuoteEditorScreen(
    initialOrderWithClient: WorkOrderWithClient? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "AR")) }

    // Autocompleted state from order if provided, or empty defaults for manual editing
    val initialOrder = initialOrderWithClient?.order
    val initialClient = initialOrderWithClient?.client

    var clientName by remember { mutableStateOf(initialClient?.fullName ?: "") }
    var clientPhone by remember { mutableStateOf(initialClient?.phone ?: "") }
    var clientAddress by remember { mutableStateOf(initialClient?.address ?: "") }

    var deviceBrand by remember {
        mutableStateOf(
            when {
                initialOrder?.deviceModel?.contains("Nintendo", ignoreCase = true) == true ||
                initialOrder?.deviceModel?.contains("SNES", ignoreCase = true) == true ||
                initialOrder?.deviceModel?.contains("N64", ignoreCase = true) == true -> "Nintendo"
                initialOrder?.deviceModel?.contains("Sega", ignoreCase = true) == true ||
                initialOrder?.deviceModel?.contains("Genesis", ignoreCase = true) == true -> "Sega"
                initialOrder?.deviceModel?.contains("Sony", ignoreCase = true) == true ||
                initialOrder?.deviceModel?.contains("PlayStation", ignoreCase = true) == true ||
                initialOrder?.deviceModel?.contains("PS", ignoreCase = true) == true -> "Sony"
                else -> "Retro Console"
            }
        )
    }

    var deviceModel by remember { mutableStateOf(initialOrder?.deviceModel ?: "") }
    var serialNumber by remember { mutableStateOf(initialOrder?.serialNumber ?: "") }
    var reportedFault by remember { mutableStateOf(initialOrder?.reportedFault ?: "") }
    var repairDetail by remember {
        mutableStateOf(
            initialOrder?.technicalDiagnosis?.ifBlank {
                "Recap de condensadores electrolíticos Nichicon, limpieza de placa con ultrasónicos, lubricación de componentes y calibración en banco de pruebas."
            } ?: "Recap de condensadores electrolíticos Nichicon, limpieza de placa con ultrasónicos, lubricación de componentes y calibración en banco de pruebas."
        )
    }

    val defaultPrice = initialOrder?.totalCost?.takeIf { it > 0 }
        ?: ((initialOrder?.partsCost ?: 0.0) + (initialOrder?.laborCost ?: 0.0))

    var finalCostInput by remember { mutableStateOf(if (defaultPrice > 0) defaultPrice.toString() else "35000") }
    var quoteCode by remember { mutableStateOf(initialOrder?.orderCode ?: "RS-PRE-0042") }

    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("quote_editor_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(ArcadeSurface, CircleShape)
                    .border(1.dp, NeonCyan, CircleShape)
                    .size(40.dp)
                    .testTag("quote_editor_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = NeonCyan
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "GENERADOR DE PRESUPUESTOS PDF",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Documento A4 Empresarial Retro State",
                    color = RetroGoldCoin,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = RetroGoldCoin,
                modifier = Modifier.size(28.dp)
            )
        }

        // --- Section 1: Customer Details ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. DATOS DEL CLIENTE",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre Completo del Cliente *") },
                    placeholder = { Text("ej. Carlos Giménez") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ArcadeCardBorder,
                        focusedTextColor = RetroWhite,
                        unfocusedTextColor = RetroWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_quote_client_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Teléfono / WhatsApp") },
                        placeholder = { Text("ej. +54 9 11 4521-8899") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = ArcadeCardBorder,
                            focusedTextColor = RetroWhite,
                            unfocusedTextColor = RetroWhite
                        ),
                        modifier = Modifier.weight(1f).testTag("input_quote_client_phone")
                    )

                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Dirección / Localidad") },
                        placeholder = { Text("ej. CABA, Lanús...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = ArcadeCardBorder,
                            focusedTextColor = RetroWhite,
                            unfocusedTextColor = RetroWhite
                        ),
                        modifier = Modifier.weight(1f).testTag("input_quote_client_address")
                    )
                }
            }
        }

        // --- Section 2: Device & Fault Details ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = NeonMagenta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2. ARTEFACTO / EQUIPO A REPARAR",
                        color = NeonMagenta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deviceBrand,
                        onValueChange = { deviceBrand = it },
                        label = { Text("Marca") },
                        placeholder = { Text("ej. Nintendo, Sega, Sony") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = ArcadeCardBorder,
                            focusedTextColor = RetroWhite,
                            unfocusedTextColor = RetroWhite
                        ),
                        modifier = Modifier.weight(1f).testTag("input_quote_brand")
                    )

                    OutlinedTextField(
                        value = deviceModel,
                        onValueChange = { deviceModel = it },
                        label = { Text("Modelo / Artefacto *") },
                        placeholder = { Text("ej. Super Nintendo (SNES)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = ArcadeCardBorder,
                            focusedTextColor = RetroWhite,
                            unfocusedTextColor = RetroWhite
                        ),
                        modifier = Modifier.weight(1.5f).testTag("input_quote_model")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Número de Serie (S/N)") },
                        placeholder = { Text("ej. UN18290192") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = ArcadeCardBorder,
                            focusedTextColor = RetroWhite,
                            unfocusedTextColor = RetroWhite
                        ),
                        modifier = Modifier.weight(1f).testTag("input_quote_sn")
                    )

                    OutlinedTextField(
                        value = quoteCode,
                        onValueChange = { quoteCode = it },
                        label = { Text("Código de Presupuesto") },
                        placeholder = { Text("ej. RS-PRE-0042") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RetroGoldCoin,
                            unfocusedBorderColor = ArcadeCardBorder,
                            focusedTextColor = RetroWhite,
                            unfocusedTextColor = RetroWhite
                        ),
                        modifier = Modifier.weight(1f).testTag("input_quote_code")
                    )
                }

                OutlinedTextField(
                    value = reportedFault,
                    onValueChange = { reportedFault = it },
                    label = { Text("Falla Reportada por el Cliente") },
                    placeholder = { Text("ej. Sin salida de video por conector RCA...") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelRedHeart,
                        unfocusedBorderColor = ArcadeCardBorder,
                        focusedTextColor = RetroWhite,
                        unfocusedTextColor = RetroWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_quote_fault")
                )
            }
        }

        // --- Section 3: Repair Detail & Costs ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RetroGoldCoin.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = RetroGoldCoin,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3. REPARACIÓN Y COSTO FINAL",
                        color = RetroGoldCoin,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                OutlinedTextField(
                    value = repairDetail,
                    onValueChange = { repairDetail = it },
                    label = { Text("Reparación a Realizar / Detalle de Insumos *") },
                    placeholder = { Text("ej. Recap completo Nichicon, reemplazo 7805 y calibración en banco...") },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RetroGoldCoin,
                        unfocusedBorderColor = ArcadeCardBorder,
                        focusedTextColor = RetroWhite,
                        unfocusedTextColor = RetroWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_quote_repair_detail")
                )

                OutlinedTextField(
                    value = finalCostInput,
                    onValueChange = { finalCostInput = it },
                    label = { Text("Costo Final ($) *") },
                    placeholder = { Text("ej. 35000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelGreen1UP,
                        unfocusedBorderColor = ArcadeCardBorder,
                        focusedTextColor = RetroWhite,
                        unfocusedTextColor = RetroWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_quote_final_cost")
                )
            }
        }

        // --- Mandatory Legal Legend Preview ---
        Surface(
            color = ArcadeSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ArcadeCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "📜 LEYENDA LEGAL MANDATORIA EN PIE DE PÁGINA:",
                    color = RetroGoldCoin,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\"Presupuesto válido sujeto a modificación según el precio actual de repuestos. Todas las reparaciones tienen garantía de ${initialOrder?.warrantyDays ?: 30} días.\"",
                    color = RetroMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // --- Action Buttons (Generate PDF & Share Intent) ---
        val costValue = finalCostInput.replace(',', '.').toDoubleOrNull() ?: 0.0
        val isFormValid = clientName.isNotBlank() && deviceModel.isNotBlank() && repairDetail.isNotBlank() && costValue > 0

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (isFormValid) {
                        val pdfData = QuotePdfData(
                            clientName = clientName.trim(),
                            clientPhone = clientPhone.trim(),
                            clientAddress = clientAddress.trim(),
                            deviceBrand = deviceBrand.trim(),
                            deviceModel = deviceModel.trim(),
                            serialNumber = serialNumber.trim(),
                            reportedFault = reportedFault.trim(),
                            repairDetail = repairDetail.trim(),
                            finalCost = costValue,
                            quoteCode = quoteCode.trim(),
                            warrantyDays = initialOrder?.warrantyDays ?: 30
                        )
                        runCatching { PdfGeneratorManager.createQuotePdf(context, pdfData) }
                            .onSuccess { pdfFile ->
                                generatedPdfFile = pdfFile
                                Toast.makeText(context, "¡Presupuesto PDF generado con éxito!", Toast.LENGTH_SHORT).show()
                                runCatching { PdfGeneratorManager.sharePdfIntent(context, pdfFile, pdfData.quoteCode) }
                                    .onFailure {
                                        Toast.makeText(context, "El PDF se generó, pero no hay una app para compartirlo.", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .onFailure {
                                Toast.makeText(context, "No se pudo generar el PDF. Intentalo nuevamente.", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelGreen1UP,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_generate_share_pdf")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERAR Y COMPARTIR PDF (WhatsApp / Email)",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            generatedPdfFile?.let { file ->
                OutlinedButton(
                    onClick = {
                        PdfGeneratorManager.sharePdfIntent(context, file, quoteCode)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_re share_pdf")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reenviar Último PDF Generado (${file.name})",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
