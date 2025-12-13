package com.example.sera_application.domain.repository

//for images
interface ImageRepository {

    suspend fun saveImage(localUri: String, fileName: String): String  // returns stored path

    suspend fun deleteImage(path: String): Boolean

    suspend fun loadImage(path: String): ByteArray?
}