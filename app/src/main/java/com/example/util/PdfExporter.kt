package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    private const val PDF_ASSET_PATH = "pdf/les-luminautes-la-voie-de-lumiere.pdf"
    private const val PDF_FILENAME = "les-luminautes-la-voie-de-lumiere.pdf"

    fun exportAndOpenPdf(context: Context) {
        try {
            var savedToPublicDownloads = false

            // Save to public Downloads directory on Android 10+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, PDF_FILENAME)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outStream ->
                            context.assets.open(PDF_ASSET_PATH).use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                        savedToPublicDownloads = true
                    }
                } catch (e: Exception) {
                    Log.w("PdfExporter", "Could not save to MediaStore.Downloads: ${e.message}")
                }
            }

            // Always write to cached / shared directory for direct viewing/printing intent
            val shareDir = File(context.cacheDir, "shared_pdf").apply { mkdirs() }
            val sharedFile = File(shareDir, PDF_FILENAME)
            FileOutputStream(sharedFile).use { out ->
                context.assets.open(PDF_ASSET_PATH).use { input ->
                    input.copyTo(out)
                }
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sharedFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(viewIntent, "Ouvrir ou imprimer le PDF original").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val message = if (savedToPublicDownloads) {
                "PDF original enregistré dans vos Téléchargements"
            } else {
                "PDF prêt pour lecture et impression"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("PdfExporter", "Error exporting PDF: ${e.message}", e)
            Toast.makeText(context, "Impossible d'ouvrir le PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
