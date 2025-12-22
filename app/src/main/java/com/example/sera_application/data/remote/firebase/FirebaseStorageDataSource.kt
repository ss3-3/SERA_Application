package com.example.sera_application.data.remote.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import javax.inject.Inject

/**
 * Data source for Firebase Storage operations.
 * Handles uploading images to Firebase Storage and getting download URLs.
 */
class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FirebaseStorageDataSource"
        private const val EVENTS_FOLDER = "events"
        private const val PROFILES_FOLDER = "profiles"
    }

    /**
     * Uploads an image to Firebase Storage and returns the download URL.
     * 
     * @param uri The local file URI of the image
     * @param folder The folder path in Firebase Storage (e.g., "events", "profiles")
     * @param fileName The file name (e.g., "event_123.jpg")
     * @return The download URL if successful, null otherwise
     */
    suspend fun uploadImage(uri: Uri, folder: String, fileName: String): String? {
        return try {
            val storageRef = storage.reference
            val imageRef = storageRef.child("$folder/$fileName")
            
            // Use putStream for content URIs (from gallery), putFile for file URIs
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "Cannot open input stream for URI: $uri")
                return null
            }
            
            val uploadTask = imageRef.putStream(inputStream).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            inputStream.close()
            
            Log.d(TAG, "Image uploaded successfully: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload image: ${e.message}", e)
            null
        }
    }

    /**
     * Uploads an event image to Firebase Storage.
     * 
     * @param uri The local file URI of the image
     * @param fileName The file name (e.g., "event_123.jpg")
     * @return The download URL if successful, null otherwise
     */
    suspend fun uploadEventImage(uri: Uri, fileName: String): String? {
        return uploadImage(uri, EVENTS_FOLDER, fileName)
    }

    /**
     * Uploads a profile image to Firebase Storage.
     * 
     * @param uri The local file URI of the image
     * @param fileName The file name (e.g., "user_123.jpg")
     * @return The download URL if successful, null otherwise
     */
    suspend fun uploadProfileImage(uri: Uri, fileName: String): String? {
        return uploadImage(uri, PROFILES_FOLDER, fileName)
    }

    /**
     * Deletes an image from Firebase Storage.
     * 
     * @param downloadUrl The download URL of the image to delete
     * @return true if deleted successfully, false otherwise
     */
    suspend fun deleteImage(downloadUrl: String): Boolean {
        return try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            storageRef.delete().await()
            Log.d(TAG, "Image deleted successfully: $downloadUrl")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete image: ${e.message}", e)
            false
        }
    }
}

