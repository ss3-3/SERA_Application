package com.example.sera_application.utils

import java.io.File

/**
 * Utility object for handling image path operations.
 * 
 * This utility provides safe handling of image paths during Phase 1,
 * before LocalFileManager integration is complete.
 */
object ImagePathUtils {
    
    /**
     * Placeholder value used when imagePath is not yet set.
     * UI components should treat this as null/empty and show placeholder.
     */
    const val DEFAULT_IMAGE_PATH = "DEFAULT"
    
    /**
     * Validates if an image path is valid and can be used to load an image.
     * 
     * A path is considered valid if:
     * - It is not null
     * - It is not blank/empty
     * - It is not the DEFAULT placeholder
     * - It represents a local file path (starts with "/")
     * 
     * @param path The image path to validate
     * @return true if the path is valid for image loading, false otherwise
     */
    fun isValidImagePath(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        if (path == DEFAULT_IMAGE_PATH) return false
        
        // Valid local file path should start with "/"
        // Example: "/data/user/0/com.example.app/files/images/event_123.jpg"
        return path.startsWith("/")
    }
    
    /**
     * Checks if a file exists at the given path.
     * Returns false if path is invalid or file doesn't exist.
     * 
     * @param path The image path to check
     * @return true if file exists, false otherwise
     */
    fun imageFileExists(path: String?): Boolean {
        if (!isValidImagePath(path)) return false
        return try {
            File(path!!).exists()
        } catch (e: Exception) {
            false
        }
    }
}

