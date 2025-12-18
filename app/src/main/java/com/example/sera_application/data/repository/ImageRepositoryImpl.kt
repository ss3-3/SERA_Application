package com.example.sera_application.data.repository

import android.content.Context
import android.net.Uri
import com.example.sera_application.domain.repository.ImageRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementation of ImageRepository.
 * Uses Firebase Storage for remote image storage.
 */
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) : ImageRepository {

    private val storageRef = storage.reference

    override suspend fun saveImage(localUri: String, fileName: String): String {
        return try {
            val fileUri = Uri.parse(localUri)
            val imageRef = storageRef.child("images/$fileName")
            
            val uploadTask = imageRef.putFile(fileUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Failed to save image: ${e.message}", e)
        }
    }

    override suspend fun deleteImage(path: String): Boolean {
        return try {
            val imageRef = storage.getReferenceFromUrl(path)
            imageRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loadImage(path: String): ByteArray? {
        return try {
            val imageRef = storage.getReferenceFromUrl(path)
            val maxDownloadSize = 10 * 1024 * 1024L // 10MB
            imageRef.getBytes(maxDownloadSize).await()
        } catch (e: Exception) {
            null
        }
    }
}