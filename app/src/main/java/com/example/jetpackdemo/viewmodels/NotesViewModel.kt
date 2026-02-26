package com.example.jetpackdemo.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NotesRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _notesState = MutableStateFlow<Resource<GeneratedNotes>>(Resource.Loading())
    val notesState: StateFlow<Resource<GeneratedNotes>> = _notesState.asStateFlow()

    private val _exportState = MutableStateFlow<Resource<ExportCourseNotesResponse>?>(null)
    val exportState: StateFlow<Resource<ExportCourseNotesResponse>?> = _exportState.asStateFlow()

    fun loadNotes(subtopicId: String, provider: String? = null) {
        viewModelScope.launch {
            _notesState.value = Resource.Loading()
            try {
                val response = repository.getGeneratedNotes(subtopicId, provider)
                if (response.isSuccessful && response.body() != null) {
                    _notesState.value = Resource.Success(response.body()!!.notes)
                    Log.d("NotesVM", "Notes loaded (generated: ${response.body()!!.generated})")
                } else {
                    _notesState.value = Resource.Error("Failed to load notes: ${response.code()}")
                    Log.e("NotesVM", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NotesVM", "Error loading notes", e)
                _notesState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun exportCourseNotes(courseId: String) {
        viewModelScope.launch {
            _exportState.value = Resource.Loading()
            try {
                val response = repository.exportCourseNotes(courseId, "json")
                if (response.isSuccessful && response.body() != null) {
                    _exportState.value = Resource.Success(response.body()!!)
                } else {
                    _exportState.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _exportState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
