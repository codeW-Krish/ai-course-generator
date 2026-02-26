package com.example.jetpackdemo

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.example.jetpackdemo.data.model.QuizAttemptResult
import com.example.jetpackdemo.data.model.QuizQuestion
import com.example.jetpackdemo.data.model.SubmitQuizResponse
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.InteractivePhase
import com.example.jetpackdemo.viewmodels.InteractiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveLearningScreen(
    navController: NavController,
    viewModel: InteractiveViewModel,
    courseId: String? = null,
    subtopicId: String? = null
) {
    val phase by viewModel.phase.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val subtopic by viewModel.subtopic.collectAsState()
    val courseProgress by viewModel.courseProgress.collectAsState()
    val quizQuestions by viewModel.quizQuestions.collectAsState()
    val quizReady by viewModel.quizReady.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val textAnswer by viewModel.textAnswer.collectAsState()
    val currentAnswerResult by viewModel.currentAnswerResult.collectAsState()
    val quizResults by viewModel.quizResults.collectAsState()
    val heartsRemaining by viewModel.heartsRemaining.collectAsState()
    val submitResult by viewModel.submitResult.collectAsState()

    // Load on first composition
    LaunchedEffect(courseId, subtopicId) {
        if (courseId != null && phase == InteractivePhase.LOADING_CONTENT && subtopic == null) {
            viewModel.loadNextContent(courseId)
        }
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            subtopic?.title ?: "Interactive Learning",
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            fontSize = 18.sp
                        )
                        if (courseProgress != null) {
                            Text(
                                "${courseProgress!!.completed}/${courseProgress!!.total} subtopics",
                                color = AppColors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSession()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                },
                actions = {
                    if (phase == InteractivePhase.QUIZ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(3) { index ->
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (index < heartsRemaining) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (phase) {
            InteractivePhase.LOADING_CONTENT -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Loading content...", color = AppColors.textSecondary)
                        Text("Preparing your lesson", color = AppColors.textSecondary, fontSize = 12.sp)
                    }
                }
            }

            InteractivePhase.READING -> {
                val formattedContent = remember(subtopic?.id, subtopic?.content) {
                    formatInteractiveContentForDisplay(
                        rawContent = subtopic?.content,
                        fallbackTitle = subtopic?.title ?: ""
                    )
                }

                ContentReadingView(
                    content = formattedContent,
                    title = subtopic?.title ?: "",
                    quizReady = quizReady,
                    onContinueToQuiz = { viewModel.startQuiz() },
                    onOpenHub = {
                        val subId = subtopic?.id ?: return@ContentReadingView
                        val cId = subtopic?.courseId ?: courseId ?: return@ContentReadingView
                        val title = subtopic?.title ?: ""
                        navController.navigate("interactive_hub/$cId/$subId/$title")
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            InteractivePhase.LOADING_QUIZ -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Preparing quiz...", color = AppColors.textSecondary)
                        Text("Almost ready", color = AppColors.textSecondary, fontSize = 12.sp)
                    }
                }
            }

            InteractivePhase.QUIZ -> {
                if (quizQuestions.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No questions available.", color = AppColors.textSecondary)
                    }
                } else if (heartsRemaining <= 0 && currentAnswerResult != null) {
                    GameOverView(
                        onStudyMaterials = {
                            val subId = subtopic?.id ?: return@GameOverView
                            val cId = subtopic?.courseId ?: courseId ?: return@GameOverView
                            val title = subtopic?.title ?: ""
                            navController.navigate("interactive_hub/$cId/$subId/$title")
                        },
                        onRetry = {
                            if (courseId != null) viewModel.loadNextContent(courseId)
                        },
                        hasCourseId = courseId != null,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    val question = quizQuestions.getOrNull(currentQuestionIndex) ?: return@Scaffold
                    QuizQuestionView(
                        question = question,
                        questionNumber = currentQuestionIndex + 1,
                        totalQuestions = quizQuestions.size,
                        selectedAnswer = selectedAnswer,
                        textAnswer = textAnswer,
                        answerResult = currentAnswerResult,
                        heartsRemaining = heartsRemaining,
                        onSelectAnswer = { viewModel.selectAnswer(it) },
                        onTextChanged = { viewModel.setTextAnswer(it) },
                        onCheckAnswer = { viewModel.checkAnswer() },
                        onNext = { viewModel.nextQuestion() },
                        onOpenHub = {
                            val subId = subtopic?.id ?: return@QuizQuestionView
                            val cId = subtopic?.courseId ?: courseId ?: return@QuizQuestionView
                            val title = subtopic?.title ?: ""
                            navController.navigate("interactive_hub/$cId/$subId/$title")
                        },
                        isLastQuestion = currentQuestionIndex >= quizQuestions.size - 1,
                        modifier = Modifier.padding(padding)
                    )
                }
            }

            InteractivePhase.SUMMARY -> {
                QuizSummaryView(
                    results = quizResults,
                    submitResult = submitResult,
                    heartsRemaining = heartsRemaining,
                    totalQuestions = quizQuestions.size,
                    onNextSubtopic = {
                        if (courseId != null) viewModel.loadNextContent(courseId)
                    },
                    onBackToCourses = {
                        viewModel.clearSession()
                        navController.popBackStack()
                    },
                    onOpenHub = {
                        val subId = subtopic?.id ?: return@QuizSummaryView
                        val cId = subtopic?.courseId ?: courseId ?: return@QuizSummaryView
                        val title = subtopic?.title ?: ""
                        navController.navigate("interactive_hub/$cId/$subId/$title")
                    },
                    hasCourseId = courseId != null,
                    modifier = Modifier.padding(padding)
                )
            }

            InteractivePhase.COURSE_COMPLETED -> {
                CourseCompletedView(
                    onBackToCourses = {
                        viewModel.clearSession()
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            InteractivePhase.ERROR -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            errorMessage ?: "Something went wrong",
                            color = AppColors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                when {
                                    courseId != null -> viewModel.loadNextContent(courseId)
                                    subtopicId != null -> viewModel.loadSession(subtopicId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) { Text("Retry") }
                    }
                }
            }
        }
    }
}

private data class ParsedInteractiveContent(
    val title: String,
    val whyThisMatters: String,
    val coreConcepts: List<Pair<String, String>>,
    val examples: List<Pair<String, String>>,
    val codeOrMath: String?
)

private fun formatInteractiveContentForDisplay(rawContent: Any?, fallbackTitle: String): String {
    if (rawContent == null) return "Content is not available yet."

    val parsed = parseInteractiveContent(rawContent)
    if (parsed == null) {
        val raw = rawContent.toString().trim()
        return if (raw.isNotBlank()) raw else "Content is not available yet."
    }

    return buildString {
        append(if (parsed.title.isNotBlank()) parsed.title else fallbackTitle)
        append("\n\n")
        append("Why this matters:\n")
        append(parsed.whyThisMatters.ifBlank { "No summary available yet." })

        if (parsed.coreConcepts.isNotEmpty()) {
            append("\n\nCore Concepts:\n")
            parsed.coreConcepts.forEach { (concept, explanation) ->
                append("• ")
                append(concept)
                if (explanation.isNotBlank()) {
                    append(": ")
                    append(explanation)
                }
                append("\n")
            }
        }

        if (parsed.examples.isNotEmpty()) {
            append("\nExamples:\n")
            parsed.examples.forEachIndexed { index, (type, content) ->
                val formattedType = type.replace("_", " ").replaceFirstChar { it.uppercase() }
                append("${index + 1}. [$formattedType] ")
                append(content)
                append("\n")
            }
        }

        if (!parsed.codeOrMath.isNullOrBlank()) {
            append("\nCode or Math:\n")
            append(parsed.codeOrMath)
        }
    }.trim()
}

private fun parseInteractiveContent(rawContent: Any): ParsedInteractiveContent? {
    return try {
        val jsonElement = when (rawContent) {
            is String -> {
                val trimmed = rawContent.trim()
                if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]"))
                ) {
                    JsonParser.parseString(trimmed)
                } else {
                    return ParsedInteractiveContent(
                        title = "Learning Content",
                        whyThisMatters = trimmed,
                        coreConcepts = emptyList(),
                        examples = emptyList(),
                        codeOrMath = null
                    )
                }
            }

            else -> JsonParser.parseString(Gson().toJson(rawContent))
        }

        val obj = when {
            jsonElement.isJsonObject -> jsonElement.asJsonObject
            jsonElement.isJsonArray && jsonElement.asJsonArray.size() > 0 && jsonElement.asJsonArray[0].isJsonObject -> {
                jsonElement.asJsonArray[0].asJsonObject
            }

            else -> return null
        }

        val title = obj.string("title")
            ?: obj.string("subtopic_title")
            ?: "Learning Content"

        val why = obj.string("why_this_matters")
            ?: obj.string("whyThisMatters")
            ?: obj.string("content")
            ?: ""

        val coreConcepts = obj.array("core_concepts").mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            val item = element.asJsonObject
            val concept = item.string("concept") ?: return@mapNotNull null
            val explanation = item.string("explanation") ?: ""
            concept to explanation
        }

        val examples = obj.array("examples").mapNotNull { element ->
            if (!element.isJsonObject) return@mapNotNull null
            val item = element.asJsonObject
            val type = item.string("type") ?: "example"
            val content = item.string("content") ?: ""
            type to content
        }

        val codeOrMath = obj.string("code_or_math") ?: obj.string("codeOrMath")

        ParsedInteractiveContent(
            title = title,
            whyThisMatters = why,
            coreConcepts = coreConcepts,
            examples = examples,
            codeOrMath = codeOrMath
        )
    } catch (_: Exception) {
        null
    }
}

