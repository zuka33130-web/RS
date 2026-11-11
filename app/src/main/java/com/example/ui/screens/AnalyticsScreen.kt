package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RetargetingCandidate
import com.example.ui.analytics.AnalyticsViewModel
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
import com.example.util.WhatsAppSender
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val topConsoles by viewModel.topDeviceModels.collectAsStateWithLifecycle()
    val topProfitFaults by viewModel.topProfitFaults.collectAsStateWithLifecycle()
    val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val retargetingCandidates by viewModel.retargetingCandidates.collectAsStateWithLifecycle()

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    val incomeTotal = financialSummary.find { it.type == "INCOME" }?.totalAmount ?: 0.0
    val expenseTotal = financialSummary.find { it.type == "EXPENSE" }?.totalAmount ?: 0.0
    val netBalance = incomeTotal - expenseTotal

    val maxConsoleCount = topConsoles.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDark)
            .padding(16.dp)
            .testTag("analytics_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = NeonCyan)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "INTELIGENCIA DE NEGOCIO (KPIs)",
                    color = NeonCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Estadísticas del taller y retargeting automático de clientes",
                    color = RetroMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {

            // --- SECCIÓN 1: BALANCE FINANCIERO MENSUAL ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                    border = BorderStroke(1.dp, RetroGoldCoin.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = RetroGoldCoin, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BALANCE GENERAL DEL TALLER",
                                color = RetroGoldCoin,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCard(
                                label = "Total Ingresos",
                                amount = currencyFormat.format(incomeTotal),
                                color = PixelGreen1UP,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                label = "Total Gastos",
                                amount = currencyFormat.format(expenseTotal),
                                color = PixelRedHeart,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = ArcadeSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (netBalance >= 0) PixelGreen1UP else PixelRedHeart),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("MARGEN DE GANANCIA NETO:", color = RetroWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = currencyFormat.format(netBalance),
                                    color = if (netBalance >= 0) PixelGreen1UP else PixelRedHeart,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // --- SECCIÓN 2: CONSOLA MÁS INGRESADA ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONSOLAS MÁS INGRESADAS (VOLUMEN)",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (topConsoles.isEmpty()) {
                            Text("[ Sin datos de órdenes de consola ]", color = RetroMuted, fontSize = 11.sp)
                        } else {
                            topConsoles.forEach { console ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(console.deviceModel, color = RetroWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${console.count} equipos", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { console.count.toFloat() / maxConsoleCount },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = NeonCyan,
                                        trackColor = ArcadeSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- SECCIÓN 3: REPARACIÓN MÁS RENTABLE ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                    border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REPARACIONES CON MAYOR MARGEN",
                                color = NeonMagenta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (topProfitFaults.isEmpty()) {
                            Text("[ Sin datos de rentabilidad acumulada ]", color = RetroMuted, fontSize = 11.sp)
                        } else {
                            topProfitFaults.forEach { stat ->
                                Surface(
                                    color = ArcadeSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(stat.reportedFault.take(35), color = RetroWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${stat.count} reparaciones realizadas", color = RetroMuted, fontSize = 10.sp)
                                        }
                                        Text(
                                            currencyFormat.format(stat.totalProfit),
                                            color = RetroGoldCoin,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SECCIÓN 4: RETARGETING AUTOMÁTICO ("SIDE QUESTS") ---
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PixelGreen1UP, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SIDE QUESTS (RETARGETING MANTENIMIENTO)",
                            color = PixelGreen1UP,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Clientes con equipos entregados hace 6+ meses sugeridos para servicio preventivo",
                            color = RetroMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (retargetingCandidates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(ArcadeSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, ArcadeCardBorder, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[ No hay solicitudes pendientes de retargeting en este período ]",
                            color = RetroMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                items(retargetingCandidates) { candidate ->
                    RetargetingCard(candidate = candidate)
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ArcadeSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = RetroMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(amount, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RetargetingCard(candidate: RetargetingCandidate) {
    val context = LocalContext.current
    val order = candidate.workOrderWithClient.order
    val client = candidate.workOrderWithClient.client

    Card(
        colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
        border = BorderStroke(1.5.dp, PixelGreen1UP),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONSOLA: ${order.deviceModel}",
                    color = PixelGreen1UP,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )

                Surface(
                    color = PixelGreen1UP.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, PixelGreen1UP)
                ) {
                    Text(
                        text = "Entregada hace ${candidate.monthsSinceDelivery} meses",
                        color = PixelGreen1UP,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Cliente: ${client?.fullName ?: "Consola en Salón"}", color = RetroWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Sugerencia: ${candidate.suggestedAction}", color = RetroMuted, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val clientPhone = client?.phone.orEmpty()
                    if (clientPhone.isNotBlank()) {
                        val clientName = client?.firstName ?: "Cliente"
                        val device = order.deviceModel
                        val message = "Hola $clientName! Somos del servicio técnico Retro State RS. Hace unos meses reparamos tu $device. Para cuidar tu inversión y evitar sobrecalentamientos o desgaste térmico, te sugerimos realizar un mantenimiento preventivo de pasta térmica y limpieza interna en el taller. ¡Avisanos si querés coordinar un turno!"
                        WhatsAppSender.sendToWhatsApp(context, clientPhone, message)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelGreen1UP,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_retargeting_whatsapp")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ENVIAR ALERTA RETARGETING POR WHATSAPP", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
