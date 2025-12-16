package com.example.sera_application.domain.usecase.image

import com.example.sera_application.domain.repository.ImageRepository
import javax.inject.Inject

class SaveImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(localUri: String, fileName: String): String? {
        if (localUri.isBlank() || fileName.isBlank()) return null

        return try {
            imageRepository.saveImage(localUri, fileName)
        } catch (e: Exception) {
            null
        }
    }
}