package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ConfirmationNumber
import com.example.util.TicketImageGenerator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.ui.draw.clip
import com.example.ui.components.LocalImageViewer
import com.example.data.model.ClientEntity
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkOrderWithClient
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.GameBoyGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.PixelGreen1UP
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroGoldCoin
import com.example.ui.theme.RetroMuted
import com.example.ui.theme.RetroWhite
import com.example.ui.theme.SnesPurple
import com.example.util.RetroDocumentGenerator
import com.example.util.WhatsAppSender
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderDetailScreen(
    orderWithClient: WorkOrderWithClient,
    onBack: () -> Unit,
    onUpdateStatus: (WorkOrderStatus) -> Unit,
    onUpdateCosts: (partsCost: Double, laborCost: Double, diagnosis: String) -> Unit,
    onDeleteOrder: () -> Unit,
    onOpenQuoteEditor: (WorkOrderWithClient) -> Unit = {}
) {
    val context = LocalContext.current
    val order = orderWithClient.order
    val client = orderWithClient.client

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "AR")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var isStatusDropdownExpanded by remember { mutableStateOf(false) }
    var showEditCostsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val currentStatus = WorkOrderStatus.values().find { it.name == order.status } ?: WorkOrderStatus.TO_CHECK

    // Color and tag for current workflow state
    val statusColor = when (currentStatus) {
        WorkOrderStatus.TO_CHECK -> PixelRedHeart
        WorkOrderStatus.QUOTED -> RetroGoldCoin
        WorkOrderStatus.WAITING_PARTS -> NeonMagenta
        WorkOrderStatus.REPAIRED -> NeonCyan
        WorkOrderStatus.DELIVERED -> PixelGreen1UP
        WorkOrderStatus.UNDER_WARRANTY -> GameBoyGreen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("order_detail_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Terminal Top Bar ---
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
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = NeonCyan
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FICHA DE TRABAJO RS",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = order.orderCode,
                    color = RetroGoldCoin,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            IconButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .background(ArcadeSurface, CircleShape)
                    .border(1.dp, PixelRedHeart.copy(alpha = 0.6f), CircleShape)
                    .size(40.dp)
                    .testTag("delete_order_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar Orden",
                    tint = PixelRedHeart,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- 2. Interactive Status Workflow Selector (Cyber Dropdown) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, statusColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORKFLOW DE REPARACIÓN:",
                        color = RetroMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, statusColor)
                    ) {
                        Text(
                            text = currentStatus.terminalBadge,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dropdown trigger button
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = ArcadeSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, statusColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isStatusDropdownExpanded = true }
                            .testTag("status_dropdown_trigger")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${currentStatus.stageNumber}. ${currentStatus.displayName}",
                                    color = RetroWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Cambiar Estado",
                                tint = statusColor
                            )
                        }
                    }

                    // Retro-themed DropdownMenu with pixelated borders
                    DropdownMenu(
                        expanded = isStatusDropdownExpanded,
                        onDismissRequest = { isStatusDropdownExpanded = false },
                        modifier = Modifier
                            .background(ArcadeSurface)
                            .border(1.5.dp, NeonCyan, RoundedCornerShape(8.dp))
                    ) {
                        WorkOrderStatus.values().forEach { status ->
                            val isSelected = status == currentStatus
                            val itemColor = when (status) {
                                WorkOrderStatus.TO_CHECK -> PixelRedHeart
                                WorkOrderStatus.QUOTED -> RetroGoldCoin
                                WorkOrderStatus.WAITING_PARTS -> NeonMagenta
                                WorkOrderStatus.REPAIRED -> NeonCyan
                                WorkOrderStatus.DELIVERED -> PixelGreen1UP
                                WorkOrderStatus.UNDER_WARRANTY -> GameBoyGreen
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(itemColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "${status.stageNumber}. ${status.displayName}",
                                            color = if (isSelected) itemColor else RetroWhite,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = {
                                    isStatusDropdownExpanded = false
                                    onUpdateStatus(status)
                                },
                                modifier = Modifier.testTag("status_option_${status.name}")
                            )
                        }
                    }
                }
            }
        }

        // --- 3. CRM Client Information Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                            text = "FICHA DE CLIENTE (CRM)",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (client?.phone?.isNotBlank() == true) {
                        Surface(
                            color = PixelGreen1UP.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, PixelGreen1UP),
                            modifier = Modifier.clickable {
                                WhatsAppSender.sendToWhatsApp(
                                    context = context,
                                    phone = client.phone,
                                    message = "Hola ${client.fullName}! Te escribimos desde el taller Retro State RS sobre tu orden ${order.orderCode} (${order.deviceModel})."
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = PixelGreen1UP,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Chat WhatsApp",
                                    color = PixelGreen1UP,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DetailItem(label = "Nombre Completo", value = client?.fullName ?: "Consola en Salón")
                DetailItem(label = "Teléfono / Contacto", value = client?.phone?.ifBlank { "No especificado" } ?: "No especificado")
                DetailItem(label = "Dirección", value = client?.address?.ifBlank { "Retiro por Taller" } ?: "Retiro por Taller")
                if (client?.notes?.isNotBlank() == true) {
                    DetailItem(label = "Notas del Cliente", value = client.notes)
                }
            }
        }

        // --- 4. Equipment & Diagnostics Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = NeonMagenta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EQUIPO & DIAGNÓSTICO TÉCNICO",
                        color = NeonMagenta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                DetailItem(label = "Modelo de Consola", value = order.deviceModel)
                if (order.serialNumber.isNotBlank()) {
                    DetailItem(label = "Número de Serie (S/N)", value = order.serialNumber)
                }
                DetailItem(label = "Fecha de Ingreso", value = dateFormat.format(Date(order.entryDate)))

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Falla Reportada:",
                    color = PixelRedHeart,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = ArcadeSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = order.reportedFault.ifBlank { "Sin detalle especificado." },
                        color = RetroWhite,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(
                    text = "Diagnóstico Técnico Oficial:",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = ArcadeSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = order.technicalDiagnosis.ifBlank { "En proceso de revisión en banco de trabajo." },
                        color = RetroWhite,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                if (order.internalNotes.isNotBlank()) {
                    Text(
                        text = "Notas Internas del Taller:",
                        color = RetroGoldCoin,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = ArcadeSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = order.internalNotes,
                            color = RetroWhite,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // --- 4.5. Legal Intake Evidence & Signature ---
        if (order.photosUris.isNotEmpty() || !order.signatureUri.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EVIDENCIA FOTOGRÁFICA Y FIRMA LEGAL",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (order.photosUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Estado físico al ingresar (${order.photosUris.size} fotos):",
                            color = RetroMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(order.photosUris) { index, photoUri ->
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                                ) {
                                    LocalImageViewer(
                                        uriString = photoUri,
                                        contentDescription = "Foto ${index + 1}",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    if (!order.signatureUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Firma digital de conformidad:",
                            color = PixelGreen1UP,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(ArcadeDark, RoundedCornerShape(8.dp))
                                .border(1.dp, PixelGreen1UP, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            LocalImageViewer(
                                uriString = order.signatureUri,
                                contentDescription = "Firma cliente",
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- 5. Budget & Cost Breakdown Module ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RetroGoldCoin.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = RetroGoldCoin,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRESUPUESTO Y COSTOS",
                            color = RetroGoldCoin,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    TextButton(
                        onClick = { showEditCostsDialog = true },
                        modifier = Modifier.testTag("btn_edit_costs")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar Costos", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CostBox(label = "Repuestos", amount = order.partsCost, currencyFormat = currencyFormat, color = NeonMagenta)
                    CostBox(label = "Mano de Obra", amount = order.laborCost, currencyFormat = currencyFormat, color = NeonCyan)
                    CostBox(label = "TOTAL", amount = if (order.totalCost > 0) order.totalCost else (order.partsCost + order.laborCost), currencyFormat = currencyFormat, color = RetroGoldCoin, isBold = true)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onOpenQuoteEditor(orderWithClient) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroGoldCoin,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_open_pdf_quote_editor")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GENERAR PRESUPUESTO PDF (A4)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- 6. WhatsApp Document Automation Actions (B2B Engine) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ArcadeCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = PixelGreen1UP,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DOCUMENTOS AUTOMATIZADOS (WHATSAPP)",
                        color = PixelGreen1UP,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Genera documentos con formato técnico formal y despáchalos en un toque directamente al chat de WhatsApp del cliente:",
                    color = RetroMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // ACTION 0: Ticket Digital PNG (Imagen)
                DocActionButton(
                    title = "0. Enviar Ticket Digital (Imagen PNG)",
                    subtitle = "Comprobante visual inmersivo en alta resolución con diseño retro arcade.",
                    accentColor = NeonCyan,
                    icon = Icons.Default.ConfirmationNumber,
                    testTag = "btn_send_digital_ticket_image",
                    onClick = {
                        TicketImageGenerator.shareDigitalTicket(context, order, client)
                    }
                )

                // ACTION 1: Orden de Trabajo (Ingreso)
                DocActionButton(
                    title = "1. Enviar Orden de Ingreso (Recepción)",
                    subtitle = "Comprobante formal de recepción con número de orden, número de serie y fallas.",
                    accentColor = NeonCyan,
                    icon = Icons.Default.ReceiptLong,
                    testTag = "btn_send_intake_whatsapp",
                    onClick = {
                        val message = RetroDocumentGenerator.generateIntakeReceipt(order, client)
                        WhatsAppSender.sendToWhatsApp(context, client?.phone.orEmpty(), message)
                    }
                )

                // ACTION 2: Presupuesto Profesional
                DocActionButton(
                    title = "2. Enviar Presupuesto Profesional",
                    subtitle = "Desglose formal de costos (repuestos + mano de obra), diagnóstico y validez.",
                    accentColor = RetroGoldCoin,
                    icon = Icons.Default.MonetizationOn,
                    testTag = "btn_send_quote_whatsapp",
                    onClick = {
                        val message = RetroDocumentGenerator.generateQuoteDocument(order, client)
                        WhatsAppSender.sendToWhatsApp(context, client?.phone.orEmpty(), message)
                    }
                )

                // ACTION 3: Orden de Garantía
                DocActionButton(
                    title = "3. Enviar Certificado de Garantía",
                    subtitle = "Certificado formal de cobertura (${order.warrantyDays} días), condiciones y fecha límite.",
                    accentColor = PixelGreen1UP,
                    icon = Icons.Default.Security,
                    testTag = "btn_send_warranty_whatsapp",
                    onClick = {
                        val message = RetroDocumentGenerator.generateWarrantyCertificate(order, client)
                        WhatsAppSender.sendToWhatsApp(context, client?.phone.orEmpty(), message)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- Dialog: Edit Costs & Diagnosis ---
    if (showEditCostsDialog) {
        var partsInput by remember { mutableStateOf(if (order.partsCost > 0) order.partsCost.toString() else "") }
        var laborInput by remember { mutableStateOf(if (order.laborCost > 0) order.laborCost.toString() else "") }
        var diagnosisInput by remember { mutableStateOf(order.technicalDiagnosis) }
        val partsCost = partsInput.replace(',', '.').toDoubleOrNull() ?: 0.0
        val laborCost = laborInput.replace(',', '.').toDoubleOrNull() ?: 0.0
        val areCostsValid = listOf(partsInput, laborInput).all { input ->
            input.isBlank() || input.replace(',', '.').toDoubleOrNull()?.let { it.isFinite() && it >= 0.0 } == true
        }

        AlertDialog(
            onDismissRequest = { showEditCostsDialog = false },
            containerColor = ArcadeSurface,
            title = {
                Text(
                    text = "PRESUPUESTO TÉCNICO",
                    color = RetroGoldCoin,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Detalla los costos de repuestos y mano de obra para calcular el presupuesto final:",
                        color = RetroMuted,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = partsInput,
                        onValueChange = { partsInput = it },
                        label = { Text("Costo Repuestos ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = ArcadeCardBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_parts_cost")
                    )

                    OutlinedTextField(
                        value = laborInput,
                        onValueChange = { laborInput = it },
                        label = { Text("Mano de Obra ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = ArcadeCardBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_labor_cost")
                    )

                    OutlinedTextField(
                        value = diagnosisInput,
                        onValueChange = { diagnosisInput = it },
                        label = { Text("Diagnóstico / Trabajo a Realizar") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RetroGoldCoin,
                            unfocusedBorderColor = ArcadeCardBorder
                        ),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("input_diagnosis")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCosts(partsCost, laborCost, diagnosisInput)
                        showEditCostsDialog = false
                    },
                    enabled = areCostsValid,
                    colors = ButtonDefaults.buttonColors(containerColor = RetroGoldCoin, contentColor = Color.Black),
                    modifier = Modifier.testTag("btn_save_costs")
                ) {
                    Text("Guardar Presupuesto", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCostsDialog = false }) {
                    Text("Cancelar", color = RetroMuted)
                }
            }
        )
    }

    // --- Dialog: Delete Confirmation ---
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = ArcadeSurface,
            title = {
                Text("¿Eliminar Orden de Trabajo?", color = PixelRedHeart, fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "Esta acción eliminará permanentemente la orden ${order.orderCode} del sistema.",
                    color = RetroWhite,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteOrder()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelRedHeart)
                ) {
                    Text("Eliminar Definitivamente", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = RetroMuted)
                }
            }
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            color = RetroMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = RetroWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CostBox(
    label: String,
    amount: Double,
    currencyFormat: NumberFormat,
    color: Color,
    isBold: Boolean = false
) {
    Surface(
        color = ArcadeSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = RetroMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currencyFormat.format(amount),
                color = color,
                fontSize = if (isBold) 14.sp else 12.sp,
                fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DocActionButton(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ArcadeSurface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = RetroWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitle,
                    color = RetroMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag(testTag)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
