package com.example.sera_application.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class PdfReceiptGenerator(private val context: Context) {
    /**
     * Mock PDF generation. In a real app, you would use a library like iText or PdfDocument.
     * This version just creates a dummy file so the URI provider doesn't crash.
     */
    fun generateReceipt(data: ReceiptData): File {
        val fileName = "receipt_${data.transactionId}.pdf"
        val file = File(context.cacheDir, fileName)
        
        // Write some dummy content to make it a valid file
        FileOutputStream(file).use { out ->
            out.write("Receipt for ${data.eventName}\n".toByteArray())
            out.write("Transaction ID: ${data.transactionId}\n".toByteArray())
            out.write("Amount: RM ${data.price}\n".toByteArray())
        }
        
        return file
    }
}