private fun JsonObject.string(key: String): String? {
    val value = get(key) ?: return null
    if (value.isJsonNull) return null
    return runCatching { value.asString }.getOrNull()
}

private fun JsonObject.array(key: String) = getAsJsonArray(key)?.toList() ?: emptyList()

// =============================================================================
// PHASE 1: Content Reading View
// =============================================================================

@Composable
private fun ContentReadingView(
    content: String,
    title: String,
    quizReady: Boolean,
    onContinueToQuiz: () -> Unit,
    onOpenHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.primary.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MenuBook, "Read", tint = AppColors.primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Read & Learn", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.primary)
                    Text("Take your time to understand the material", fontSize = 12.sp, color = AppColors.textSecondary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    content,
                    fontSize = 15.sp,
                    color = AppColors.textPrimary,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Hub button
        OutlinedButton(
            onClick = onOpenHub,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Apps, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Learning Hub", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))

        // Continue to Quiz button
        Button(
            onClick = onContinueToQuiz,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
        ) {
            if (quizReady) {
                Text("Continue to Quiz", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
            } else {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Preparing Quiz...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// =============================================================================
// PHASE 2: Quiz Question View (Client-Side Verification)
// =============================================================================

@Composable
private fun QuizQuestionView(
    question: QuizQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: String?,
    textAnswer: String,
    answerResult: QuizAttemptResult?,
    heartsRemaining: Int,
    onSelectAnswer: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    onCheckAnswer: () -> Unit,
    onNext: () -> Unit,
    onOpenHub: () -> Unit,
    isLastQuestion: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAnswered = answerResult != null
    val isCorrect = answerResult?.isCorrect == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Question $questionNumber of $totalQuestions",
                color = AppColors.textSecondary,
                fontSize = 14.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (question.questionType) {
                    "mcq" -> AppColors.primary.copy(alpha = 0.1f)
                    else -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                }
            ) {
                Text(
                    if (question.questionType == "mcq") "Multiple Choice" else "Fill in the Blank",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = if (question.questionType == "mcq") AppColors.primary else Color(0xFFF59E0B)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { questionNumber.toFloat() / totalQuestions },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = AppColors.primary,
            trackColor = AppColors.primary.copy(alpha = 0.15f)
        )

        Spacer(Modifier.height(24.dp))

        // Question text
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    question.questionText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // MCQ options
        if (question.questionType == "mcq") {
            question.options.forEach { option ->
                val bgColor = when {
                    isAnswered && isCorrect && option == selectedAnswer -> AppColors.progressGreen.copy(alpha = 0.1f)
                    isAnswered && !isCorrect && option == selectedAnswer -> Color(0xFFEF4444).copy(alpha = 0.1f)
                    isAnswered && option.equals(answerResult?.correctAnswer, ignoreCase = true) -> AppColors.progressGreen.copy(alpha = 0.1f)
                    option == selectedAnswer -> AppColors.primary.copy(alpha = 0.08f)
                    else -> AppColors.surface
                }
                val borderColor = when {
                    isAnswered && isCorrect && option == selectedAnswer -> AppColors.progressGreen
                    isAnswered && !isCorrect && option == selectedAnswer -> Color(0xFFEF4444)
                    isAnswered && option.equals(answerResult?.correctAnswer, ignoreCase = true) -> AppColors.progressGreen
                    option == selectedAnswer -> AppColors.primary
                    else -> Color(0xFFE5E7EB)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isAnswered) { onSelectAnswer(option) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAnswered) {
                            val icon = when {
                                isCorrect && option == selectedAnswer -> Icons.Default.CheckCircle
                                !isCorrect && option == selectedAnswer -> Icons.Default.Cancel
                                option.equals(answerResult?.correctAnswer, ignoreCase = true) -> Icons.Default.CheckCircle
                                else -> null
                            }
                            icon?.let {
                                Icon(
                                    it, null,
                                    tint = if (option.equals(answerResult?.correctAnswer, ignoreCase = true) || (isCorrect && option == selectedAnswer))
                                        AppColors.progressGreen else Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                        }
                        Text(option, color = AppColors.textPrimary, fontSize = 15.sp)
                    }
                }
            }
        } else {
            // Fill in the blank
            OutlinedTextField(
                value = textAnswer,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnswered,
                label = { Text("Your answer") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // Result feedback (instant, client-side)
        AnimatedVisibility(visible = isAnswered) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) AppColors.progressGreen.copy(alpha = 0.08f)
                    else Color(0xFFEF4444).copy(alpha = 0.08f)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            null,
                            tint = if (isCorrect) AppColors.progressGreen else Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isCorrect) "Correct!" else "Incorrect",
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) AppColors.progressGreen else Color(0xFFEF4444),
                            fontSize = 16.sp
                        )
                    }
                    if (!isCorrect && answerResult?.explanation?.isNotBlank() == true) {
                        Spacer(Modifier.height(8.dp))
                        Text("\uD83D\uDCA1 ${answerResult.explanation}", color = AppColors.textSecondary, fontSize = 14.sp)
                    } else if (!isCorrect && answerResult?.hint?.isNotBlank() == true) {
                        Spacer(Modifier.height(8.dp))
                        Text("\uD83D\uDCA1 ${answerResult.hint}", color = AppColors.textSecondary, fontSize = 14.sp)
                    }
                    if (!isCorrect) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Correct answer: ${answerResult?.correctAnswer}",
                            color = AppColors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
        if (!isAnswered) {
            Button(
                onClick = onCheckAnswer,
                enabled = (question.questionType == "mcq" && selectedAnswer != null) ||
                        (question.questionType != "mcq" && textAnswer.isNotBlank()),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Check Answer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenHub,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Apps, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hub")
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Text(
                        if (isLastQuestion) "View Results" else "Next Question",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Hint toggle
        if (!isAnswered && question.hint.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            var showHint by remember { mutableStateOf(false) }
            TextButton(onClick = { showHint = !showHint }) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (showHint) "Hide Hint" else "Show Hint", color = Color(0xFFF59E0B))
            }
            AnimatedVisibility(visible = showHint) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Text(question.hint, modifier = Modifier.padding(12.dp), color = Color(0xFF92400E), fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// =============================================================================
// PHASE 3: Quiz Summary View
// =============================================================================

@Composable
private fun QuizSummaryView(
    results: List<QuizAttemptResult>,
    submitResult: SubmitQuizResponse?,
    heartsRemaining: Int,
    totalQuestions: Int,
    onNextSubtopic: () -> Unit,
    onBackToCourses: () -> Unit,
    onOpenHub: () -> Unit,
    hasCourseId: Boolean,
    modifier: Modifier = Modifier
) {
    val correctCount = results.count { it.isCorrect }
    val wrongCount = results.count { !it.isCorrect }
    val passed = heartsRemaining > 0
    val isPerfect = correctCount == totalQuestions
    val scorePercent = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPerfect) Color(0xFFF0FDF4) else if (passed) AppColors.primary.copy(alpha = 0.06f) else Color(0xFFFEF2F2)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isPerfect) Color(0xFF22C55E).copy(alpha = 0.15f)
                    else if (passed) AppColors.primary.copy(alpha = 0.1f)
                    else Color(0xFFEF4444).copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            when {
                                isPerfect -> Icons.Default.EmojiEvents
                                passed -> Icons.Default.CheckCircle
                                else -> Icons.Default.Cancel
                            },
                            "Result",
                            tint = when {
                                isPerfect -> Color(0xFFF59E0B)
                                passed -> AppColors.progressGreen
                                else -> Color(0xFFEF4444)
                            },
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    when {
                        isPerfect -> "Perfect Score!"
                        passed -> "Quiz Passed!"
                        else -> "Quiz Failed"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "$correctCount/$totalQuestions correct ($scorePercent%)",
                    fontSize = 18.sp,
                    color = AppColors.textSecondary
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBadge("Correct", "$correctCount", AppColors.progressGreen)
                    StatBadge("Wrong", "$wrongCount", Color(0xFFEF4444))
                    StatBadge("Hearts", "$heartsRemaining/3", Color(0xFFEF4444))
                    if (submitResult != null) {
                        StatBadge("XP", "+${submitResult.xpEarned}", Color(0xFFF59E0B))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Wrong answers with explanations
        val wrongResults = results.filter { !it.isCorrect }
        if (wrongResults.isNotEmpty()) {
            Text(
                "Review Wrong Answers",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(12.dp))

            wrongResults.forEachIndexed { index, result ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Q${index + 1}: ${result.questionText}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = AppColors.textPrimary,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Icon(Icons.Default.Cancel, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Your answer: ${result.userAnswer}", fontSize = 13.sp, color = Color(0xFFEF4444))
                        }
                        Spacer(Modifier.height(4.dp))
                        Row {
                            Icon(Icons.Default.CheckCircle, null, tint = AppColors.progressGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Correct: ${result.correctAnswer}", fontSize = 13.sp, color = AppColors.progressGreen)
                        }
                        if (result.explanation.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    "\uD83D\uDCA1 ${result.explanation}",
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 13.sp,
                                    color = Color(0xFF92400E),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Action buttons
        OutlinedButton(
            onClick = onOpenHub,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Apps, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Learning Hub", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))

        if (hasCourseId) {
            Button(
                onClick = onNextSubtopic,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Next Subtopic", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
        } else {
            OutlinedButton(
                onClick = onBackToCourses,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Courses", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 12.sp, color = AppColors.textSecondary)
    }
}

// =============================================================================
// Game Over / Course Completed Views
// =============================================================================

@Composable
private fun GameOverView(
    onStudyMaterials: () -> Unit,
    onRetry: () -> Unit,
    hasCourseId: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("\uD83D\uDC94 Game Over!", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFEF4444))
                Spacer(Modifier.height(12.dp))
                Text(
                    "You ran out of hearts. Review the study materials and try again!",
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onStudyMaterials,
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Study Materials") }
                    if (hasCourseId) {
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) { Text("Try Again") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCompletedView(
    onBackToCourses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.progressGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        "Trophy",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Course Completed!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "You've completed all the subtopics in this course. Great job!",
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBackToCourses,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Back to Courses", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
