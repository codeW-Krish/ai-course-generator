package com.example.jetpackdemo.shared_pref

import android.content.Context
import android.content.SharedPreferences

// This class is responsible for securely saving and retrieving the user's API key.
class UserPreferencesManager(context: Context) {
        private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Existing token methods...
    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    // Add role management
    fun saveUserRole(role: String) {
        sharedPreferences.edit().putString("user_role", role).apply()
    }

    fun getUserRole(): String {
        return sharedPreferences.getString("user_role", "user") ?: "user"
    }

    fun isAdmin(): Boolean {
        return getUserRole() == "admin"
    }

    // Add provider management with validation
    fun saveContentProvider(provider: String?) {
        sharedPreferences.edit().putString("content_provider", provider).apply()
    }

    fun getContentProvider(): String? {
        return sharedPreferences.getString("content_provider", null)
    }

    fun saveOutlineProvider(provider: String?) {
        sharedPreferences.edit().putString("outline_provider", provider).apply()
    }

    fun getOutlineProvider(): String? {
        return sharedPreferences.getString("outline_provider", null)
    }

    // Validate and fix providers against available list
    fun validateAndFixProviders(availableProviders: List<String>): Boolean {
        var needsFix = false

        val currentContent = getContentProvider()
        val currentOutline = getOutlineProvider()

        if (currentContent != null && !availableProviders.contains(currentContent)) {
            saveContentProvider(null)
            needsFix = true
        }

        if (currentOutline != null && !availableProviders.contains(currentOutline)) {
            saveOutlineProvider(null)
            needsFix = true
        }

        return needsFix
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
