package com.example.sera_application.data.local.image

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class LocalImageManager(private val context: Context) {

    fun saveEventImage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream")

        val imagesDir = File(context.filesDir, "event_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val file = File(imagesDir, "event_${UUID.randomUUID()}.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        return file.absolutePath
    }
}
