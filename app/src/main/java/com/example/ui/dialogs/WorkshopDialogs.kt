package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.WorkOrderEntity
import com.example.ui.theme.*

@Composable
fun NewCrmOrderDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit //Simplified for now to test integration
) {
    var deviceModel by remember { mutableStateOf("") }
    var fault by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RetroGray,
        title = { Text("Nueva Orden", color = RetroTerminalGreen) },
        text = {
            Column {
                OutlinedTextField(value = deviceModel, onValueChange = { deviceModel = it }, label = { Text("Equipo") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = fault, onValueChange = { fault = it }, label = { Text("Falla") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(deviceModel, fault, "", "") }, colors = ButtonDefaults.buttonColors(containerColor = RetroTerminalGreen)) {
                Text("Guardar")
            }
        }
    )
}
