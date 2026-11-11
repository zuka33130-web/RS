package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.SupplyItem
import com.example.data.model.TaskPriority
import com.example.data.model.TransactionType
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroMuted
import com.example.ui.theme.RetroWhite

private fun String.toDecimalOrNull(): Double? =
    trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

@Composable
fun NewTransactionDialog(
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onSave: (type: TransactionType, amount: Double, category: String, description: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var amountInput by remember { mutableStateOf("") }
    var category by remember(initialType) {
        mutableStateOf(if (initialType == TransactionType.INCOME) "Cobro Reparación" else "Insumos Taller")
    }
    var description by remember { mutableStateOf("") }
    val amount = amountInput.toDecimalOrNull()
    val isValid = amount != null && amount > 0.0 && category.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArcadeSurface,
        title = { Text("Nuevo movimiento", color = RetroWhite) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = {
                                type = option
                                category = if (option == TransactionType.INCOME) "Cobro Reparación" else "Insumos Taller"
                            },
                            label = { Text(if (option == TransactionType.INCOME) "Ingreso" else "Gasto") },
                            modifier = Modifier.testTag("transaction_type_${option.name}")
                        )
                    }
                }
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Importe *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amountInput.isNotBlank() && (amount == null || amount <= 0.0),
                    supportingText = {
                        if (amountInput.isNotBlank() && (amount == null || amount <= 0.0)) {
                            Text("Ingresá un importe mayor que cero")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_transaction_amount")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_transaction_category")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_transaction_description")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validAmount = amount
                    if (validAmount != null && isValid) {
                        onSave(type, validAmount, category.trim(), description.trim())
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("btn_save_transaction")
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = RetroMuted) }
        }
    )
}

@Composable
fun NewTaskDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, priority: TaskPriority, relatedConsole: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var relatedConsole by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArcadeSurface,
        title = { Text("Nueva tarea", color = RetroWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tarea *") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )
                Text("Prioridad", color = RetroMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskPriority.entries.forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option.label) },
                            modifier = Modifier.testTag("task_priority_${option.name}")
                        )
                    }
                }
                OutlinedTextField(
                    value = relatedConsole,
                    onValueChange = { relatedConsole = it },
                    label = { Text("Consola o área relacionada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_console")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title.trim(), priority, relatedConsole.trim()) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("btn_save_task")
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = RetroMuted) }
        }
    )
}

@Composable
fun NewSupplyDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        quantity: Int,
        unit: String,
        isNeeded: Boolean,
        estimatedPrice: Double,
        supplier: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Consumibles") }
    var quantityInput by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("u") }
    var isNeeded by remember { mutableStateOf(true) }
    var priceInput by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }

    val quantity = quantityInput.toIntOrNull()
    val price = priceInput.toDecimalOrNull() ?: 0.0
    val isPriceValid = priceInput.isBlank() || (priceInput.toDecimalOrNull()?.let { it >= 0.0 } == true)
    val isValid = name.isNotBlank() && category.isNotBlank() && unit.isNotBlank() &&
        quantity != null && quantity > 0 && isPriceValid

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArcadeSurface,
        title = { Text("Nuevo insumo", color = RetroWhite) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_supply_name")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_supply_category")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Cantidad *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = quantityInput.isNotBlank() && (quantity == null || quantity <= 0),
                        modifier = Modifier.weight(1f).testTag("input_supply_quantity")
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_supply_unit")
                    )
                }
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Precio estimado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = !isPriceValid,
                    modifier = Modifier.fillMaxWidth().testTag("input_supply_price")
                )
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Proveedor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_supply_supplier")
                )
                FilterChip(
                    selected = isNeeded,
                    onClick = { isNeeded = !isNeeded },
                    label = { Text(if (isNeeded) "Pendiente de compra" else "Disponible en stock") },
                    modifier = Modifier.testTag("input_supply_needed")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validQuantity = quantity
                    if (validQuantity != null && isValid) {
                        onSave(
                            name.trim(), category.trim(), validQuantity, unit.trim(),
                            isNeeded, price, supplier.trim()
                        )
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("btn_save_supply")
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = RetroMuted) }
        }
    )
}
