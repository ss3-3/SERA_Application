package com.example.sera_application.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File

data class ReceiptData(
    val eventName: String,
    val transactionId: String,
    val date: String,
    val time: String,
    val venue: String,
    val ticketType: String,
    val quantity: Int,
    val seats: String,
    val price: Double,
    val email: String,
    val name: String,
    val phone: String
)

class PdfReceiptGenerator(private val context: Context) {

    fun generateReceipt(receiptData: ReceiptData): File {
        val pdfFile = File(context.cacheDir, "receipt_${receiptData.transactionId}.pdf")

        val writer = PdfWriter(pdfFile)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        val title = Paragraph("PAYMENT RECEIPT")
            .setFontSize(24f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(title)

        val eventName = Paragraph(receiptData.eventName)
            .setFontSize(18f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10f)
        document.add(eventName)

        val qrCodeBitmap = generateQRCode(receiptData)
        val qrCodeBytes = bitmapToByteArray(qrCodeBitmap)
        val qrCodeImage = Image(ImageDataFactory.create(qrCodeBytes))
            .setWidth(200f)
            .setHeight(200f)
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(qrCodeImage)

        val qrNote = Paragraph("Please present this QR code at the entrance")
            .setFontSize(10f)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DeviceRgb(128, 128, 128))
            .setMarginBottom(20f)
        document.add(qrNote)

        val eventDetailsTitle = Paragraph("EVENT DETAILS")
            .setFontSize(14f)
            .setBold()
            .setMarginBottom(10f)
        document.add(eventDetailsTitle)

        val eventTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)

        addTableRow(eventTable, "Date:", receiptData.date)
        addTableRow(eventTable, "Time:", receiptData.time)
        addTableRow(eventTable, "Venue:", receiptData.venue)
        addTableRow(eventTable, "Ticket Type:", receiptData.ticketType)
        addTableRow(eventTable, "Quantity:", receiptData.quantity.toString())
        addTableRow(eventTable, "Seats:", receiptData.seats)

        document.add(eventTable)

        val paymentDetailsTitle = Paragraph("PAYMENT DETAILS")
            .setFontSize(14f)
            .setBold()
            .setMarginBottom(10f)
        document.add(paymentDetailsTitle)

        val paymentTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)

        addTableRow(paymentTable, "Transaction ID:", receiptData.transactionId)
        addTableRow(paymentTable, "Email:", receiptData.email)
        addTableRow(paymentTable, "Name:", receiptData.name)
        addTableRow(paymentTable, "Phone:", receiptData.phone)
        addTableRow(paymentTable, "Total Amount:", String.format("RM %.2f", receiptData.price))

        document.add(paymentTable)

        document.close()

        return pdfFile
    }

    private fun addTableRow(table: Table, label: String, value: String) {
        table.addCell(
            com.itextpdf.layout.element.Cell()
                .add(Paragraph(label).setFontSize(11f).setBold())
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(5f)
        )
        table.addCell(
            com.itextpdf.layout.element.Cell()
                .add(Paragraph(value).setFontSize(11f))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(5f)
        )
    }

    private fun generateQRCode(receiptData: ReceiptData): Bitmap {
        val qrContent = "Event: ${receiptData.eventName}\n" +
                "Transaction ID: ${receiptData.transactionId}\n" +
                "Date: ${receiptData.date}\n" +
                "Time: ${receiptData.time}\n" +
                "Seats: ${receiptData.seats}\n" +
                "Tickets: ${receiptData.quantity}"

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}