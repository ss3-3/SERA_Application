package com.example.sera_application.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "sera_app_prefs"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"
    }

    fun setRememberMe(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply()
        if (!enabled) {
            // Clear saved credentials if remember me is disabled
            clearSavedCredentials()
        }
    }

    fun getRememberMe(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString(KEY_SAVED_EMAIL, email)
            .putString(KEY_SAVED_PASSWORD, password)
            .apply()
    }

    fun getSavedEmail(): String? {
        return prefs.getString(KEY_SAVED_EMAIL, null)
    }

    fun getSavedPassword(): String? {
        return prefs.getString(KEY_SAVED_PASSWORD, null)
    }

    fun clearSavedCredentials() {
        prefs.edit()
            .remove(KEY_SAVED_EMAIL)
            .remove(KEY_SAVED_PASSWORD)
            .apply()
    }

    fun hasSavedCredentials(): Boolean {
        return getSavedEmail() != null && getSavedPassword() != null
    }
}

