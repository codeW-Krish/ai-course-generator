package com.example.jetpackdemo.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InteractiveViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InteractiveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InteractiveViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
