package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcadeCardBorder
import com.example.ui.theme.ArcadeDark
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PixelRedHeart
import com.example.ui.theme.RetroGoldCoin
import com.example.ui.theme.RetroMuted
import com.example.util.LegalIntakeStorage

/**
 * Visualizador de imágenes locales mediante URI file:// o content://
 */
@Composable
fun LocalImageViewer(
    uriString: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmap = remember(uriString) {
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" && uri.path != null) {
                BitmapFactory.decodeFile(uri.path)
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(ArcadeDark),
            contentAlignment = Alignment.Center
        ) {
            Text("[ Foto ]", color = RetroMuted, fontSize = 9.sp)
        }
    }
}

/**
 * Módulo de Recepción Legal y Evidencia Fotográfica en Retro State RS.
 * Permite capturar hasta 3 fotos del estado físico (rayones, golpes, sellos) y firmar digitalmente.
 */
@Composable
fun LegalIntakeSection(
    modifier: Modifier = Modifier,
    photosUris: List<String>,
    signatureUri: String?,
    onPhotosChanged: (updatedPhotos: List<String>) -> Unit,
    onSignatureChanged: (updatedSignature: String?) -> Unit
) {
    val context = LocalContext.current

    // Launcher de Cámara nativa con TakePicturePreview (retorna Bitmap directamente sin FileProvider)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null && photosUris.size < 3) {
            val savedUri = LegalIntakeStorage.savePhotoBitmap(context, bitmap)
            if (savedUri != null) {
                onPhotosChanged(photosUris + savedUri)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ArcadeSurface),
        border = BorderStroke(1.dp, RetroGoldCoin.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("legal_intake_section")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header Módulo Legal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = RetroGoldCoin,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "3. RECEPCIÓN LEGAL & EVIDENCIA FOTOGRÁFICA",
                        color = RetroGoldCoin,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Constancia física (hasta 3 fotos) y conformidad del cliente",
                        color = RetroMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SUBSECCIÓN A: EVIDENCIA FOTOGRÁFICA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESTADO FÍSICO (${photosUris.size}/3 FOTOS)",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (photosUris.size < 3) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("btn_take_photo")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TOMAR FOTO", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (photosUris.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(ArcadeDark, RoundedCornerShape(6.dp))
                        .border(1.dp, ArcadeCardBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[ Ninguna foto registrada. Inmortaliza rayones o sellos antes de desarmar ]",
                        color = RetroMuted,
                        fontSize = 10.sp
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(photosUris) { index, photoUri ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(8.dp))
                        ) {
                            LocalImageViewer(
                                uriString = photoUri,
                                contentDescription = "Foto evidencia ${index + 1}",
                                modifier = Modifier.fillMaxSize()
                            )

                            // Botón eliminar foto
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .background(PixelRedHeart, CircleShape)
                                    .clickable {
                                        val mutable = photosUris.toMutableList()
                                        mutable.removeAt(index)
                                        onPhotosChanged(mutable)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- SUBSECCIÓN B: FIRMA DIGITAL DEL CLIENTE ---
            SignaturePad(
                onSignatureSaved = { uri ->
                    onSignatureChanged(uri)
                },
                onSignatureCleared = {
                    onSignatureChanged(null)
                }
            )
        }
    }
}
