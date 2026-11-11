package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.backup.BackupStats
import com.example.ui.settings.SettingsViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showRestoreWarningDialog by remember { mutableStateOf(false) }

    // SAF Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri)
        }
    }

    // Confirmation Dialog before overwriting database
    if (showRestoreWarningDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreWarningDialog = false },
            containerColor = ArcadeSurface,
            titleContentColor = PixelRedHeart,
            textContentColor = RetroWhite,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = PixelRedHeart,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "¡ADVERTENCIA DE SOBRESCRITURA!",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Restaurar una copia de seguridad reemplazará todas las órdenes, ingresos, egresos, tareas y repuestos actuales.",
                        fontSize = 13.sp,
                        color = RetroWhite
                    )
                    Text(
                        text = "Esta acción es irreversible y sobrescribirá la base de datos local del taller. ¿Deseas seleccionar el archivo .json para restaurar?",
                        fontSize = 12.sp,
                        color = RetroMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreWarningDialog = false
                        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelRedHeart,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("btn_confirm_restore")
                ) {
                    Text("Continuar y Seleccionar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRestoreWarningDialog = false },
                    border = BorderStroke(1.dp, RetroMuted)
                ) {
                    Text("Cancelar", color = RetroWhite)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDark)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Retro Header Banner ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_retro_banner),
                    contentDescription = "Retro State RS Header",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonCyan)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = RetroGoldCoin,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MEMORY CARD RS • SAVE SLOTS",
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "SISTEMA Y COPIAS DE SEGURIDAD",
                        color = RetroWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Gestión local Room Database & respaldo en nube",
                        color = RetroMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- 2. Loading State Indicator ---
        if (uiState.isLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                    border = BorderStroke(1.dp, NeonCyan)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Procesando datos de la base de datos...",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // --- 3. Success Message Card ---
        uiState.successMessage?.let { msg ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceVariant),
                    border = BorderStroke(1.5.dp, PixelGreen1UP)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PixelGreen1UP,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡OPERACIÓN EXITOSA!",
                                color = PixelGreen1UP,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg,
                                color = RetroWhite,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearMessages() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("✕", color = RetroMuted, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // --- 4. Error Message Card ---
        uiState.errorMessage?.let { errorMsg ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceVariant),
                    border = BorderStroke(1.5.dp, PixelRedHeart)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = PixelRedHeart,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ERROR EN LA OPERACIÓN",
                                color = PixelRedHeart,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = errorMsg,
                                color = RetroWhite,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearMessages() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("✕", color = RetroMuted, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // --- 5. Data Management Module (Main Actions) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GESTIÓN DE DATOS",
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Todas tus órdenes, clientes, saldo financiero, misiones y repuestos se almacenan localmente. Creá copias periódicas para no perder información ante cambios de dispositivo.",
                    color = RetroMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                // ACTION 1: Local Export (SAF)
                BackupActionCard(
                    title = "Crear Copia de Seguridad (Local)",
                    subtitle = "Exporta la base de datos completa a un archivo .json mediante el explorador de archivos oficial de Android.",
                    icon = Icons.Default.FileDownload,
                    accentColor = NeonCyan,
                    buttonText = "Exportar a Dispositivo",
                    testTag = "btn_export_backup",
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        val suggestedName = "RetroStateRS_Backup_$timestamp.json"
                        exportLauncher.launch(suggestedName)
                    }
                )

                // ACTION 2: Local Import (SAF with Warning)
                BackupActionCard(
                    title = "Restaurar Copia de Seguridad",
                    subtitle = "Carga un archivo de respaldo previo y reconstruye las órdenes, transacciones, misiones y stock.",
                    icon = Icons.Default.FileUpload,
                    accentColor = NeonMagenta,
                    buttonText = "Restaurar desde Archivo",
                    testTag = "btn_import_backup",
                    isWarning = true,
                    onClick = {
                        showRestoreWarningDialog = true
                    }
                )

                // ACTION 3: Cloud Share (FileProvider + ACTION_SEND)
                BackupActionCard(
                    title = "Respaldar en la Nube (Drive / Correo)",
                    subtitle = "Genera el archivo al vuelo y abre el menú para guardarlo directamente en Google Drive, enviarlo por Gmail o compartirlo por WhatsApp.",
                    icon = Icons.Default.CloudUpload,
                    accentColor = PixelGreen1UP,
                    buttonText = "Compartir / Subir a Drive",
                    testTag = "btn_share_cloud_backup",
                    onClick = {
                        viewModel.prepareCloudShareIntent(context) { chooserIntent ->
                            context.startActivity(chooserIntent)
                        }
                    }
                )
            }
        }

        // --- 6. Last Backup Stats Card ---
        uiState.lastBackupStats?.let { stats ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
                    border = BorderStroke(1.dp, RetroGoldCoin)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RetroGoldCoin,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RESUMEN DEL ÚLTIMO PROCESO",
                                color = RetroGoldCoin,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(label = "Órdenes", value = "${stats.ordersCount}", color = NeonCyan)
                            StatBox(label = "Movimientos", value = "${stats.transactionsCount}", color = PixelGreen1UP)
                            StatBox(label = "Misiones", value = "${stats.tasksCount}", color = RetroGoldCoin)
                            StatBox(label = "Repuestos", value = "${stats.suppliesCount}", color = SnesPurple)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            .format(Date(stats.timestamp))
                        Text(
                            text = "Fecha de registro: $formattedDate",
                            color = RetroMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // --- 7. Security & Privacy Notice ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceVariant),
                border = BorderStroke(1.dp, ArcadeCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = GameBoyGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SEGURIDAD Y PRIVACIDAD",
                            color = GameBoyGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tus archivos de respaldo son 100% tuyos. Se generan en formato JSON seguro y sin acceso a servidores de terceros sin tu autorización.",
                            color = RetroMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // --- 8. App & Workshop Info ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RETRO STATE RS • TALLER DE REPARACIÓN",
                        color = RetroMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Versión 1.2.0 • 2026 Edition",
                        color = RetroMuted.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BackupActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    buttonText: String,
    testTag: String,
    isWarning: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isWarning) NeonMagenta.copy(alpha = 0.6f) else ArcadeCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = RetroWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    if (isWarning) {
                        Text(
                            text = "Sobrescribe datos locales",
                            color = PixelRedHeart,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = RetroMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWarning) ArcadeSurfaceVariant else accentColor,
                    contentColor = if (isWarning) NeonMagenta else Color.Black
                ),
                border = if (isWarning) BorderStroke(1.dp, NeonMagenta) else null
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ArcadeSurfaceVariant,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Text(
                text = label,
                color = RetroMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
