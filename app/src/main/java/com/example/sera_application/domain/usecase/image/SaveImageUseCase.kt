package com.example.sera_application.domain.usecase.image

import android.net.Uri
import com.example.sera_application.domain.repository.ImageRepository
import javax.inject.Inject

/**
 * Use case for saving images to local storage.
 * 
 * Accepts a Uri from the UI layer and returns an absolute file path.
 * The Uri is passed through to the repository layer where it's handled.
 * 
 * @param uri The Android Uri of the image (from gallery picker)
 * @param fileName The desired file name (e.g., "event_123.jpg" or "user_456.jpg")
 * @return The absolute file path if successful, empty string if failed, null if invalid input
 */
class SaveImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(uri: Uri, fileName: String): String? {
        if (fileName.isBlank()) {
            return null
        }

        return try {
            val path = imageRepository.saveImage(uri, fileName)
            // Return null if path is empty (save failed)
            if (path.isBlank()) null else path
        } catch (e: Exception) {
            null
        }
    }
}