package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FinancialTransaction
import com.example.data.model.TransactionType
import com.example.ui.finance.FinanceUiState
import com.example.ui.finance.FinanceViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    viewModel: FinanceViewModel,
    onOpenIncomeDialog: () -> Unit,
    onOpenExpenseDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    Box(modifier = Modifier.fillMaxSize().background(ArcadeDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = RetroGoldCoin,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BILLETERA RETRO RS",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Control de caja, ingresos y gastos del taller estilo Mercado Pago",
                            style = MaterialTheme.typography.bodySmall,
                            color = RetroMuted
                        )
                    }
                }
            }

            // 1. Tarjeta Principal (Header): Saldo Total en grande + 2 botones rápidos ("Ingreso" y "Gasto")
            item {
                BalanceCard(
                    uiState = uiState,
                    currencyFormat = currencyFormat,
                    onToggleVisibility = { viewModel.toggleBalanceVisibility() },
                    onIncomeClick = onOpenIncomeDialog,
                    onExpenseClick = onOpenExpenseDialog
                )
            }

            // 2. Resumen del Mes: Fila con dos indicadores (Total Ingresos verde, Total Egresos rojo)
            item {
                MonthlySummaryRow(
                    uiState = uiState,
                    currencyFormat = currencyFormat
                )
            }

            // Selector de Período
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedPeriod == "ESTE_MES",
                        onClick = { viewModel.setPeriod("ESTE_MES") },
                        label = { Text("Este mes", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = uiState.selectedPeriod == "MES_ANTERIOR",
                        onClick = { viewModel.setPeriod("MES_ANTERIOR") },
                        label = { Text("Mes anterior", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = uiState.selectedPeriod == "TODOS",
                        onClick = { viewModel.setPeriod("TODOS") },
                        label = { Text("Todo el historial", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // Desglose de Gastos por Categoría
            if (uiState.expensesByCategory.isNotEmpty()) {
                item {
                    CategorizedExpensesCard(
                        expensesByCategory = uiState.expensesByCategory,
                        totalExpense = uiState.monthlyExpense,
                        currencyFormat = currencyFormat
                    )
                }
            }

            // 3. Título de Historial
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ÚLTIMOS MOVIMIENTOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = RetroGoldCoin
                    )
                    Text(
                        text = "${uiState.transactions.size} operaciones",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroMuted
                    )
                }
            }

            // Lista de Movimientos (LazyColumn items)
            if (uiState.transactions.isEmpty()) {
                item {
                    EmptyTransactionsCard()
                }
            } else {
                items(uiState.transactions, key = { it.id }) { tx ->
                    TransactionRow(
                        transaction = tx,
                        showBalance = uiState.showBalance,
                        currencyFormat = currencyFormat,
                        onDelete = { viewModel.deleteTransaction(tx) }
                    )
                }
            }
        }

        // FAB adicional de carga rápida
        FloatingActionButton(
            onClick = onOpenIncomeDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_transaction_fab"),
            containerColor = PixelGreen1UP,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nuevo Movimiento"
            )
        }
    }
}

// --- Componentes Modulares de UI ---

/**
 * 1. Tarjeta Principal con Saldo Disponible y Botones Rápidos (Ingreso / Gasto)
 */
