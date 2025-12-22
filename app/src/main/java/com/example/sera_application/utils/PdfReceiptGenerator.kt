package com.example.sera_application.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for generating PDF receipts using Android's built-in PdfDocument API.
 */
class PdfReceiptGenerator(private val context: Context) {

    /**
     * Generates a PDF receipt file from the provided receipt data.
     *
     * @param receiptData The receipt data to generate the PDF from
     * @return The generated PDF file
     * @throws IOException if file creation fails
     */
    fun generateReceipt(receiptData: ReceiptData): File {
        // Create a temporary file in the app's cache directory
        val fileName = "receipt_${receiptData.transactionId}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)


        try {
            // Create a new PDF document
            val document = PdfDocument()

            // Page size: A4 (595 x 842 points at 72 DPI)
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Set background to white
            canvas.drawColor(Color.WHITE)

            // Define text paints
            val titlePaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val headerPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 16f
                isFakeBoldText = true
            }

            val normalPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 12f
            }

            val labelPaint = TextPaint().apply {
                color = Color.GRAY
                textSize = 11f
            }

            var yPosition = 50f // Start position from top

            // Title
            canvas.drawText("RECEIPT", 297.5f, yPosition, titlePaint)
            yPosition += 40f

            // Draw line separator
            val linePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 2f
            }
            canvas.drawLine(50f, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            // Event Information Section
            canvas.drawText("Event Information", 50f, yPosition, headerPaint)
            yPosition += 25f

            drawLabelValue(canvas, "Event Name:", receiptData.eventName, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Transaction ID:", receiptData.transactionId, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Date:", receiptData.date, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Time:", receiptData.time, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Venue:", receiptData.venue, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 30f

            // Ticket Information Section
            canvas.drawText("Ticket Information", 50f, yPosition, headerPaint)
            yPosition += 25f

            drawLabelValue(canvas, "Ticket Type:", receiptData.ticketType, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Quantity:", receiptData.quantity.toString(), 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Seats:", receiptData.seats, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f

            val priceText = "RM ${String.format("%.2f", receiptData.price)}"
            drawLabelValue(canvas, "Total Price:", priceText, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 30f

            // Customer Information Section
            canvas.drawText("Customer Information", 50f, yPosition, headerPaint)
            yPosition += 25f

            drawLabelValue(canvas, "Name:", receiptData.name, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Email:", receiptData.email, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 20f
            drawLabelValue(canvas, "Phone:", receiptData.phone, 50f, yPosition, labelPaint, normalPaint)
            yPosition += 40f

            // Generate and draw QR code
            try {
                val qrContent = "Transaction: ${receiptData.transactionId}\nEvent: ${receiptData.eventName}\nDate: ${receiptData.date}"
                val qrBitmap = QrCodeGenerator.generateQrCode(qrContent, 150)

                // Draw QR code on the right side
                val qrX = 400f
                val qrY = yPosition - 100f // Position above footer
                canvas.drawBitmap(qrBitmap, qrX, qrY, null)

                // Add QR code label
                val qrLabelPaint = TextPaint().apply {
                    color = Color.BLACK
                    textSize = 10f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Scan for verification", qrX + 75f, qrY + 165f, qrLabelPaint)
            } catch (e: Exception) {
                // If QR code generation fails, continue without it
                e.printStackTrace()
            }

            yPosition += 100f

            // Footer
            val footerPaint = TextPaint().apply {
                color = Color.GRAY
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Thank you for your purchase!", 297.5f, yPosition, footerPaint)
            yPosition += 15f
            canvas.drawText("This is an official receipt. Please keep it for your records.", 297.5f, yPosition, footerPaint)

            // Finish the page
            document.finishPage(page)

            // Write to file
            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }

            // Close the document
            document.close()

        } catch (e: Exception) {
            throw IOException("Failed to generate receipt PDF: ${e.message}", e)
        }


        return file
    }

    /**
     * Helper function to draw label-value pairs
     */
    private fun drawLabelValue(
        canvas: android.graphics.Canvas,
        label: String,
        value: String,
        x: Float,
        y: Float,
        labelPaint: TextPaint,
        valuePaint: TextPaint
    ) {
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x + 150f, y, valuePaint)
    }

    /**
     * Draws a back arrow icon at the specified position
     */
    private fun drawBackArrow(canvas: Canvas, x: Float, y: Float, size: Float) {
        val arrowPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val path = Path().apply {
            // Draw arrow pointing left
            moveTo(x + size, y)
            lineTo(x, y + size / 2)
            lineTo(x + size, y + size)
        }

        canvas.drawPath(path, arrowPaint)
    }
}