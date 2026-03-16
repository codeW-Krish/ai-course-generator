package com.example.jetpackdemo.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val repository: FlashcardRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _flashcards = MutableStateFlow<Resource<List<FlashcardItem>>>(Resource.Loading())
    val flashcards: StateFlow<Resource<List<FlashcardItem>>> = _flashcards.asStateFlow()

    private val _dueFlashcards = MutableStateFlow<Resource<List<FlashcardItem>>>(Resource.Loading())
    val dueFlashcards: StateFlow<Resource<List<FlashcardItem>>> = _dueFlashcards.asStateFlow()

    private val _reviewResult = MutableStateFlow<Resource<ReviewFlashcardResponse>?>(null)
    val reviewResult: StateFlow<Resource<ReviewFlashcardResponse>?> = _reviewResult.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    fun loadFlashcards(subtopicId: String, provider: String? = null) {
        viewModelScope.launch {
            _flashcards.value = Resource.Loading()
            try {
                val response = repository.getFlashcards(subtopicId, provider)
                if (response.isSuccessful && response.body() != null) {
                    val cards = response.body()!!.flashcards
                    _flashcards.value = Resource.Success(cards)
                    _currentCardIndex.value = 0
                    _isFlipped.value = false
                    Log.d("FlashcardVM", "Loaded ${cards.size} flashcards (generated: ${response.body()!!.generated})")
                } else {
                    _flashcards.value = Resource.Error("Failed to load flashcards: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FlashcardVM", "Error loading flashcards", e)
                _flashcards.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadDueFlashcards(courseId: String) {
        viewModelScope.launch {
            _dueFlashcards.value = Resource.Loading()
            try {
                val response = repository.getDueFlashcards(courseId)
                if (response.isSuccessful && response.body() != null) {
                    _dueFlashcards.value = Resource.Success(response.body()!!.dueCards)
                    _currentCardIndex.value = 0
                    _isFlipped.value = false
                } else {
                    _dueFlashcards.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _dueFlashcards.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun reviewFlashcard(flashcardId: String, quality: Int) {
        viewModelScope.launch {
            try {
                val response = repository.reviewFlashcard(flashcardId, quality)
                if (response.isSuccessful && response.body() != null) {
                    _reviewResult.value = Resource.Success(response.body()!!)
                    Log.d("FlashcardVM", "Reviewed: next review in ${response.body()!!.intervalDays} days")
                } else {
                    _reviewResult.value = Resource.Error("Review failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _reviewResult.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun nextCard() {
        val cards = (_flashcards.value as? Resource.Success)?.data ?: return
        if (_currentCardIndex.value < cards.size - 1) {
            _currentCardIndex.value++
            _isFlipped.value = false
        }
    }

    fun previousCard() {
        if (_currentCardIndex.value > 0) {
            _currentCardIndex.value--
            _isFlipped.value = false
        }
    }

    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
    }

    fun resetCards() {
        _currentCardIndex.value = 0
        _isFlipped.value = false
    }

    fun clearReviewResult() {
        _reviewResult.value = null
    }
}