@Composable
fun BalanceCard(
    uiState: FinanceUiState,
    currencyFormat: NumberFormat,
    onToggleVisibility: () -> Unit,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mercadopago_balance_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF260A4E),
                            Color(0xFF140A2E),
                            Color(0xFF0C0D17)
                        )
                    )
                )
                .border(1.5.dp, NeonCyan, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = RetroGoldCoin,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DINERO DISPONIBLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = RetroGoldCoin,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp).testTag("toggle_balance_visibility")
                    ) {
                        Icon(
                            imageVector = if (uiState.showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Mostrar/Ocultar Saldo",
                            tint = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Saldo Total en grande
                Text(
                    text = if (uiState.showBalance) currencyFormat.format(uiState.totalBalance) else "$ ••••••••",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = RetroWhite
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Dos botones de acción rápida debajo del saldo: Ingreso y Gasto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Ingreso (Verde con Icono Add)
                    Button(
                        onClick = onIncomeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_income_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelGreen1UP,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ingreso",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ingreso", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }

                    // Botón Gasto (Rojo con Icono Remove)
                    Button(
                        onClick = onExpenseClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_expense_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelRedHeart,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Gasto",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gasto", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/**
 * 2. Resumen del Mes: Fila con dos indicadores visuales (Total Ingresos verde, Total Egresos rojo)
 */
@Composable
fun MonthlySummaryRow(
    uiState: FinanceUiState,
    currencyFormat: NumberFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Indicador Ingresos del Mes (Verde)
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, PixelGreen1UP.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PixelGreen1UP.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = PixelGreen1UP,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total Ingresos",
                        style = MaterialTheme.typography.labelMedium,
                        color = PixelGreen1UP,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (uiState.showBalance) currencyFormat.format(uiState.monthlyIncome) else "$ ••••••",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = PixelGreen1UP
                )
                Text(
                    text = uiState.periodLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroMuted,
                    fontSize = 10.sp
                )
            }
        }

        // Indicador Egresos del Mes (Rojo)
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, PixelRedHeart.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PixelRedHeart.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = PixelRedHeart,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total Egresos",
                        style = MaterialTheme.typography.labelMedium,
                        color = PixelRedHeart,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (uiState.showBalance) currencyFormat.format(uiState.monthlyExpense) else "$ ••••••",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = PixelRedHeart
                )
                Text(
                    text = uiState.periodLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * 3. Fila de Transacción (TransactionRow)
 * Muestra: Título, Fecha, Categoría (icono) y Monto (verde si es ingreso, rojo si es egreso)
 */
@Composable
fun TransactionRow(
    transaction: FinancialTransaction,
    showBalance: Boolean,
    currencyFormat: NumberFormat,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val isIncome = transaction.type == TransactionType.INCOME.name

    val categoryIcon = when (transaction.category) {
        "Reparación Retro", "Cobro Reparación" -> Icons.Default.Build
        "Venta Consola Clásica", "Venta Consola" -> Icons.Default.Sell
        "Capacitores & ICs", "Repuestos" -> Icons.Default.Memory
        "Lentes Ópticos" -> Icons.Default.Stars
        else -> Icons.Default.Paid
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcadeCardBorder, RoundedCornerShape(12.dp))
            .testTag("transaction_row_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Categoría con icono
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isIncome) PixelGreen1UP.copy(alpha = 0.15f) else PixelRedHeart.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = transaction.category,
                        tint = if (isIncome) PixelGreen1UP else PixelRedHeart,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Título y Fecha
                Column {
                    Text(
                        text = transaction.description.ifBlank { transaction.category },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RetroWhite
                    )
                    Text(
                        text = "${transaction.category} • ${dateFormat.format(Date(transaction.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Monto (verde con + si es ingreso, rojo con - si es egreso)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (showBalance) {
                        "${if (isIncome) "+ " else "- "}${currencyFormat.format(transaction.amount)}"
                    } else "$ ••••••",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isIncome) PixelGreen1UP else PixelRedHeart
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_tx_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = RetroMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategorizedExpensesCard(
    expensesByCategory: List<Pair<String, Double>>,
    totalExpense: Double,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcadeCardBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "DESGLOSE DE GASTOS POR CATEGORÍA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(10.dp))

            expensesByCategory.forEach { (category, amount) ->
                val percent = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = RetroWhite
                        )
                        Text(
                            text = "${currencyFormat.format(amount)} (${(percent * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PixelRedHeart
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PixelRedHeart,
                        trackColor = ArcadeSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsCard() {
    Surface(
        color = ArcadeSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = RetroMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sin movimientos registrados en este período",
                style = MaterialTheme.typography.bodyMedium,
                color = RetroMuted
            )
        }
    }
}
