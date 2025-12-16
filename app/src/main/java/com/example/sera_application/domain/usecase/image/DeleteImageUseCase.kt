package com.example.sera_application.domain.usecase.image

import com.example.sera_application.domain.repository.ImageRepository
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(path: String): Boolean {
        if (path.isBlank()) return false

        return try {
            imageRepository.deleteImage(path)
        } catch (e: Exception) {
            false
        }
    }
}