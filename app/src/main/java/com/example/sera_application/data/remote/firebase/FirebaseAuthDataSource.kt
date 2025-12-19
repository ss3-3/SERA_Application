package com.example.sera_application.data.remote.firebase

import com.example.sera_application.data.remote.datasource.AuthRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth
) : AuthRemoteDataSource {

    override suspend fun login(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("User ID is null after login")
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid ?: throw Exception("User ID is null after registration")
        
        // Update display name
        result.user?.updateProfile(
            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()
        )?.await()

        return userId
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Boolean {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
        
        // Re-authenticate first
        user.reauthenticate(credential).await()
        
        // Update password
        user.updatePassword(newPassword).await()
        return true
    }
}