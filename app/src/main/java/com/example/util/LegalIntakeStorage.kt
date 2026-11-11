package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor de almacenamiento local para fotos y firmas digitales de Recepción Legal en Retro State RS.
 * Almacena los archivos en context.filesDir de forma segura y devuelve sus URIs en formato String.
 */
object LegalIntakeStorage {

    private fun getOrCreateDir(context: Context, subDirName: String): File {
        val dir = File(context.filesDir, subDirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Guarda una foto capturada por la cámara (Bitmap) en el directorio de evidencias del taller.
     */
    fun savePhotoBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val photosDir = getOrCreateDir(context, "photos")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val photoFile = File(photosDir, "intake_photo_$timestamp.jpg")

            FileOutputStream(photoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Uri.fromFile(photoFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Guarda el Bitmap generado por el SignaturePad en el directorio de firmas.
     */
    fun saveSignatureBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val signaturesDir = getOrCreateDir(context, "signatures")
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val signatureFile = File(signaturesDir, "signature_$timestamp.png")

            FileOutputStream(signatureFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Uri.fromFile(signatureFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
