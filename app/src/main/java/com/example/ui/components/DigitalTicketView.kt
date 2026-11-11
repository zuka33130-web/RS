package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.WorkOrderEntity
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.PixelGreen1UP
import com.example.ui.theme.RetroGoldCoin
import com.example.ui.theme.RetroMuted
import com.example.ui.theme.RetroWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable que renderiza el Ticket Digital de Ingreso con estética retro arcade.
 */
@Composable
fun DigitalTicketView(
    order: WorkOrderEntity,
    client: ClientEntity?,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(order.entryDate))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("digital_ticket_view"),
        colors = CardDefaults.cardColors(containerColor = ArcadeDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, NeonCyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo & Banner
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RETRO STATE RS",
                    color = NeonCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "TALLER & LABORATORIO TÉCNICO",
                color = RetroMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -",
                color = ArcadeCardBorder,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "TICKET DIGITAL DE INGRESO",
                color = RetroGoldCoin,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -",
                color = ArcadeCardBorder,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Datos Clave
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TicketLine(label = "CÓDIGO DE ORDEN:", value = order.orderCode, valueColor = NeonCyan, isBold = true)
                TicketLine(label = "FECHA DE INGRESO:", value = formattedDate)
                TicketLine(label = "CLIENTE:", value = client?.fullName ?: "Consola en Salón")
                TicketLine(label = "TELÉFONO:", value = client?.phone?.ifBlank { "N/D" } ?: "N/D")
                TicketLine(label = "CONSOLA / EQUIPO:", value = order.deviceModel, valueColor = NeonMagenta, isBold = true)
                if (order.serialNumber.isNotBlank()) {
                    TicketLine(label = "N° DE SERIE (S/N):", value = order.serialNumber)
                }
                TicketLine(label = "FALLA REPORTADA:", value = order.reportedFault)
                if (order.photosUris.isNotEmpty()) {
                    TicketLine(label = "EVIDENCIA FÍSICA:", value = "${order.photosUris.size} foto(s) registrada(s)", valueColor = PixelGreen1UP)
                }
                if (!order.signatureUri.isNullOrBlank()) {
                    TicketLine(label = "FIRMA DIGITAL:", value = "Registrada en conformidad", valueColor = PixelGreen1UP)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "==========================================",
                color = NeonCyan.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "CONSERVE ESTE TICKET DIGITAL\nPARA RETIRAR SU EQUIPO",
                color = RetroWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "==========================================",
                color = NeonCyan.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Garantía Oficial Retro State RS sobre mano de obra y repuestos sustituidos.",
                color = RetroMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TicketLine(
    label: String,
    value: String,
    valueColor: Color = RetroWhite,
    isBold: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = RetroMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Normal
        )
    }
}
