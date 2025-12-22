package com.example.sera_application.domain.repository

/**
 * Repository interface for image operations.
 * 
 * Handles saving and deleting images using local file storage and Firebase Storage.
 * Domain layer works only with String paths/URLs, not Android Uri objects.
 */
interface ImageRepository {

    /**
     * Saves an image from a Uri to local storage.
     * 
     * @param uri The Android Uri of the image (from gallery picker)
     * @param fileName The desired file name (e.g., "event_123.jpg")
     * @return The absolute file path of the saved image, or empty string on failure
     */
    suspend fun saveImage(uri: android.net.Uri, fileName: String): String

    /**
     * Uploads an image to Firebase Storage and returns the download URL.
     * This should be used for event images and profile images that need to be accessible by all users.
     * 
     * @param uri The Android Uri of the image (from gallery picker)
     * @param folder The folder path in Firebase Storage (e.g., "events", "profiles")
     * @param fileName The desired file name (e.g., "event_123.jpg")
     * @return The download URL if successful, null otherwise
     */
    suspend fun uploadImageToFirebase(uri: android.net.Uri, folder: String, fileName: String): String?

    /**
     * Deletes an image file at the given path (local or Firebase Storage URL).
     * 
     * @param path The absolute file path or Firebase Storage download URL
     * @return true if deleted successfully, false otherwise
     */
    suspend fun deleteImage(path: String): Boolean

    /**
     * Loads image bytes from a file path.
     * 
     * @param path The absolute file path
     * @return Image bytes, or null if loading fails
     */
    suspend fun loadImage(path: String): ByteArray?
}