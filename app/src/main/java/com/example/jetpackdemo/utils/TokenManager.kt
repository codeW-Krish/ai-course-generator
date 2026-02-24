package com.example.jetpackdemo.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

// Manages the secure storage of JWT access and refresh tokens.
class TokenManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "auth_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val ID_TOKEN = "id_token"
        private const val ACCESS_TOKEN = "access_token_legacy"
        private const val REFRESH_TOKEN = "refresh_token_legacy"
    }

    fun saveIdToken(idToken: String) {
        with(sharedPreferences.edit()) {
            putString(ID_TOKEN, idToken)
            apply()
        }
    }

    fun getIdToken(): String? {
        return sharedPreferences.getString(ID_TOKEN, null)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        with(sharedPreferences.edit()) {
            putString(ID_TOKEN, accessToken)
            putString(ACCESS_TOKEN, accessToken)
            putString(REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ID_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        with(sharedPreferences.edit()) {
            remove(ID_TOKEN)
            remove(ACCESS_TOKEN)
            remove(REFRESH_TOKEN)
            apply()
        }
    }
}
