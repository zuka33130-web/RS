package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WorkOrderStatus
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroGoldCoin
import com.example.ui.theme.RetroMuted
import com.example.ui.theme.RetroWhite

import com.example.ui.components.LegalIntakeSection

private val RETRO_CONSOLE_PRESETS = listOf(
    "Super Nintendo (SNES)",
    "Sega Genesis / Mega Drive",
    "Sony PlayStation 1 (PS1)",
    "Sony PlayStation 2 (PS2)",
    "Nintendo 64 (N64)",
    "Game Boy Color",
    "Game Boy Advance SP",
    "Nintendo GameCube",
    "Sega Dreamcast",
    "Nintendo Entertainment System (NES)"
)

@Composable
fun NewCrmOrderDialog(
    onDismiss: () -> Unit,
    onSave: (
        firstName: String,
        lastName: String,
        address: String,
        phone: String,
        clientNotes: String,
        deviceModel: String,
        serialNumber: String,
        reportedFault: String,
        internalNotes: String,
        photosUris: List<String>,
        signatureUri: String?
    ) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var clientNotes by remember { mutableStateOf("") }

    var deviceModel by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var reportedFault by remember { mutableStateOf("") }
    var internalNotes by remember { mutableStateOf("") }

    var photosUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var signatureUri by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ArcadeSurface,
            border = BorderStroke(1.5.dp, NeonCyan),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("new_crm_order_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NUEVA ORDEN & CLIENTE (CRM)",
                            color = NeonCyan,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Registro de entrada al taller y datos relacionales",
                            color = RetroMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = RetroMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: CRM Client Data
                Text(
                    text = "1. DATOS DEL CLIENTE",
                    color = RetroGoldCoin,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombre *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = ArcadeCardBorder
                        ),
                        modifier = Modifier.weight(1f).testTag("input_first_name")
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = ArcadeCardBorder
                        ),
                        modifier = Modifier.weight(1f).testTag("input_last_name")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono / WhatsApp") },
                    placeholder = { Text("ej. +54 9 11 4521-8899") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyan) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ArcadeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_phone")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección / Localidad") },
                    placeholder = { Text("ej. Av. Rivadavia 4521, CABA") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = NeonCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ArcadeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_address")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientNotes,
                    onValueChange = { clientNotes = it },
                    label = { Text("Notas del Cliente") },
                    placeholder = { Text("Preferencias, historial o aclaraciones") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ArcadeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_client_notes")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: Equipment & Fault Data
                Text(
                    text = "2. CONSOLA Y FALLA REPORTADA",
                    color = NeonMagenta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Quick console presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RETRO_CONSOLE_PRESETS.forEach { preset ->
                        FilterChip(
                            selected = deviceModel == preset,
                            onClick = { deviceModel = preset },
                            label = { Text(preset, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMagenta,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deviceModel,
                    onValueChange = { deviceModel = it },
                    label = { Text("Modelo de Consola / Equipo *") },
                    placeholder = { Text("ej. Super Nintendo SNES, Sega Genesis...") },
                    leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = null, tint = NeonMagenta) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMagenta,
                        unfocusedBorderColor = ArcadeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_device_model")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Número de Serie (S/N) / Identificador") },
                    placeholder = { Text("ej. UN18290192 o Etiqueta trasera") },
                    leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = NeonMagenta) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonMagenta,
                        unfocusedBorderColor = ArcadeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_serial_number")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reportedFault,
                    onValueChange = { reportedFault = it },
                    label = { Text("Falla Reportada por el Cliente *") },
                    placeholder = { Text("ej. Enciende pero no da video en TV CRT...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelRedHeart,
                        unfocusedBorderColor = ArcadeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_reported_fault")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = internalNotes,
                    onValueChange = { internalNotes = it },
                    label = { Text("Notas Internas del Taller") },
                    placeholder = { Text("ej. Trae 2 joysticks y cable de audio...") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RetroGoldCoin,
                        unfocusedBorderColor = ArcadeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_internal_notes")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: Legal Intake & Photographic Evidence
                LegalIntakeSection(
                    photosUris = photosUris,
                    signatureUri = signatureUri,
                    onPhotosChanged = { updated -> photosUris = updated },
                    onSignatureChanged = { updated -> signatureUri = updated }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = RetroMuted)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val isValid = firstName.isNotBlank() && deviceModel.isNotBlank() && reportedFault.isNotBlank()

                    Button(
                        onClick = {
                            if (isValid) {
                                onSave(
                                    firstName, lastName, address, phone, clientNotes,
                                    deviceModel, serialNumber, reportedFault, internalNotes,
                                    photosUris, signatureUri
                                )
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_save_crm_order")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("INGRESAR AL TALLER", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
