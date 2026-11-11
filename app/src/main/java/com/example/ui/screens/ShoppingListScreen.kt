package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SupplyItem
import com.example.ui.daily.DailyManagementViewModel
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
import java.text.NumberFormat
import java.util.Locale

// Common workshop supplies for instant 1-tap addition
private val WORKSHOP_PRESETS = listOf(
    "Alcohol isopropílico",
    "Estaño 60/40",
    "Flux Amtech NC-559",
    "Pasta térmica MX-4",
    "Malla desoldante",
    "Repuestos PS4 (Láser KEM-490)",
    "Pilas CR2032 con pines",
    "Capacitores 100uF 25V",
    "Cables AWG 30 modding",
    "Fusibles SMD 0603"
)

@Composable
fun ShoppingListScreen(
    viewModel: DailyManagementViewModel,
    onOpenDetailedDialog: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var quickInputText by remember { mutableStateOf("") }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "AR")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDark)
            .testTag("shopping_list_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- 1. Header with Stats ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LISTA DE COMPRAS E INSUMOS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Faltan ${uiState.neededSuppliesCount} insumos • ${uiState.inStockSuppliesCount} comprados en stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.neededSuppliesCount > 0) RetroGoldCoin else PixelGreen1UP,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- 2. Quick Add Bar (Min-touch design for busy hands) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AÑADIR INSUMO RÁPIDO (1 TOQUE)",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickInputText,
                                onValueChange = { quickInputText = it },
                                placeholder = {
                                    Text(
                                        "ej. Alcohol isopropílico, Estaño...",
                                        color = RetroMuted,
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (quickInputText.isNotBlank()) {
                                        viewModel.quickAddPresetSupply(quickInputText.trim())
                                        quickInputText = ""
                                        focusManager.clearFocus()
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = ArcadeCardBorder,
                                    focusedTextColor = RetroWhite,
                                    unfocusedTextColor = RetroWhite,
                                    focusedContainerColor = ArcadeSurfaceVariant,
                                    unfocusedContainerColor = ArcadeSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_supply_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (quickInputText.isNotBlank()) {
                                        viewModel.quickAddPresetSupply(quickInputText.trim())
                                        quickInputText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = quickInputText.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .size(width = 52.dp, height = 52.dp)
                                    .testTag("quick_supply_add_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast Presets Scroll (Zero-typing addition)
                        Text(
                            text = "Frecuentes del taller (tocá para sumar):",
                            color = RetroMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WORKSHOP_PRESETS.forEach { preset ->
                                Surface(
                                    color = ArcadeSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, ArcadeCardBorder),
                                    modifier = Modifier.clickable {
                                        viewModel.quickAddPresetSupply(preset)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = preset,
                                            color = RetroWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. Filter Chips ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.shoppingFilter == "NEEDED",
                        onClick = { viewModel.setShoppingFilter("NEEDED") },
                        label = { Text("🛒 Por Comprar (${uiState.neededSuppliesCount})", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PixelRedHeart,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_needed")
                    )
                    FilterChip(
                        selected = uiState.shoppingFilter == "BOUGHT",
                        onClick = { viewModel.setShoppingFilter("BOUGHT") },
                        label = { Text("✔ Comprados (${uiState.inStockSuppliesCount})", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PixelGreen1UP,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_bought")
                    )
                    FilterChip(
                        selected = uiState.shoppingFilter == "ALL",
                        onClick = { viewModel.setShoppingFilter("ALL") },
                        label = { Text("Todos", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )
                }
            }

            // --- 4. Empty State ---
            if (uiState.shoppingList.isEmpty()) {
                item {
                    Surface(
                        color = ArcadeSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ArcadeCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = PixelGreen1UP,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "¡TODO EL STOCK AL DÍA!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = PixelGreen1UP
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No hay insumos pendientes en esta sección. Escribe arriba para anotar repuestos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = RetroMuted
                            )
                        }
                    }
                }
            } else {
                // --- 5. Shopping Items List ---
                items(uiState.shoppingList, key = { it.id }) { item ->
                    val isBought = !item.isNeeded

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isBought) ArcadeCardBorder else NeonCyan.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("shopping_item_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBought) ArcadeSurface.copy(alpha = 0.5f) else ArcadeSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox to mark as bought/needed
                            Checkbox(
                                checked = isBought,
                                onCheckedChange = { viewModel.toggleShoppingItem(item) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PixelGreen1UP,
                                    uncheckedColor = NeonCyan,
                                    checkmarkColor = Color.Black
                                ),
                                modifier = Modifier.testTag("checkbox_supply_${item.id}")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isBought) FontWeight.Normal else FontWeight.Bold,
                                    color = if (isBought) RetroMuted else RetroWhite,
                                    textDecoration = if (isBought) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (item.quantity > 1) {
                                        Text(
                                            text = "Cant: ${item.quantity} ${item.unit}",
                                            color = RetroGoldCoin,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (item.estimatedPrice > 0) {
                                        Text(
                                            text = "~${currencyFormat.format(item.estimatedPrice)}",
                                            color = PixelGreen1UP,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (item.category.isNotBlank()) {
                                        Surface(
                                            color = ArcadeSurfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.category,
                                                color = NeonMagenta,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteShoppingItem(item) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_supply_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar insumo",
                                    tint = RetroMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB to open detailed supply dialog
        FloatingActionButton(
            onClick = onOpenDetailedDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_supply_fab"),
            containerColor = RetroGoldCoin,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.AddShoppingCart,
                contentDescription = "Nuevo Insumo Detallado"
            )
        }
    }
}
