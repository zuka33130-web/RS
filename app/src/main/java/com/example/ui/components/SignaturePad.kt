package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PixelGreen1UP
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroMuted
import com.example.ui.theme.RetroWhite
import com.example.util.LegalIntakeStorage

/**
 * Trazo individual formado por una lista de puntos de arrastre.
 */
data class LineStroke(
    val points: List<Offset>
)

/**
 * Componente interactivo de Firma Digital (Signature Pad) con estética retro de terminal.
 * Permite al cliente firmar con el dedo sobre un Canvas reactivo y exportar la firma como Bitmap.
 */
@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureSaved: (signatureUri: String) -> Unit = {},
    onSignatureCleared: () -> Unit = {}
) {
    val context = LocalContext.current
    val strokes = remember { mutableStateListOf<LineStroke>() }
    var currentStrokePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize(600, 250)) }
    var savedSignatureUri by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signature_pad_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = PixelGreen1UP,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FIRMA DIGITAL DEL CLIENTE",
                    color = PixelGreen1UP,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            if (savedSignatureUri != null) {
                Text(
                    text = "[ FIRMA REGISTRADA ]",
                    color = PixelGreen1UP,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Canvas de Firma Retro Terminal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(ArcadeDark, shape = RoundedCornerShape(8.dp))
                .border(
                    width = 1.5.dp,
                    color = if (savedSignatureUri != null) PixelGreen1UP else ArcadeCardBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            savedSignatureUri = null
                            currentStrokePoints = listOf(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentStrokePoints = currentStrokePoints + change.position
                        },
                        onDragEnd = {
                            if (currentStrokePoints.isNotEmpty()) {
                                strokes.add(LineStroke(currentStrokePoints))
                                currentStrokePoints = emptyList()
                            }
                        }
                    )
                }
                .testTag("signature_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                // Dibujar líneas de cuadrícula retro suaves (grid terminal)
                val gridStep = 30.dp.toPx()
                var x = gridStep
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += gridStep
                }
                var y = gridStep
                while (y < size.height) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridStep
                }

                // Dibujar línea guía de firma
                drawLine(
                    color = NeonCyan.copy(alpha = 0.3f),
                    start = Offset(20.dp.toPx(), size.height * 0.75f),
                    end = Offset(size.width - 20.dp.toPx(), size.height * 0.75f),
                    strokeWidth = 1.5f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Renderizar trazos confirmados
                strokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.points.first().x, stroke.points.first().y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = PixelGreen1UP,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }

                // Renderizar trazo actual en vivo
                if (currentStrokePoints.size > 1) {
                    val activePath = Path().apply {
                        moveTo(currentStrokePoints.first().x, currentStrokePoints.first().y)
                        for (i in 1 until currentStrokePoints.size) {
                            lineTo(currentStrokePoints[i].x, currentStrokePoints[i].y)
                        }
                    }
                    drawPath(
                        path = activePath,
                        color = PixelGreen1UP,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Marca de agua si está vacío
                if (strokes.isEmpty() && currentStrokePoints.isEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "Firme aquí con el dedo...",
                        size.width / 2f,
                        size.height / 2f,
                        Paint().apply {
                            color = Color.White.copy(alpha = 0.25f).toArgb()
                            textSize = 34f
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de acción: Limpiar y Guardar Firma
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    strokes.clear()
                    currentStrokePoints = emptyList()
                    savedSignatureUri = null
                    onSignatureCleared()
                },
                enabled = strokes.isNotEmpty() || savedSignatureUri != null,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PixelRedHeart
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("btn_clear_signature")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("LIMPIAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (strokes.isNotEmpty()) {
                        val w = canvasSize.width.coerceAtLeast(400)
                        val h = canvasSize.height.coerceAtLeast(200)

                        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)

                        // Fondo oscuro terminal
                        canvas.drawColor(0xFF0C0D17.toInt())

                        // Pintura de firma verde neón
                        val paint = Paint().apply {
                            color = 0xFF39FF14.toInt() // Neon Green
                            strokeWidth = 6f
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            isAntiAlias = true
                        }

                        strokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                val androidPath = android.graphics.Path()
                                androidPath.moveTo(stroke.points.first().x, stroke.points.first().y)
                                for (i in 1 until stroke.points.size) {
                                    androidPath.lineTo(stroke.points[i].x, stroke.points[i].y)
                                }
                                canvas.drawPath(androidPath, paint)
                            }
                        }

                        val uri = LegalIntakeStorage.saveSignatureBitmap(context, bitmap)
                        if (uri != null) {
                            savedSignatureUri = uri
                            onSignatureSaved(uri)
                        }
                    }
                },
                enabled = strokes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelGreen1UP,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("btn_save_signature")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("GUARDAR FIRMA", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
