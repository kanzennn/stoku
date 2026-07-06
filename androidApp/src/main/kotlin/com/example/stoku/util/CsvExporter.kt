package com.example.stoku.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

/** Writes a CSV file to the Downloads folder, using MediaStore on API 29+ and legacy file APIs below it. */
object CsvExporter {

    fun export(context: Context, fileName: String, header: List<String>, rows: List<List<String>>): Result<Uri> =
        runCatching {
            val content = buildString {
                appendLine(header.joinToString(",") { escape(it) })
                rows.forEach { row -> appendLine(row.joinToString(",") { escape(it) }) }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("Unable to create file in Downloads")
                resolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
                    ?: error("Unable to open output stream")
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(content.toByteArray()) }
                Uri.fromFile(file)
            }
        }

    private fun escape(field: String): String =
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
}
