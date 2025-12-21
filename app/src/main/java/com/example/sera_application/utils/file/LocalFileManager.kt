package com.example.sera_application.utils.file

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Manages local file operations for images.
 * 
 * This class handles saving images to internal app storage and managing
 * image files. All file operations are isolated here to maintain Clean Architecture.
 * 
 * Storage location: /data/data/{package}/files/images/
 */
class LocalFileManager(private val context: Context) {

    companion object {
        private const val TAG = "LocalFileManager"
        private const val IMAGES_DIR_NAME = "images"
    }

    /**
     * Gets or creates the images directory in app internal storage.
     */
    private fun getImagesDirectory(): File {
        val imagesDir = File(context.filesDir, IMAGES_DIR_NAME)
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return imagesDir
    }

    /**
     * Saves an image from a Uri to internal app storage.
     * 
     * @param uri The Uri of the image to save (from gallery picker)
     * @param fileName The desired file name (e.g., "event_123.jpg" or "user_456.jpg")
     * @return The absolute file path of the saved image, or empty string on failure
     * 
     * Example return: "/data/user/0/com.example.app/files/images/event_123.jpg"
     */
    fun saveImageFromUri(uri: Uri, fileName: String): String {
        if (fileName.isBlank()) {
            Log.e(TAG, "saveImageFromUri: fileName is blank")
            return ""
        }

        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: run {
                    Log.e(TAG, "saveImageFromUri: Cannot open input stream for uri: $uri")
                    return ""
                }

            val imagesDir = getImagesDirectory()
            val outputFile = File(imagesDir, fileName)
            
            // Ensure parent directory exists
            outputFile.parentFile?.mkdirs()

            FileOutputStream(outputFile).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }

            val absolutePath = outputFile.absolutePath
            Log.d(TAG, "saveImageFromUri: Image saved successfully to $absolutePath")
            absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "saveImageFromUri: IOException while saving image", e)
            ""
        } catch (e: Exception) {
            Log.e(TAG, "saveImageFromUri: Unexpected error while saving image", e)
            ""
        }
    }

    /**
     * Deletes an image file at the given path.
     * 
     * @param path The absolute file path to delete
     * @return true if file was deleted successfully, false otherwise
     */
    fun deleteImage(path: String): Boolean {
        if (path.isBlank()) {
            Log.w(TAG, "deleteImage: path is blank")
            return false
        }

        return try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "deleteImage: Successfully deleted $path")
                } else {
                    Log.w(TAG, "deleteImage: Failed to delete $path")
                }
                deleted
            } else {
                Log.w(TAG, "deleteImage: File does not exist or is not a file: $path")
                false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "deleteImage: SecurityException while deleting $path", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "deleteImage: Unexpected error while deleting $path", e)
            false
        }
    }

    /**
     * Clears unused images from the images directory.
     * 
     * This method deletes all image files in the images directory that are not
     * in the provided set of used paths. Useful for cleanup operations.
     * 
     * @param usedPaths Set of absolute file paths that should be kept
     * @return Number of files deleted
     */
    fun clearUnusedImages(usedPaths: Set<String>): Int {
        return try {
            val imagesDir = getImagesDirectory()
            if (!imagesDir.exists() || !imagesDir.isDirectory) {
                return 0
            }

            val usedPathsSet = usedPaths.toSet()
            var deletedCount = 0

            imagesDir.listFiles()?.forEach { file ->
                if (file.isFile && !usedPathsSet.contains(file.absolutePath)) {
                    if (file.delete()) {
                        deletedCount++
                        Log.d(TAG, "clearUnusedImages: Deleted unused file ${file.absolutePath}")
                    }
                }
            }

            Log.d(TAG, "clearUnusedImages: Deleted $deletedCount unused files")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "clearUnusedImages: Error while clearing unused images", e)
            0
        }
    }
}
