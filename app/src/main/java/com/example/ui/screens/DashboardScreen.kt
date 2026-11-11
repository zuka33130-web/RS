package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.WorkOrderStatus
import com.example.data.model.WorkOrderWithClient
import com.example.ui.WorkshopViewModel
import com.example.ui.components.CrmWorkOrderCard
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

import androidx.compose.material.icons.filled.Analytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WorkshopViewModel,
    onOpenNewOrder: () -> Unit,
    onOpenCrmOrderDetail: (WorkOrderWithClient) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAnalytics: () -> Unit = {}
) {
    val crmOrders by viewModel.displayedCrmOrders.collectAsStateWithLifecycle()
    val allCrmOrders by viewModel.allCrmOrders.collectAsStateWithLifecycle()
    val selectedCrmTab by viewModel.selectedCrmTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showSearchBar by remember { mutableStateOf(false) }

    // Counts for exact 6 workflow states
    val toCheckCount = allCrmOrders.count { it.order.status == WorkOrderStatus.TO_CHECK.name }
    val quotedCount = allCrmOrders.count { it.order.status == WorkOrderStatus.QUOTED.name }
    val waitingPartsCount = allCrmOrders.count { it.order.status == WorkOrderStatus.WAITING_PARTS.name }
    val repairedCount = allCrmOrders.count { it.order.status == WorkOrderStatus.REPAIRED.name }
    val deliveredCount = allCrmOrders.count { it.order.status == WorkOrderStatus.DELIVERED.name }
    val underWarrantyCount = allCrmOrders.count { it.order.status == WorkOrderStatus.UNDER_WARRANTY.name }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Retro Arcade Banner & Header
            Surface(
                color = ArcadeDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Pixel Art Banner Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_retro_banner),
                            contentDescription = "Retro State RS Workshop Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay for readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ArcadeDark.copy(alpha = 0.85f),
                                            ArcadeDark
                                        )
                                    )
                                )
                        )

                        // Top controls row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = RetroGoldCoin,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "1P: READY",
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { showSearchBar = !showSearchBar },
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                        .border(1.dp, NeonMagenta, CircleShape)
                                        .size(36.dp)
                                        .testTag("search_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (showSearchBar) Icons.Default.Clear else Icons.Default.Search,
                                        contentDescription = "Buscar consola retro",
                                        tint = NeonMagenta,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onOpenAnalytics,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                        .border(1.dp, RetroGoldCoin, CircleShape)
                                        .size(36.dp)
                                        .testTag("analytics_top_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Inteligencia de Negocio y Retargeting",
                                        tint = RetroGoldCoin,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onOpenSettings,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                        .border(1.dp, NeonCyan, CircleShape)
                                        .size(36.dp)
                                        .testTag("settings_top_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Configuración y Backup",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Telemetry & Subheader
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RETRO STATE RS",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Servicio Técnico & Modding de Consolas Clásicas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RetroMuted,
                                    fontSize = 11.sp
                                )
                            }

                            // Mascot Avatar Mini
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, RetroGoldCoin, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_retro_mascot),
                                    contentDescription = "Fox Mascot",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Search Bar
                        if (showSearchBar) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                placeholder = { Text("Buscar SNES, Genesis, mod RGB, cliente o S/N...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_text_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar", tint = NeonMagenta)
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = ArcadeCardBorder,
                                    focusedContainerColor = ArcadeSurface,
                                    unfocusedContainerColor = ArcadeSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Retro Telemetry Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RetroPill(
                                label = "1. REVISAR",
                                count = toCheckCount,
                                color = PixelRedHeart,
                                modifier = Modifier.weight(1f)
                            )
                            RetroPill(
                                label = "2. PRESUPUESTO",
                                count = quotedCount,
                                color = RetroGoldCoin,
                                modifier = Modifier.weight(1f)
                            )
                            RetroPill(
                                label = "3. REPUESTO",
                                count = waitingPartsCount,
                                color = NeonMagenta,
                                modifier = Modifier.weight(1f)
                            )
                            RetroPill(
                                label = "4. REPARADA",
                                count = repairedCount,
                                color = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Segmented Tabs by 6 Exact Workflow States
            ScrollableTabRow(
                selectedTabIndex = selectedCrmTab,
                edgePadding = 12.dp,
                containerColor = ArcadeSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCrmTab]),
                        color = NeonCyan,
                        height = 3.dp
                    )
                }
            ) {
                viewModel.crmStatusTabs.forEachIndexed { index, status ->
                    val count = when (status) {
                        WorkOrderStatus.TO_CHECK -> toCheckCount
                        WorkOrderStatus.QUOTED -> quotedCount
                        WorkOrderStatus.WAITING_PARTS -> waitingPartsCount
                        WorkOrderStatus.REPAIRED -> repairedCount
                        WorkOrderStatus.DELIVERED -> deliveredCount
                        WorkOrderStatus.UNDER_WARRANTY -> underWarrantyCount
                    }

                    val tabColor = when (status) {
                        WorkOrderStatus.TO_CHECK -> PixelRedHeart
                        WorkOrderStatus.QUOTED -> RetroGoldCoin
                        WorkOrderStatus.WAITING_PARTS -> NeonMagenta
                        WorkOrderStatus.REPAIRED -> NeonCyan
                        WorkOrderStatus.DELIVERED -> PixelGreen1UP
                        WorkOrderStatus.UNDER_WARRANTY -> GameBoyGreen
                    }

                    Tab(
                        selected = selectedCrmTab == index,
                        onClick = { viewModel.selectCrmTab(index) },
                        modifier = Modifier.testTag("dashboard_tab_$index"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${status.stageNumber}. ${status.displayName}",
                                    fontWeight = if (selectedCrmTab == index) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selectedCrmTab == index) tabColor else RetroMuted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (selectedCrmTab == index) tabColor.copy(alpha = 0.2f) else ArcadeSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCrmTab == index) tabColor else ArcadeCardBorder)
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (selectedCrmTab == index) tabColor else RetroMuted,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Arcade Gesture Guidance
            Surface(
                color = ArcadeDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕹 TOCÁ UNA ORDEN PARA VER FICHA CRM • ENVIAR WHATSAPP • CAMBIAR ESTADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroGoldCoin,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Orders List
            if (crmOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_retro_mascot),
                            contentDescription = "No orders",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "¡NO HIT! Sin resultados para '$searchQuery'"
                                   else "¡ZONA DESPEJADA EN ESTE ESTADO!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Presioná el botón '+' para registrar una nueva orden con cliente (CRM) o cambiar de estado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = RetroMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(crmOrders, key = { it.order.id }) { orderWithClient ->
                        CrmWorkOrderCard(
                            orderWithClient = orderWithClient,
                            onClick = { onOpenCrmOrderDetail(orderWithClient) }
                        )
                    }
                }
            }
        }

        // Retro Floating Action Button
        FloatingActionButton(
            onClick = onOpenNewOrder,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_order_fab"),
            containerColor = NeonMagenta,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nuevo Equipo Retro"
            )
        }
    }
}

@Composable
fun RetroPill(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
