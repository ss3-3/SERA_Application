package com.example.sera_application.data.repository

import android.content.Context
import android.net.Uri
import com.example.sera_application.data.remote.firebase.FirebaseStorageDataSource
import com.example.sera_application.domain.repository.ImageRepository
import com.example.sera_application.utils.file.LocalFileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

/**
 * Implementation of ImageRepository.
 * Uses LocalFileManager for local file storage operations and FirebaseStorageDataSource for cloud storage.
 * 
 * All file I/O is delegated to LocalFileManager to maintain Clean Architecture.
 */
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseStorageDataSource: FirebaseStorageDataSource
) : ImageRepository {

    private val localFileManager = LocalFileManager(context)

    override suspend fun saveImage(uri: Uri, fileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                localFileManager.saveImageFromUri(uri, fileName)
            } catch (e: Exception) {
                // Return empty string on failure (as per requirements)
                ""
            }
        }
    }

    override suspend fun uploadImageToFirebase(uri: Uri, folder: String, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                firebaseStorageDataSource.uploadImage(uri, folder, fileName)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun deleteImage(path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check if it's a Firebase Storage URL (starts with http/https)
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    firebaseStorageDataSource.deleteImage(path)
                } else {
                    // Local file path
                    localFileManager.deleteImage(path)
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun loadImage(path: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists() && file.isFile) {
                    FileInputStream(file).use { input ->
                        input.readBytes()
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}