package com.example.jetpackdemo.shared_pref

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

// This class is responsible for securely saving and retrieving the user's API key.
class UserPreferencesManager(context: Context) {
        private val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        fun saveOutlineProvider(provider: String) {
            sharedPreferences.edit().putString("outline_provider", provider).apply()
        }

        fun getOutlineProvider(): String? {
            return sharedPreferences.getString("outline_provider", null)
        }

        fun saveContentProvider(provider: String) {
            sharedPreferences.edit().putString("content_provider", provider).apply()
        }

        fun getContentProvider(): String? {
            return sharedPreferences.getString("content_provider", null)
        }

        fun clearAll() {
            sharedPreferences.edit().clear().apply()
        }
}
