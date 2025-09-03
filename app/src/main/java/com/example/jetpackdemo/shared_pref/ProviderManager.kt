package com.example.jetpackdemo.shared_pref

import android.content.Context

object ProviderManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_PROVIDER = "selected_provider"
    private const val KEY_MODEL = "selected_model"

    fun saveProvider(context: Context, provider: String, model: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROVIDER, provider)
            .putString(KEY_MODEL, model)
            .apply()
    }

    fun getProvider(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROVIDER, "Gemini") ?: "Gemini"
    }

    fun getModel(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MODEL, null)
    }
}