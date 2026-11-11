package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SupplyItem
import com.example.ui.WorkshopViewModel
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PixelGreen1UP
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroGoldCoin
import com.example.ui.theme.RetroMuted
import com.example.ui.theme.RetroWhite
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SuppliesScreen(
    viewModel: WorkshopViewModel,
    onOpenNewSupply: () -> Unit
) {
    val supplies by viewModel.allSupplies.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("NEEDED") }

    val neededCount = supplies.count { it.isNeeded }
    val inStockCount = supplies.count { !it.isNeeded }

    val filteredSupplies = supplies.filter {
        when (selectedFilter) {
            "NEEDED" -> it.isNeeded
            "IN_STOCK" -> !it.isNeeded
            else -> true
        }
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REPUESTOS & INSUMOS RETRO",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Capacitores, lentes ópticos, flux y piezas clásicas",
                            style = MaterialTheme.typography.bodySmall,
                            color = RetroMuted
                        )
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "NEEDED",
                        onClick = { selectedFilter = "NEEDED" },
                        label = { Text("🛒 Por Comprar ($neededCount)", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PixelRedHeart,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "IN_STOCK",
                        onClick = { selectedFilter = "IN_STOCK" },
                        label = { Text("✔ En Stock ($inStockCount)", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PixelGreen1UP,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Todos", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // Empty state
            if (filteredSupplies.isEmpty()) {
                item {
                    Surface(
                        color = ArcadeSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedFilter == "NEEDED") "¡INVENTARIO COMPLETO!" else "Sin insumos registrados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Podés añadir nuevos repuestos o capacitores con el botón '+'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = RetroMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredSupplies, key = { it.id }) { item ->
                    RetroSupplyCard(
                        supply = item,
                        currencyFormat = currencyFormat,
                        onToggleNeeded = { viewModel.toggleSupplyNeeded(item) },
                        onDelete = { viewModel.deleteSupply(item) }
                    )
                }
            }
        }

        // FAB to add supply
        FloatingActionButton(
            onClick = onOpenNewSupply,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_supply_fab"),
            containerColor = RetroGoldCoin,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.AddShoppingCart,
                contentDescription = "Nuevo Insumo Retro"
            )
        }
    }
}

@Composable
fun RetroSupplyCard(
    supply: SupplyItem,
    currencyFormat: NumberFormat,
    onToggleNeeded: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryIcon = when (supply.category) {
        "Químicos" -> Icons.Default.Science
        "Repuestos" -> Icons.Default.Memory
        else -> Icons.Default.Handyman
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcadeCardBorder, RoundedCornerShape(14.dp))
            .testTag("supply_card_${supply.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (supply.isNeeded) PixelRedHeart.copy(alpha = 0.15f) else PixelGreen1UP.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = if (supply.isNeeded) PixelRedHeart else PixelGreen1UP,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = supply.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = RetroWhite
                        )
                        Text(
                            text = "${supply.category} • Cant: ${supply.quantity} ${supply.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp).testTag("delete_supply_${supply.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = RetroMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (supply.supplier.isNotBlank() || supply.estimatedPrice > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (supply.supplier.isNotBlank()) {
                        Text(
                            text = "Proveedor: ${supply.supplier}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroMuted
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (supply.estimatedPrice > 0) {
                        Text(
                            text = "Precio: ${currencyFormat.format(supply.estimatedPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RetroGoldCoin
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (supply.isNeeded) {
                Button(
                    onClick = onToggleNeeded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("toggle_supply_bought_${supply.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGreen1UP),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RECIBIDO / EN STOCK (ITEM OBTENIDO)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            } else {
                OutlinedButton(
                    onClick = onToggleNeeded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("toggle_supply_needed_${supply.id}"),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PixelRedHeart)
                ) {
                    Text("MARCAR AGOTADO (REQUERIDO)", fontSize = 11.sp, color = PixelRedHeart, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
