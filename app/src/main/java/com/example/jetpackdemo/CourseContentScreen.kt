

package com.example.jetpackdemo

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetpackdemo.data.model.*
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.CourseViewModel
import com.example.jetpackdemo.viewmodels.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import androidx.compose.foundation.background



// UI Data classes
data class SubTopicContent(
    val id: String,
    val title: String,
    val readTimeMinutes: Int,
    val content: String,
    val videos: List<UiVideo>
)

data class UiVideo(
    val title: String,
    val thumbnailUrl: String,
    val youtubeUrl: String
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseContentScreen(
    courseViewModel: CourseViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val courseId by courseViewModel.courseId.collectAsState()
    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()

    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }


    val streamingEvents by courseViewModel.streamingEvents.collectAsState(initial = null)
    val generationProgress by courseViewModel.generationProgress.collectAsState(initial = null)
    val isGeneratingContent by courseViewModel.isGeneratingContent.collectAsState()
    val streamingTextPreview by courseViewModel.currentStreamingText.collectAsState()
    val streamingSubtopicPreview by courseViewModel.currentStreamingSubtopic.collectAsState()
    val isPollingActive by courseViewModel.isPollingActive.collectAsState()

    val isStreamingProvider by courseViewModel.isStreamingProvider.collectAsState()

    // Clear state when back is pressed or screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            courseViewModel.clearCourseState()
        }
    }

    // Override the back button to clear state
    BackHandler {
        courseViewModel.clearCourseState()
        onNavigateBack()
    }

    // Reset when course changes
    // FIXED: Only fetch course content when NOT generating and courseId is available
    LaunchedEffect(courseId, isGeneratingContent) {
        if (courseId != null && !isGeneratingContent) {
            courseViewModel.getFullCourseContent(courseId!!)
        }
    }

    // Handle streaming events (avoid duplicating complete handling; it's owned by the ViewModel)
    // Handle streaming events
    LaunchedEffect(streamingEvents) {
        streamingEvents?.let { event ->
            when (event.type) {
                "progress" -> courseViewModel.updateProgress(event)
                "error" -> {
                    Log.e("UI_STREAMING", "Streaming error: ${event.message}")
                    courseViewModel.stopStreaming()
                }
                "complete" -> {
                    Log.d("UI_STREAMING", "Streaming completed in UI")
                    // No need to do anything - ViewModel handles this
                }
            }
        }
    }

    // Auto-select first available subtopic with content - FIXED
//    LaunchedEffect(fullCourseContent) {
//        when (fullCourseContent) {
//            is Resource.Success -> {
//                val data = (fullCourseContent as Resource.Success<CourseFullResponse>).data
//                if (selectedSubtopic == null) {
//                    val firstContent = data!!.units
//                        .flatMap { it.subtopics }
//                        .firstOrNull { it.content != null && it.contentGeneratedAt != null }
//
//                    selectedSubtopic = firstContent
//                }
//            }
//            else -> { /* Do nothing */ }
//        }
//    }
    // FIXED: Auto-select logic - only run when we have content
    LaunchedEffect(fullCourseContent) {
        when (fullCourseContent) {
            is Resource.Success -> {
                val data = (fullCourseContent as Resource.Success<CourseFullResponse>).data
                if (selectedSubtopic == null) {
                    val firstContent = data!!.units
                        .flatMap { it.subtopics }
                        .firstOrNull {
                            // FIXED: content is now a typed object, not a string
                            it.content != null
                        }

                    if (firstContent != null) {
                        selectedSubtopic = firstContent
                        Log.d("UI_AUTO_SELECT", "✅ Auto-selected subtopic: ${firstContent.title}")
                    } else {
                        Log.d("UI_AUTO_SELECT", "❌ No valid content found in subtopics")
                        // Debug: log all subtopics
                        data.units.flatMap { it.subtopics }.forEach { sub ->
                            Log.d("UI_AUTO_SELECT", "Subtopic: ${sub.title}, Has content: ${sub.content != null}")
                        }
                    }
                }
            }
            else -> { /* Do nothing */ }
        }
    }


    // Get current content for selected subtopic - FIXED
    // FIXED: Get current content for selected subtopic
    val currentContent = remember(selectedSubtopic, fullCourseContent) {
        when (fullCourseContent) {
            is Resource.Success -> {
                val data = (fullCourseContent as Resource.Success<CourseFullResponse>).data
                selectedSubtopic?.let { sub ->
                    data!!.units
                        .flatMap { it.subtopics }
                        .find { it.id == sub.id }
                        ?.let {
                            // FIXED: content is now a typed object, just check for null
                            if (it.content != null) {
                                convertToSubTopicContent(it)
                            } else {
                                null
                            }
                        }
                }
            }
            else -> null
        }
    }

    // Check if we have any content ready to show
    val hasContentToShow = currentContent != null

    // Recompute generating state: only show generating UI when there's no content yet
