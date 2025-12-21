package com.example.sera_application.data.repository

import android.content.Context
import android.net.Uri
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
 * Uses LocalFileManager for local file storage operations.
 * 
 * All file I/O is delegated to LocalFileManager to maintain Clean Architecture.
 */
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
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

    override suspend fun deleteImage(path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                localFileManager.deleteImage(path)
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