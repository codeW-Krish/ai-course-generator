package com.example.jetpackdemo.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.data.api.RetrofitClient
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.data.repository.InteractiveRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Interactive flow phases:
 * 1. LOADING_CONTENT → Fetching subtopic content
 * 2. READING → User reads subtopic content, quiz loads in background
 * 3. QUIZ → User answers questions (client-side verification)
 * 4. SUMMARY → Quiz results summary with explanations
 */
enum class InteractivePhase {
    LOADING_CONTENT, READING, LOADING_QUIZ, QUIZ, SUMMARY, COURSE_COMPLETED, ERROR
}

class InteractiveViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val api = RetrofitClient.getAuthApi(application)
    private val repository = InteractiveRepository(api)

    // === Phase / Navigation State ===
    private val _phase = MutableStateFlow(InteractivePhase.LOADING_CONTENT)
    val phase: StateFlow<InteractivePhase> = _phase.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // === Content State ===
    private val _subtopic = MutableStateFlow<InteractiveSubtopic?>(null)
    val subtopic: StateFlow<InteractiveSubtopic?> = _subtopic.asStateFlow()

    private val _courseProgress = MutableStateFlow<CourseProgress?>(null)
    val courseProgress: StateFlow<CourseProgress?> = _courseProgress.asStateFlow()

    // === Quiz State ===
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _quizReady = MutableStateFlow(false)
    val quizReady: StateFlow<Boolean> = _quizReady.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer: StateFlow<String?> = _selectedAnswer.asStateFlow()

    private val _textAnswer = MutableStateFlow("")
    val textAnswer: StateFlow<String> = _textAnswer.asStateFlow()

    // Client-side verification result for current question
    private val _currentAnswerResult = MutableStateFlow<QuizAttemptResult?>(null)
    val currentAnswerResult: StateFlow<QuizAttemptResult?> = _currentAnswerResult.asStateFlow()

    // All answers collected during quiz
    private val _quizResults = MutableStateFlow<List<QuizAttemptResult>>(emptyList())
    val quizResults: StateFlow<List<QuizAttemptResult>> = _quizResults.asStateFlow()

    // Hearts (3 max, lose one per wrong answer)
    private val _heartsRemaining = MutableStateFlow(3)
    val heartsRemaining: StateFlow<Int> = _heartsRemaining.asStateFlow()

    // === Summary State ===
    private val _submitResult = MutableStateFlow<SubmitQuizResponse?>(null)
    val submitResult: StateFlow<SubmitQuizResponse?> = _submitResult.asStateFlow()

    // === Chat (kept for tutor) ===
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // === Legacy compatibility ===
    private val _session = MutableStateFlow<Resource<InteractiveSessionResponse>?>(null)
    val session: StateFlow<Resource<InteractiveSessionResponse>?> = _session.asStateFlow()

    private val _verifyResult = MutableStateFlow<Resource<VerifyAnswerResponse>?>(null)
    val verifyResult: StateFlow<Resource<VerifyAnswerResponse>?> = _verifyResult.asStateFlow()

    private val _courseCompleted = MutableStateFlow(false)
    val courseCompleted: StateFlow<Boolean> = _courseCompleted.asStateFlow()

    private val _currentSubtopicId = MutableStateFlow<String?>(null)
    val currentSubtopicId: StateFlow<String?> = _currentSubtopicId.asStateFlow()

    private var backgroundQuizJob: Job? = null

    // ================================================================
    // === Content-First Flow =========================================
    // ================================================================

    /**
     * Load content for the next uncompleted subtopic.
     * Shows content first, triggers background quiz loading.
     */
    fun loadNextContent(courseId: String, provider: String = "Groq") {
        viewModelScope.launch {
            _phase.value = InteractivePhase.LOADING_CONTENT
            _errorMessage.value = null
            _quizReady.value = false
            _quizQuestions.value = emptyList()
            _quizResults.value = emptyList()
            _currentQuestionIndex.value = 0
            _selectedAnswer.value = null
            _textAnswer.value = ""
            _currentAnswerResult.value = null
            _heartsRemaining.value = 3
            _submitResult.value = null

            try {
                val response = repository.getNextContent(courseId, provider)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.courseCompleted) {
                        _phase.value = InteractivePhase.COURSE_COMPLETED
                        _courseCompleted.value = true
                        return@launch
                    }

                    _subtopic.value = body.subtopic
                    _courseProgress.value = body.courseProgress
                    _heartsRemaining.value = body.heartsRemaining
                    _currentSubtopicId.value = body.subtopic?.id
                    _phase.value = InteractivePhase.READING

                    Log.d("InteractiveVM", "Content loaded: ${body.subtopic?.title}, questions_ready=${body.questionsReady}")

                    // Background: load quiz questions
                    val subtopicId = body.subtopic?.id ?: return@launch
                    backgroundQuizJob = loadQuizInBackground(subtopicId, provider)
                } else {
                    _phase.value = InteractivePhase.ERROR
                    _errorMessage.value = "Failed to load content: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("InteractiveVM", "Error loading content", e)
                _phase.value = InteractivePhase.ERROR
                _errorMessage.value = e.localizedMessage ?: "Unknown error"
            }
        }
    }

    /**
     * Load quiz in background while user reads content.
     * Retries a few times if questions aren't ready yet.
     */
    private fun loadQuizInBackground(subtopicId: String, provider: String = "Groq"): Job {
        return viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 5
            while (attempts < maxAttempts) {
                try {
                    val response = repository.getQuiz(subtopicId, provider)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.questions.isNotEmpty()) {
                            _quizQuestions.value = body.questions
                            _quizReady.value = true
                            Log.d("InteractiveVM", "Quiz loaded in background: ${body.questions.size} questions")
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Log.w("InteractiveVM", "Background quiz load attempt $attempts failed", e)
                }
                attempts++
                delay(2000L) // Wait 2s before retry
            }
            Log.w("InteractiveVM", "Background quiz loading exhausted retries")
        }
    }

    /**
     * User clicks "Continue" — transition from reading to quiz.
     */
    fun startQuiz() {
        if (_quizReady.value && _quizQuestions.value.isNotEmpty()) {
            _phase.value = InteractivePhase.QUIZ
            _currentQuestionIndex.value = 0
            _selectedAnswer.value = null
            _textAnswer.value = ""
            _currentAnswerResult.value = null
        } else {
            // Quiz not ready yet — show loading
            _phase.value = InteractivePhase.LOADING_QUIZ
            viewModelScope.launch {
                // Wait for background job to complete
                backgroundQuizJob?.join()
                if (_quizReady.value && _quizQuestions.value.isNotEmpty()) {
                    _phase.value = InteractivePhase.QUIZ
                } else {
                    // Try one more time directly
                    val subtopicId = _currentSubtopicId.value ?: return@launch
                    try {
                        val response = repository.getQuiz(subtopicId)
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            _quizQuestions.value = body.questions
                            _quizReady.value = true
                            _phase.value = InteractivePhase.QUIZ
                        } else {
                            _phase.value = InteractivePhase.ERROR
                            _errorMessage.value = "Failed to load quiz"
                        }
                    } catch (e: Exception) {
                        _phase.value = InteractivePhase.ERROR
                        _errorMessage.value = e.localizedMessage ?: "Failed to load quiz"
                    }
                }
            }
        }
    }

    // ================================================================
    // === Client-Side Answer Verification ============================
    // ================================================================

    fun selectAnswer(answer: String) {
        _selectedAnswer.value = answer
    }

    fun setTextAnswer(text: String) {
        _textAnswer.value = text
    }

    /**
     * Check answer locally (instant, no network call).
     */
    fun checkAnswer() {
        val questions = _quizQuestions.value
        val idx = _currentQuestionIndex.value
        if (idx >= questions.size) return

        val question = questions[idx]
        val userAnswer = if (question.questionType == "mcq") {
            _selectedAnswer.value ?: return
        } else {
            _textAnswer.value.ifBlank { return }
        }

        val isCorrect = userAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)

        val result = QuizAttemptResult(
            questionId = question.id,
            questionText = question.questionText,
            userAnswer = userAnswer,
            correctAnswer = question.correctAnswer,
            isCorrect = isCorrect,
            explanation = question.explanation,
            hint = question.hint
        )

        _currentAnswerResult.value = result
        _quizResults.value = _quizResults.value + result

        if (!isCorrect) {
            _heartsRemaining.value = maxOf(_heartsRemaining.value - 1, 0)
        }

        Log.d("InteractiveVM", "Answer checked locally: correct=$isCorrect, hearts=${_heartsRemaining.value}")
    }

    /**
     * Move to next question or summary.
     */
    fun nextQuestion() {
        val questions = _quizQuestions.value
        val idx = _currentQuestionIndex.value

        if (idx < questions.size - 1) {
            _currentQuestionIndex.value = idx + 1
            _selectedAnswer.value = null
            _textAnswer.value = ""
            _currentAnswerResult.value = null
        } else {
            // Last question — go to summary
            showSummary()
        }
    }

    /**
     * Show quiz summary and submit results to server async.
     */
    private fun showSummary() {
        _phase.value = InteractivePhase.SUMMARY

        // Submit results to server in background (async, non-blocking)
        val subtopicId = _currentSubtopicId.value ?: return
        viewModelScope.launch {
            try {
                val answers = _quizResults.value.map { r ->
                    QuizAnswerItem(
                        questionId = r.questionId,
                        userAnswer = r.userAnswer,
                        isCorrect = r.isCorrect
                    )
                }
                val response = repository.submitQuiz(subtopicId, answers)
                if (response.isSuccessful && response.body() != null) {
                    _submitResult.value = response.body()!!
                    Log.d("InteractiveVM", "Quiz results submitted: ${response.body()}")
                } else {
                    Log.w("InteractiveVM", "Submit quiz failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.w("InteractiveVM", "Submit quiz error (non-blocking)", e)
            }
        }
    }

    /**
     * Log a hub activity (fire-and-forget).
     */
    fun logHubActivity(courseId: String, subtopicId: String?, featureType: String, title: String = "") {
        viewModelScope.launch {
            try {
                repository.logHubActivity(courseId, subtopicId, featureType, title)
            } catch (_: Exception) { /* ignore */ }
        }
    }

    // ================================================================
    // === Legacy endpoints (kept for backward compat) ================
    // ================================================================

    fun loadNextSubtopic(courseId: String, provider: String = "Groq") {
        viewModelScope.launch {
            _session.value = Resource.Loading()
            _currentQuestionIndex.value = 0
            _verifyResult.value = null
            _selectedAnswer.value = null
            _textAnswer.value = ""
            try {
                val response = repository.getNextSubtopic(courseId, provider)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _session.value = Resource.Success(body)
                    _heartsRemaining.value = body.heartsRemaining
                    _courseCompleted.value = body.courseCompleted
                    _currentSubtopicId.value = body.subtopic?.id
                    Log.d("InteractiveVM", "Loaded next subtopic: ${body.subtopic?.title}")
                } else {
                    _session.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("InteractiveVM", "Error loading next subtopic", e)
                _session.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadSession(subtopicId: String, provider: String = "Groq") {
        viewModelScope.launch {
            _session.value = Resource.Loading()
            _currentQuestionIndex.value = 0
            _verifyResult.value = null
            _selectedAnswer.value = null
            _textAnswer.value = ""
            try {
                val response = repository.getSession(subtopicId, provider)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _session.value = Resource.Success(body)
                    _heartsRemaining.value = body.heartsRemaining
                    _currentSubtopicId.value = body.subtopic?.id
                    Log.d("InteractiveVM", "Session loaded for: ${body.subtopic?.title}")
                } else {
                    _session.value = Resource.Error("Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("InteractiveVM", "Error loading session", e)
                _session.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun verifyAnswer() {
        val sessionData = (_session.value as? Resource.Success)?.data ?: return
        val questions = sessionData.questions
        val currentIdx = _currentQuestionIndex.value
        if (currentIdx >= questions.size) return

        val question = questions[currentIdx]
        val subtopicId = sessionData.subtopic?.id ?: return
        val answer = if (question.type == "mcq") {
            _selectedAnswer.value ?: return
        } else {
            _textAnswer.value.ifBlank { return }
        }

        viewModelScope.launch {
            _verifyResult.value = Resource.Loading()
            try {
                val response = repository.verifyAnswer(subtopicId, question.id, answer)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    _verifyResult.value = Resource.Success(result)
                    _heartsRemaining.value = result.heartsRemaining
                    Log.d("InteractiveVM", "Answer verified: correct=${result.correct}, hearts=${result.heartsRemaining}")
                } else {
                    _verifyResult.value = Resource.Error("Verification failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _verifyResult.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun sendChatMessage(message: String) {
        val subtopicId = _currentSubtopicId.value ?: return
        if (message.isBlank()) return

        viewModelScope.launch {
            _chatMessages.value = _chatMessages.value + ChatMessage(message, isUser = true)
            _isChatLoading.value = true
            try {
                val response = repository.sendChat(subtopicId, message)
                if (response.isSuccessful && response.body() != null) {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        response.body()!!.aiResponse,
                        isUser = false
                    )
                } else {
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        "Sorry, I couldn't process your question. Please try again.",
                        isUser = false
                    )
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    "Connection error: ${e.localizedMessage}",
                    isUser = false
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearSession() {
        _session.value = null
        _currentQuestionIndex.value = 0
        _verifyResult.value = null
        _chatMessages.value = emptyList()
        _selectedAnswer.value = null
        _textAnswer.value = ""
        _currentSubtopicId.value = null
        _courseCompleted.value = false
        // Also reset content-first flow
        _phase.value = InteractivePhase.LOADING_CONTENT
        _subtopic.value = null
        _quizQuestions.value = emptyList()
        _quizReady.value = false
        _quizResults.value = emptyList()
        _currentAnswerResult.value = null
        _submitResult.value = null
        _courseProgress.value = null
        _errorMessage.value = null
        backgroundQuizJob?.cancel()
        backgroundQuizJob = null
    }
}