//    val isGenerating =
//        (isGeneratingContent ||
//        generationProgress != null ||
//        courseViewModel.isPollingActive() ||
//        streamingSubtopicPreview != null ||
//        streamingTextPreview.isNotEmpty()) &&
//        !hasContentToShow
    val isGenerating = (isGeneratingContent || isPollingActive) && !hasContentToShow // Get course title safely - FIXED
    val courseTitle = remember(fullCourseContent) {
        when (fullCourseContent) {
            is Resource.Success -> (fullCourseContent as Resource.Success<CourseFullResponse>).data!!.course.title
            else -> "Course"
        }
    }


    // Debug logging
    LaunchedEffect(isGenerating, hasContentToShow, isGeneratingContent, isPollingActive) {
        Log.d("UI_STATE",
            "isGenerating: $isGenerating, " +
                    "hasContentToShow: $hasContentToShow, " +
                    "isGeneratingContent: $isGeneratingContent, " +
                    "isPollingActive: $isPollingActive"
        )
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentContent?.title ?: "Course Content",
                        color = AppColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (hasContentToShow) {
                FloatingActionButton(
                    onClick = { showSheet = true },
                    containerColor = AppColors.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.List, contentDescription = "Topics")
                }
            }
        },
//        bottomBar = {
//            if (currentContent != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp,
//                    actions = {
//                        Button(
//                            onClick = { /* Mark Complete */ },
//                            modifier = Modifier.weight(1f).height(48.dp),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Icon(Icons.Default.Check, contentDescription = null)
//                            Spacer(Modifier.width(8.dp))
//                            Text("Mark Complete", fontWeight = FontWeight.SemiBold)
//                        }
//                        Spa cer(Modifier.width(12.dp))
//                        OutlinedButton(onClick = { /* Save Note */ }, modifier = Modifier.height(48.dp)) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
//                        }
//                    }
//                )
//            }
//        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {// FIXED: Show content if available, regardless of generation status
                currentContent != null -> {
                    CourseContentDisplay(
                        content = currentContent,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                isGenerating -> {
                    // Provider-aware generating state
                    if (isStreamingProvider) {
                        GeneratingStateWithStreaming(
                            courseTitle = courseTitle,
                            progress = generationProgress,
                            viewModel = courseViewModel,
                            modifier = Modifier.padding(paddingValues)
                        )
                    } else {
                        // Simple loader for Cerebras (no streaming card)
                        SimpleGeneratingState(
                            courseTitle = courseTitle,
                            progress = generationProgress,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
                fullCourseContent is Resource.Loading -> {
                    LoadingState(
                        "Loading course...",
                        Modifier.padding(paddingValues)
                    )
                }
                fullCourseContent is Resource.Error -> {
                    val errorMessage = when (fullCourseContent) {
                        is Resource.Error -> (fullCourseContent as Resource.Error<CourseFullResponse>).message ?: "Failed to load"
                        else -> "Failed to load"
                    }
                    ErrorState(
                        message = errorMessage,
                        onRetry = { courseId?.let { courseViewModel.getFullCourseContent(it) } },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No content available", color = AppColors.textSecondary)
                    }
                }
            }
        }

        // Bottom Sheet - FIXED
        // FIXED: Bottom Sheet - better content detection
        if (showSheet) {
            when (fullCourseContent) {
                is Resource.Success -> {
                    val courseData = (fullCourseContent as Resource.Success<CourseFullResponse>).data
                    ModalBottomSheet(
                        onDismissRequest = { showSheet = false },
                        sheetState = sheetState
                    ) {
                        TopicNavigationSheet(
                            courseData = courseData!!,
                            selectedSubtopicId = selectedSubtopic?.id,
                            onSubTopicSelected = { sub ->
                                // FIXED: content is now a typed object
                                val hasValidContent = sub.content != null

                                if (hasValidContent) {
                                    selectedSubtopic = sub
                                    showSheet = false
                                    Log.d("UI_SHEET", "✅ Selected subtopic: ${sub.title}")
                                } else {
                                    Log.d("UI_SHEET", "❌ Cannot select - no valid content: ${sub.title}")
                                }
                            },
                            onDismiss = { showSheet = false }
                        )
                    }
                }
                else -> { /* Don't show sheet if no data */ }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { courseViewModel.stopStreaming() }
    }
}
@Composable
fun GeneratingStateWithStreaming(
    courseTitle: String,
    progress: CourseViewModel.GenerationProgress?,
    viewModel: CourseViewModel,
    modifier: Modifier = Modifier
) {
    val streamingText by viewModel.currentStreamingText.collectAsState()
    val currentSubtopic by viewModel.currentStreamingSubtopic.collectAsState()
    val isGeneratingContent by viewModel.isGeneratingContent.collectAsState()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            CircularProgressIndicator(color = AppColors.primary, strokeWidth = 6.dp)
            Spacer(Modifier.height(24.dp))
            Text("Generating: $courseTitle", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)

            // FIXED: Always show progress if available, even if no streaming text yet
            progress?.let {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { it.progress / 100f },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    color = AppColors.primary,
                    trackColor = AppColors.surface
                )
                Spacer(Modifier.height(8.dp))
                Text(it.subtopic, color = AppColors.textSecondary, fontSize = 14.sp)
                Text("${it.generated}/${it.total} subtopics", color = AppColors.textSecondary)
            }

            // FIXED: Show streaming preview if we have ANY content OR if we're actively generating
            if (currentSubtopic != null || streamingText.isNotEmpty() || isGeneratingContent) {
                Spacer(Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            currentSubtopic ?: "Starting generation...",
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.primary,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        val scrollState = rememberScrollState()

                        LaunchedEffect(streamingText) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }

                        Text(
                            text = streamingText.ifEmpty { "Connecting to AI service...\nContent will appear here shortly." },
                            color = AppColors.textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState)
                        )
                    }
                }
            } else {
                // Show minimal status when waiting for initial connection
                Spacer(Modifier.height(16.dp))
                Text("Initializing generation...", color = AppColors.textSecondary)
            }
        }
    }
}

