package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConsoleOrder
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.PhysicalCondition
import com.example.ui.theme.ArcadeCardBorder
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
import com.example.ui.theme.SegaBlue
import com.example.ui.theme.SnesPurple
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableOrderCard(
    order: ConsoleOrder,
    onCardClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit,
    onAdvanceStatus: ((OrderStatus) -> Unit)? = null
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onMarkCompleted()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("swipeable_order_${order.id}"),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> PixelGreen1UP
                    SwipeToDismissBoxValue.EndToStart -> PixelRedHeart
                    else -> ArcadeSurfaceVariant
                },
                label = "swipe_bg_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completar",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "★ STAGE CLEAR / ENTREGAR ★",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GAME OVER / BORRAR",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    ) {
        OrderCard(
            order = order,
            onClick = onCardClick,
            onAdvanceStatus = onAdvanceStatus
        )
    }
}

@Composable
fun OrderCard(
    order: ConsoleOrder,
    onClick: () -> Unit,
    onAdvanceStatus: ((OrderStatus) -> Unit)? = null
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Retro console color styling & 90s badge
    val (brandColor, retroEraTag) = when {
        order.deviceModel.contains("SNES", ignoreCase = true) || order.deviceModel.contains("Super Nintendo", ignoreCase = true) || order.deviceModel.contains("Famicom", ignoreCase = true) ->
            Pair(SnesPurple, "16-BIT NINTENDO")
        order.deviceModel.contains("Genesis", ignoreCase = true) || order.deviceModel.contains("Mega Drive", ignoreCase = true) || order.deviceModel.contains("Sega", ignoreCase = true) ->
            Pair(SegaBlue, "16-BIT SEGA")
        order.deviceModel.contains("PlayStation", ignoreCase = true) || order.deviceModel.contains("PS1", ignoreCase = true) || order.deviceModel.contains("PSOne", ignoreCase = true) ->
            Pair(Color(0xFF003791), "32-BIT SONY")
        order.deviceModel.contains("N64", ignoreCase = true) || order.deviceModel.contains("Nintendo 64", ignoreCase = true) ->
            Pair(Color(0xFFE60012), "64-BIT NINTENDO")
        order.deviceModel.contains("Game Boy", ignoreCase = true) || order.deviceModel.contains("GBA", ignoreCase = true) ->
            Pair(GameBoyGreen, "PORTABLE RETRO")
        order.deviceModel.contains("Dreamcast", ignoreCase = true) ->
            Pair(Color(0xFFFF6600), "128-BIT SEGA")
        order.deviceModel.contains("Neo Geo", ignoreCase = true) ->
            Pair(RetroGoldCoin, "ARCADE AES")
        else -> Pair(NeonCyan, "RETRO SYSTEM")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, ArcadeCardBorder, RoundedCornerShape(14.dp))
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Retro Header: Era Tag + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(brandColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, brandColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (order.orderType == OrderType.SALE.name) Icons.Default.Sell else Icons.Default.VideogameAsset,
                            contentDescription = null,
                            tint = brandColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = order.deviceModel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = RetroWhite
                            )
                        }
                        Text(
                            text = "[$retroEraTag] ${if (order.serialNumber.isNotBlank()) "• S/N: ${order.serialNumber}" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = brandColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Retro status badge with arcade glowing border
                val statusEnum = OrderStatus.values().find { it.name == order.status } ?: OrderStatus.TO_FIX
                val (statusColor, retroLabel) = when (statusEnum) {
                    OrderStatus.TO_FIX -> Pair(PixelRedHeart, "⚡ INSERT COIN")
                    OrderStatus.IN_PROGRESS -> Pair(RetroGoldCoin, "⚙ STAGE 1")
                    OrderStatus.IN_STOCK -> Pair(NeonCyan, "★ READY")
                    OrderStatus.FOR_SALE -> Pair(PixelGreen1UP, "🛒 ON SALE")
                    OrderStatus.COMPLETED -> Pair(RetroMuted, "✔ CLEAR")
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = retroLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Client / Condition Row
            if (order.orderType == OrderType.SALE.name) {
                val condition = PhysicalCondition.values().find { it.name == order.physicalCondition } ?: PhysicalCondition.EXCELLENT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PixelGreen1UP.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PixelGreen1UP.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Restauración: ${condition.label}",
                            color = PixelGreen1UP,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = currencyFormat.format(order.finalPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NeonMagenta,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Player: ${order.clientName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RetroWhite
                        )
                    }

                    if (order.finalPrice > 0) {
                        Text(
                            text = currencyFormat.format(order.finalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = RetroGoldCoin
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Retro Diagnostic Terminal Box
            Surface(
                color = ArcadeSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (order.reportedFault.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "FALLA: ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = PixelRedHeart,
                                fontSize = 10.sp
                            )
                            Text(
                                text = order.reportedFault,
                                style = MaterialTheme.typography.bodySmall,
                                color = RetroMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (order.diagnosis.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "FIX RS: ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                fontSize = 10.sp
                            )
                            Text(
                                text = order.diagnosis,
                                style = MaterialTheme.typography.bodySmall,
                                color = RetroWhite,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (order.accessories.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📦 Items: ${order.accessories}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer row with date and next step quick button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVE: ${dateFormat.format(Date(order.entryDate))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroMuted,
                    fontSize = 10.sp
                )

                val nextStatus = when (order.status) {
                    OrderStatus.TO_FIX.name -> OrderStatus.IN_PROGRESS
                    OrderStatus.IN_PROGRESS.name -> OrderStatus.IN_STOCK
                    OrderStatus.IN_STOCK.name -> OrderStatus.COMPLETED
                    else -> null
                }

                if (nextStatus != null && onAdvanceStatus != null) {
                    Surface(
                        color = NeonCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier
                            .clickable { onAdvanceStatus(nextStatus) }
                            .testTag("advance_status_${order.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LEVEL UP ➔ ${nextStatus.tabTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
