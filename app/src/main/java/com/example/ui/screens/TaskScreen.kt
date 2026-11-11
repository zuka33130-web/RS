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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskPriority
import com.example.data.model.WorkshopTask
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

// Common fast-dispatch repair tasks for technicians with busy hands
private val QUICK_TASK_PRESETS = listOf(
    Pair("🔧 Recap condensadores", TaskPriority.HIGH),
    Pair("🔥 Cambio pasta térmica MX-4", TaskPriority.MEDIUM),
    Pair("🎮 Calibrar lente óptico", TaskPriority.HIGH),
    Pair("⚡ Instalar mod HDMI / RGB", TaskPriority.HIGH),
    Pair("🔌 Reparar puerto de carga / HDMI", TaskPriority.HIGH),
    Pair("🧼 Limpieza ultrasónica placa", TaskPriority.LOW),
    Pair("🔋 Cambiar pila CR2032 cartucho", TaskPriority.LOW)
)

@Composable
fun TaskScreen(
    viewModel: DailyManagementViewModel,
    onOpenNewTask: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDark)
            .testTag("task_screen")
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
                            text = "GESTOR DE TAREAS & PRIORIDADES",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${uiState.pendingTasksCount} pendientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = RetroWhite,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.highPriorityTasksCount > 0) {
                                Text(
                                    text = " • ${uiState.highPriorityTasksCount} URGENTES (ALTA)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelRedHeart,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = " • ${uiState.completedTasksCount} listas",
                                style = MaterialTheme.typography.bodySmall,
                                color = PixelGreen1UP,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- 2. Quick Presets Card (1-tap creation for busy hands) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ArcadeCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = RetroGoldCoin,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AGREGAR CON 1 TOQUE (TAREAS FRECUENTES)",
                                color = RetroGoldCoin,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QUICK_TASK_PRESETS.forEach { (title, priority) ->
                                val chipBorder = when (priority) {
                                    TaskPriority.HIGH -> PixelRedHeart
                                    TaskPriority.MEDIUM -> RetroGoldCoin
                                    TaskPriority.LOW -> PixelGreen1UP
                                }
                                Surface(
                                    color = ArcadeSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, chipBorder.copy(alpha = 0.7f)),
                                    modifier = Modifier.clickable {
                                        viewModel.quickAddPresetTask(title, priority)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = chipBorder,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = title,
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
                        selected = uiState.taskFilter == "PENDING",
                        onClick = { viewModel.setTaskFilter("PENDING") },
                        label = { Text("Pendientes (${uiState.pendingTasksCount})", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_task_pending")
                    )
                    FilterChip(
                        selected = uiState.taskFilter == "HIGH",
                        onClick = { viewModel.setTaskFilter("HIGH") },
                        label = { Text("🔥 Alta (${uiState.highPriorityTasksCount})", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PixelRedHeart,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_task_high")
                    )
                    FilterChip(
                        selected = uiState.taskFilter == "COMPLETED",
                        onClick = { viewModel.setTaskFilter("COMPLETED") },
                        label = { Text("Listas (${uiState.completedTasksCount})", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PixelGreen1UP,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_task_completed")
                    )
                    FilterChip(
                        selected = uiState.taskFilter == "ALL",
                        onClick = { viewModel.setTaskFilter("ALL") },
                        label = { Text("Todas", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArcadeSurfaceVariant,
                            selectedLabelColor = RetroWhite
                        ),
                        modifier = Modifier.testTag("filter_task_all")
                    )
                }
            }

            // --- 4. Empty State ---
            if (uiState.tasks.isEmpty()) {
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
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = PixelGreen1UP,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "¡BANCO DE TRABAJO LIBRE!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = PixelGreen1UP
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No hay tareas pendientes en este filtro. Usa el botón '+' o los accesos rápidos para anotar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = RetroMuted
                            )
                        }
                    }
                }
            } else {
                // --- 5. Task List Items ---
                items(uiState.tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        // FAB to add new task
        FloatingActionButton(
            onClick = onOpenNewTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_task_fab"),
            containerColor = NeonCyan,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nueva Tarea"
            )
        }
    }
}

@Composable
fun TaskCard(
    task: WorkshopTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityEnum = TaskPriority.values().find { it.name == task.priority } ?: TaskPriority.MEDIUM
    // Visual indicators: Alta (Rojo), Media (Amarillo), Baja (Verde)
    val (priorityColor, priorityLabel) = when (priorityEnum) {
        TaskPriority.HIGH -> Pair(PixelRedHeart, "ALTA")
        TaskPriority.MEDIUM -> Pair(RetroGoldCoin, "MEDIA")
        TaskPriority.LOW -> Pair(PixelGreen1UP, "BAJA")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.isCompleted) ArcadeCardBorder else priorityColor.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) ArcadeSurface.copy(alpha = 0.6f) else ArcadeSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox to mark task as completed
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PixelGreen1UP,
                    uncheckedColor = priorityColor,
                    checkmarkColor = Color.Black
                ),
                modifier = Modifier.testTag("checkbox_task_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                    color = if (task.isCompleted) RetroMuted else RetroWhite,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority Badge: Alta (Rojo), Media (Amarillo), Baja (Verde)
                    Surface(
                        color = priorityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, priorityColor)
                    ) {
                        Text(
                            text = "PRIORIDAD: $priorityLabel",
                            color = priorityColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp
                        )
                    }

                    if (task.relatedConsole.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = task.relatedConsole,
                                color = NeonMagenta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = RetroMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