@Composable
fun SimpleGeneratingState(
    courseTitle: String,
    progress: CourseViewModel.GenerationProgress?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = AppColors.primary,
                strokeWidth = 6.dp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Generating: $courseTitle",
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )

            progress?.let {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { it.progress / 100f },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    color = AppColors.primary,
                    trackColor = AppColors.surface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${it.generated}/${it.total} subtopics",
                    color = AppColors.textSecondary
                )
                Text(
                    "Processing: ${it.subtopic}",
                    color = AppColors.textSecondary,
                    fontSize = 14.sp
                )
            } ?: run {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Preparing batch generation...",
                    color = AppColors.textSecondary
                )
            }
        }
    }
}


@Composable
fun CourseContentDisplay(
    content: SubTopicContent,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                content.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Read time",
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${content.readTimeMinutes} min read",
                    color = AppColors.textSecondary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Display formatted content
            FormattedContentDisplay(content.content)

            Spacer(modifier = Modifier.height(24.dp))
            if (content.videos.isNotEmpty()) {
                Text(
                    "Watch Related Videos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(content.videos) { video ->
                    VideoThumbnailCard(video)
                }
            }
        }
    }
}



@Composable
fun FormattedContentDisplay(content: String) {
    val lines = content.split("\n")
    var currentSection = ""
    val codeBuffer = StringBuilder() // collect code lines

    Column(modifier = Modifier.padding(8.dp)) {
        lines.forEachIndexed { index, line ->
            when {
                line.startsWith("Why this matters:") -> {
                    currentSection = "why_matters"
                    Text(
                        "Why this matters:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                line.startsWith("Core Concepts:") -> {
                    currentSection = "core_concepts"
                    Text(
                        "Core Concepts:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                line.startsWith("Examples:") -> {
                    currentSection = "examples"
                    Text(
                        "Examples:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                line.startsWith("Code Example:") -> {
                    currentSection = "code"
                    codeBuffer.clear() // reset buffer
                    Text(
                        "Code Example:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                currentSection == "code" -> {
                    codeBuffer.appendLine(line) // collect all code lines
                }
                line.startsWith("•") && currentSection == "core_concepts" -> {
                    val parts = line.removePrefix("•").split(":", limit = 2)
                    if (parts.size == 2) {
                        val concept = parts[0].trim()
                        val explanation = parts[1].trim()
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                "• $concept:",
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.textPrimary
                            )
                            Text(
                                explanation,
                                color = AppColors.textSecondary,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    } else {
                        Text(line, color = AppColors.textSecondary)
                    }
                }
                line.matches(Regex("\\d+\\. \\[.*\\]:.*")) && currentSection == "examples" -> {
                    val parts = line.split("]:", limit = 2)
                    if (parts.size == 2) {
                        val numberAndType = parts[0].removePrefix("[").split(" [")
                        val exampleContent = parts[1].trim()
                        if (numberAndType.size == 2) {
                            val number = numberAndType[0]
                            val type = numberAndType[1]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "$number [${type.replaceFirstChar { it.uppercase() }}]:",
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.textPrimary
                                    )
                                    Text(
                                        exampleContent,
                                        color = AppColors.textSecondary,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                        fontStyle = if (type.contains("analog", ignoreCase = true))
                                            FontStyle.Italic else FontStyle.Normal,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
                line.isNotBlank() -> {
                    Text(
                        line,
                        color = when (currentSection) {
                            "why_matters" -> AppColors.textSecondary
                            else -> AppColors.textPrimary
                        },
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
            }

            // After finishing last line or switching section, render collected code
            if ((currentSection != "code" || index == lines.lastIndex) && codeBuffer.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Text(
                        codeBuffer.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                codeBuffer.clear() // reset after rendering
            }
        }
    }
}



//@Composable
//fun VideoThumbnailCard(video: UiVideo) {  // Changed parameter type to UiVideo
//    val context = LocalContext.current
//    var showVideoDialog by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier.width(200.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(video.thumbnailUrl)
//                    .crossfade(true)
//                    .build(),
//                contentDescription = video.title,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(110.dp)
//                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
//                contentScale = ContentScale.Crop
//            )
//            Text(
//                text = video.title,
//                modifier = Modifier.padding(12.dp),
//                fontWeight = FontWeight.SemiBold,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//
//    if (showVideoDialog) {
//        AlertDialog(
//            onDismissRequest = { showVideoDialog = false },
//            title = { Text(video.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
//            text = {
//                Column {
//                    Text(
//                        "Open this video in YouTube?",
//                        color = AppColors.textSecondary
//                    )
//                }
//            },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        // Convert thumbnail URL to watch URL
//                        val videoId = video.thumbnailUrl.substringAfter("vi/").substringBefore("/")
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
//                        context.startActivity(intent)
//                    }
//                ) {
//                    Text("Watch on YouTube")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showVideoDialog = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//}

@Composable
fun VideoThumbnailCard(video: UiVideo) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Play button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color.Red.copy(alpha = 0.9f),
                                    Color.Red.copy(alpha = 0.7f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // YouTube badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    color = Color.Red,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "YouTube",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = AppColors.textPrimary,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = AppColors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Tap to watch",
                        color = AppColors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Watch on YouTube",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        video.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = AppColors.textPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "This will open YouTube to play the video.",
                        color = AppColors.textSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        openYouTubeVideo(context, video.youtubeUrl)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Watch Now", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            },
            containerColor = AppColors.surface
        )
    }
}

// STEP 3: Add this helper function (if not already added)
private fun openYouTubeVideo(context: android.content.Context, videoUrl: String) {
    val videoId = extractVideoId(videoUrl)

    if (videoId != null) {
        // Try YouTube app first
        try {
            val appIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("vnd.youtube:$videoId")
            )
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(appIntent)
            Log.d("YOUTUBE", "✅ Opened in YouTube app")
        } catch (e: Exception) {
            // Fallback to browser
            try {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=$videoId")
                )
                context.startActivity(webIntent)
                Log.d("YOUTUBE", "✅ Opened in browser")
            } catch (e2: Exception) {
                Log.e("YOUTUBE", "❌ Failed to open: ${e2.message}")
                android.widget.Toast.makeText(
                    context,
                    "Cannot open video",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    } else {
        // Just try the URL directly
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("YOUTUBE", "❌ Invalid URL: $videoUrl")
        }
    }
}

// STEP 4: Add extractVideoId helper (if not already added)
private fun extractVideoId(url: String): String? {
    return try {
        when {
            url.contains("youtube.com/watch?v=") -> {
                url.substringAfter("v=").substringBefore("&")
            }
            url.contains("youtu.be/") -> {
                url.substringAfter("youtu.be/").substringBefore("?")
            }
            url.contains("youtube.com/embed/") -> {
                url.substringAfter("embed/").substringBefore("?")
            }
            url.contains("m.youtube.com/watch?v=") -> {
                url.substringAfter("v=").substringBefore("&")
            }
            else -> null
        }
    } catch (e: Exception) {
        Log.e("VIDEO_ID", "Failed to extract video ID from: $url", e)
        null
    }
}


@Composable
fun TopicNavigationSheet(
    courseData: CourseFullResponse,
    selectedSubtopicId: String?,
    onSubTopicSelected: (Subtopic) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedUnitId by remember { mutableStateOf<String?>(null) }

    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        items(courseData.units) { unit ->
            UnitItemView(
                unit = unit,
                selectedSubtopicId = selectedSubtopicId,
                isExpanded = expandedUnitId == unit.id,
                onExpand = {
                    expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
                },
                onSubTopicSelected = onSubTopicSelected
            )
        }
    }
}

@Composable
fun UnitItemView(
    unit: UnitWithSubtopics,
    selectedSubtopicId: String?,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onSubTopicSelected: (Subtopic) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpand)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(unit.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand"
            )
        }
        if (isExpanded) {
            unit.subtopics.forEach { subTopic ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = subTopic.content != null) {
                            if (subTopic.content != null) {
                                onSubTopicSelected(subTopic)
                            }
                        }
                        .padding(start = 32.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (subTopic.content != null) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Content available",
                            tint = AppColors.progressGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Generating content",
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subTopic.title,
                        color = when {
                            subTopic.id == selectedSubtopicId -> AppColors.primary
                            subTopic.content == null -> AppColors.textSecondary
                            else -> AppColors.textPrimary
                        },
                        fontWeight = if (subTopic.id == selectedSubtopicId) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (subTopic.content == null) FontStyle.Italic else FontStyle.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = AppColors.textSecondary)
        }
    }
}

@Composable
fun GeneratingState(
    courseTitle: String,
    generationStatus: Resource<GenerationStatusResponse>?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Generating content for: $courseTitle", color = AppColors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            if (generationStatus is Resource.Success) {
                val status = generationStatus.data
                if (status != null) {
                    Text(
                        "Completed: ${status.generatedSubtopics}/${status.totalSubtopics} subtopics",
                        color = AppColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Status: ${status.status}", color = AppColors.textSecondary)
                }
            } else if (generationStatus is Resource.Loading) {
                Text("Checking generation status...", color = AppColors.textSecondary)
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Error, contentDescription = "Error")
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = AppColors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private fun normalizeBackendString(input: String?): String? {
    return input
        ?.replace("\\n", "\n")
        ?.replace("\\t", "\t")
        ?.replace("\\\"", "\"")
        ?.replace("\\\\", "\\")
        ?.trim()
}

private fun normalizeGeneratedContent(original: GeneratedSubtopicContent): GeneratedSubtopicContent {
    return original.copy(
        subtopicTitle = normalizeBackendString(original.subtopicTitle) ?: "",
        title = normalizeBackendString(original.title) ?: "",
        whyThisMatters = normalizeBackendString(original.whyThisMatters) ?: "",
        coreConcepts = original.coreConcepts.map { concept ->
            concept.copy(explanation = normalizeBackendString(concept.explanation) ?: "")
        },
        examples = original.examples.map { example ->
            example.copy(content = normalizeBackendString(example.content) ?: "")
        },
        codeOrMath = normalizeBackendString(original.codeOrMath),
        youtubeKeywords = original.youtubeKeywords // leave as is
    )
}

private fun convertToSubTopicContent(subtopic: Subtopic): SubTopicContent? {
    return try {
        // Content is now a typed GeneratedSubtopicContent object
        val parsedContent = subtopic.content
        if (parsedContent == null) {
            Log.e("CONTENT_CONVERSION", "No content for: ${subtopic.title}")
            return null
        }

        // Create a normalized copy
        val generatedContent = normalizeGeneratedContent(parsedContent)

        val readTimeMinutes = estimateReadTime(generatedContent.whyThisMatters ?: "")

        SubTopicContent(
            id = subtopic.id,
            title = generatedContent.subtopicTitle,
            readTimeMinutes = readTimeMinutes,
            content = buildString {
                append("${generatedContent.title}\n\n")
                append("Why this matters:\n${generatedContent.whyThisMatters}\n\n")

                if (generatedContent.coreConcepts.isNotEmpty()) {
                    append("Core Concepts:\n")
                    generatedContent.coreConcepts.forEach { concept ->
                        append("• ${concept.concept}: ${concept.explanation}\n")
                    }
                }

                if (generatedContent.examples.isNotEmpty()) {
                    append("\nExamples:\n")
                    generatedContent.examples.forEachIndexed { index, example ->
                        val formattedType = example.type.replace("_", " ")
                            .replaceFirstChar { it.uppercase() }
                        append("${index + 1}. [$formattedType]: ${example.content}\n")
                    }
                }

                if (!generatedContent.codeOrMath.isNullOrBlank()) {
                    append("\nCode Example:\n${generatedContent.codeOrMath}")
                }
            },
            videos = subtopic.videos.map { video ->
                UiVideo(video.title, video.thumbnail, video.youtubeUrl)
            }
        )
    } catch (e: Exception) {
        Log.e("CONTENT_CONVERSION", "Error converting content for ${subtopic.title}: ${e.message}", e)
        null
    }
}


// Helper functions
//private fun convertToSubTopicContent(subtopic: Subtopic): SubTopicContent {
//    val generatedContent = parseGeneratedContent(subtopic.content)
//    val readTimeMinutes = estimateReadTime(generatedContent?.whyThisMatters ?: "")
//
//    return SubTopicContent(
//        id = subtopic.id,
//        title = subtopic.title,
//        readTimeMinutes = readTimeMinutes,
//        content = generatedContent?.let {
//            buildString {
//                append("${it.title}\n\n")
//                append("Why this matters:\n${it.whyThisMatters}\n\n")
//                append("Core Concepts:\n")
//                it.coreConcepts.forEach { concept ->
//                    append("• ${concept.concept}: ${concept.explanation}\n")
//                }
//                if (it.examples.isNotEmpty()) {
//                    append("\nExamples:\n")
//                    it.examples.forEachIndexed { index, example ->
//                        val formattedType = example.type.replace("_", " ").lowercase()
//                        append("${index + 1}. [$formattedType]: ${example.content}\n")
//                    }
//                }
//                it.codeOrMath?.let { code ->
//                    append("\nCode Example:\n$code")
//                }
//            }
//        } ?: "Content is being generated... Please check back soon.",
//        videos = subtopic.videos.map { video ->
//            UiVideo(video.title, video.thumbnail)
//        }
//    )
//}

private fun estimateReadTime(text: String): Int {
    val wordsPerMinute = 200
    val wordCount = text.split("\\s+".toRegex()).size
    return maxOf(1, wordCount / wordsPerMinute)
}

private fun parseGeneratedContent(jsonString: String?): GeneratedSubtopicContent? {
    if (jsonString == null) return null
    return try {
        // FIXED: Handle JSON array case
        if (jsonString.trim().startsWith("[")) {
            // It's a JSON array - parse as list and take first element
            val listType = object : TypeToken<List<GeneratedSubtopicContent>>() {}.type
            val contentList = Gson().fromJson<List<GeneratedSubtopicContent>>(jsonString, listType)
            contentList.firstOrNull()
        } else {
            // It's a single object
            Gson().fromJson(jsonString, GeneratedSubtopicContent::class.java)
        }
    } catch (e: Exception) {
        Log.e("CONTENT_CONVERSION", "Parse error for: ${jsonString.take(100)}", e)
        null
    }
}