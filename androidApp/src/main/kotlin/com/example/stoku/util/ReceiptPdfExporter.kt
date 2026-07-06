package com.example.stoku.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Renders a captured receipt bitmap into a single-page PDF in the app cache dir and
 * returns a content:// Uri (via FileProvider) suitable for a share/view Intent.
 */
object ReceiptPdfExporter {

    fun export(context: Context, trxId: String, bitmap: Bitmap): Result<Uri> = runCatching {
        val document = PdfDocument()
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create(),
        )
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val file = File(receiptsDir, "$trxId.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
