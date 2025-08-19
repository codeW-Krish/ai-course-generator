package com.example.jetpackdemo.shared_pref

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

// This class is responsible for securely saving and retrieving the user's API key.
class UserPreferencesManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "user_api_key_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val API_KEY = "gemini_api_key"
    }

    fun saveApiKey(apiKey: String) {
        with(sharedPreferences.edit()) {
            putString(API_KEY, apiKey)
            apply()
        }
    }

    fun getApiKey(): String {
        return sharedPreferences.getString(API_KEY, "") ?: ""
    }

    // New function to clear the API key on logout
    fun clearApiKey() {
        with(sharedPreferences.edit()) {
            remove(API_KEY)
            apply()
        }
    }
}
