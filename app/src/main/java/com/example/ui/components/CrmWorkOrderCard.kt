package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkOrderWithClient
import com.example.ui.theme.*

@Composable
fun CrmWorkOrderCard(
    orderWithClient: WorkOrderWithClient,
    onClick: () -> Unit
) {
    val order = orderWithClient.order
    val client = orderWithClient.client
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(2.dp, NeonCyan),
        colors = CardDefaults.cardColors(containerColor = RetroGray),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[#${order.orderCode}] ${order.deviceModel}",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Box(modifier = Modifier.background(RetroBlack).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = "CLIENTE: ${client?.fullName?.uppercase() ?: "N/A"}", color = Color.White, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "> FALLA: ${order.reportedFault}", color = RetroAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "TOTAL: $${order.totalCost}", color = Color.White, fontSize = 14.sp)
        }
    }
}
