package com.example.sera_application.domain.usecase.image

import com.example.sera_application.domain.repository.ImageRepository
import javax.inject.Inject

class LoadImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(path: String): ByteArray? {
        if (path.isBlank()) return null

        return try {
            imageRepository.loadImage(path)
        } catch (e: Exception) {
            null
        }
    }
}