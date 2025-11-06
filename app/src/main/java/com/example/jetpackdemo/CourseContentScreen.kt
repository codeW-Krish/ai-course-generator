//package com.example.jetpackdemo
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.example.jetpackdemo.ui.theme.AppColors
//
//// --- Data Models for this screen ---
//data class Video(val title: String, val thumbnailUrl: String)
//
//data class SubTopicContent(
//    val id: Int,
//    val title: String,
//    val readTimeMinutes: Int,
//    val content: String,
//    val videos: List<Video>
//)
//
//// --- Expanded Mock Data with longer text explanations ---
//private val allSubTopicContents = mapOf(
//    101 to SubTopicContent(
//        id = 101,
//        title = "What is Machine Learning?",
//        readTimeMinutes = 8,
//        content = "Machine Learning is a transformative subset of artificial intelligence (AI) that empowers computers to learn from data and make decisions without being explicitly programmed for every single scenario. Instead of relying on a rigid set of hard-coded rules, machine learning algorithms are designed to identify patterns, relationships, and insights within large datasets.\n\nThink of it like teaching a child to recognize a cat. You don't list out every possible feature of a cat (pointy ears, whiskers, four legs, a tail). Instead, you show them hundreds of pictures of different cats. Over time, the child's brain learns to identify the underlying patterns that define a 'cat' on its own. Machine learning works in a similar fashion; we feed algorithms vast amounts of data, and they learn to perform tasks like classification, prediction, and clustering by recognizing these inherent patterns.\n\nThis capability is revolutionary because it allows us to solve problems that are too complex or would change too frequently for traditional programming. From recommending movies you might like, to detecting fraudulent credit card transactions, machine learning is the engine behind many of the intelligent systems we interact with every day.",
//        videos = listOf(
//            Video("Machine Learning Explained", "https://img.youtube.com/vi/ukzFI9rgMRA/0.jpg"),
//            Video("Intro to AI", "https://img.youtube.com/vi/ad79nYk2keg/0.jpg")
//        )
//    ),
//    102 to SubTopicContent(
//        id = 102,
//        title = "Types of ML Algorithms",
//        readTimeMinutes = 12,
//        content = "Machine learning algorithms are generally categorized into three main types: Supervised, Unsupervised, and Reinforcement Learning. The choice of which to use depends heavily on the nature of the problem you're trying to solve and the kind of data you have available.\n\nSupervised Learning is the most common type. In this approach, the algorithm is trained on a labeled dataset, meaning each piece of data is tagged with a correct answer or outcome. The goal is for the algorithm to learn the mapping function that can predict the output for new, unseen data. Common examples include spam detection in emails (labeled as 'spam' or 'not spam') and predicting house prices based on features like size and location (labeled with the actual sale price).\n\nUnsupervised Learning, on the other hand, deals with unlabeled data. The algorithm tries to find patterns and structures within the data on its own, without any predefined outcomes. This is often used for exploratory data analysis. Key applications include customer segmentation (grouping customers with similar purchasing habits) and anomaly detection (finding unusual data points that don't fit with the rest of the dataset).",
//        videos = listOf(Video("ML Algorithms Overview", "https://img.youtube.com/vi/I74o3p_fK_c/0.jpg"))
//    ),
//    201 to SubTopicContent(
//        id = 201,
//        title = "Data Cleaning",
//        readTimeMinutes = 10,
//        content = "Data cleaning, also known as data cleansing or data scrubbing, is the process of detecting and correcting (or removing) corrupt, inaccurate, or irrelevant records from a dataset. It is arguably one of the most important and time-consuming steps in the entire machine learning workflow, as the quality of the data directly and significantly impacts the quality of the model's predictions. The principle of 'garbage in, garbage out' is especially true in machine learning.\n\nThis process involves several key tasks. One of the most common is handling missing values, where some data points are empty. A data scientist might choose to fill these gaps with the mean or median value of the column, or in some cases, remove the entire row if the missing data is too significant. Another task is correcting structural errors, such as typos or inconsistent naming conventions (e.g., 'New York' vs. 'NY').\n\nFinally, data cleaning also involves identifying and handling outliers—data points that are significantly different from other observations. These can be legitimate data points or errors, and deciding how to treat them requires careful domain knowledge. By performing these steps, we ensure that the machine learning model is trained on accurate, consistent, and reliable data, leading to more trustworthy results.",
//        videos = emptyList()
//    ),
//    202 to SubTopicContent(
//        id = 202,
//        title = "Feature Scaling",
//        readTimeMinutes = 7,
//        content = "Feature scaling is a critical data preprocessing step used to normalize the range of independent variables or features of data. When a dataset contains features that vary widely in magnitudes, units, and range, some machine learning algorithms might not perform correctly. For instance, algorithms that compute the distance between data points, like K-Nearest Neighbors (KNN) or Support Vector Machines (SVM), are highly sensitive to the scale of the data.\n\nA feature with a broad range of values can dominate the distance calculation, making the model biased towards that feature. To prevent this, we scale the features to bring them all into a similar range. The two most common methods of feature scaling are Normalization and Standardization.\n\nNormalization (or Min-Max Scaling) scales the data to a fixed range, usually 0 to 1. Standardization, on the other hand, transforms the data to have a mean of 0 and a standard deviation of 1. The choice between them depends on the algorithm and the distribution of the data, but performing one of them is often essential for building a high-performing model.",
//        videos = listOf(Video("Normalization vs Standardization", "https://img.youtube.com/vi/mnk_N_fL-5E/0.jpg"))
//    )
//)
//
//private val mockUnitsForContent = listOf(
//    UnitItem(1, "Unit 1: Getting Started", 3,  listOf(
//        SubTopic(101, "What is Machine Learning?"),
//        SubTopic(102, "Types of ML Algorithms"),
//    )),
//    UnitItem(2, "Unit 2: Data Preprocessing", 4, listOf(
//        SubTopic(201, "Data Cleaning"),
//        SubTopic(202, "Feature Scaling"),
//    ))
//)
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(onNavigateBack: () -> Unit) {
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var expandedUnitId by remember { mutableStateOf<Int?>(null) }
//    var currentSubTopic by remember { mutableStateOf(allSubTopicContents.values.first()) }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = { Text(currentSubTopic.title, color = AppColors.textPrimary, maxLines = 1) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Handle more options */ }) {
//                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = AppColors.textPrimary)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { showSheet = true },
//                containerColor = AppColors.primary,
//                contentColor = AppColors.onPrimary,
//                shape = CircleShape
//            ) {
//                Icon(Icons.Default.List, contentDescription = "View Topics")
//            }
//        },
//        bottomBar = {
//            BottomAppBar(
//                containerColor = AppColors.surface,
//                tonalElevation = 8.dp,
//                actions = {
//                    Button(
//                        onClick = { /* Mark as Complete */ },
//                        modifier = Modifier.weight(1f).height(48.dp),
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Icon(Icons.Default.Check, contentDescription = null)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Mark Complete")
//                    }
//                    Spacer(modifier = Modifier.width(12.dp))
//                    OutlinedButton(
//                        onClick = { /* Save Note */ },
//                        modifier = Modifier.height(48.dp)
//                    ) {
//                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Note")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item {
//                Text(currentSubTopic.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
//                Spacer(modifier = Modifier.height(8.dp))
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Default.Schedule, contentDescription = "Read time", tint = AppColors.textSecondary, modifier = Modifier.size(16.dp))
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("${currentSubTopic.readTimeMinutes} min read", color = AppColors.textSecondary, fontSize = 14.sp)
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(currentSubTopic.content, color = AppColors.textPrimary, fontSize = 16.sp, lineHeight = 24.sp)
//                Spacer(modifier = Modifier.height(24.dp))
//                if (currentSubTopic.videos.isNotEmpty()) {
//                    Text("Watch Related Videos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//            }
//            item {
//                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                    items(currentSubTopic.videos) { video ->
//                        VideoThumbnailCard(video)
//                    }
//                }
//            }
//        }
//
//        if (showSheet) {
//            ModalBottomSheet(
//                onDismissRequest = { showSheet = false },
//                sheetState = sheetState
//            ) {
//                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
//                    items(mockUnitsForContent) { unit ->
//                        UnitItemView(
//                            unit = unit,
//                            isExpanded = expandedUnitId == unit.id,
//                            onExpand = {
//                                expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
//                            },
//                            onSubTopicSelected = { subTopic ->
//                                // Find the new content from our mock data map
//                                val newContent = allSubTopicContents[subTopic.id]
//                                if (newContent != null) {
//                                    currentSubTopic = newContent
//                                }
//                                showSheet = false
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun VideoThumbnailCard(video: Video) {
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
//                maxLines = 2
//            )
//        }
//    }
//}
//
//@Composable
//fun UnitItemView(
//    unit: UnitItem,
//    isExpanded: Boolean,
//    onExpand: () -> Unit,
//    onSubTopicSelected: (SubTopic) -> Unit
//) {
//    Column {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable(onClick = onExpand)
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(unit.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
//            Icon(
//                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                contentDescription = "Expand"
//            )
//        }
//        if (isExpanded) {
//            unit.subTopics.forEach { subTopic ->
//                Text(
//                    text = subTopic.title,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { onSubTopicSelected(subTopic) }
//                        .padding(start = 32.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
//                    color = AppColors.textSecondary
//                )
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true, device = "id:pixel_4")
//@Composable
//fun CourseContentScreenPreview() {
//    CourseContentScreen(onNavigateBack = {})
//}
//package com.example.jetpackdemo
//
//import android.content.Intent
//import android.net.Uri
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material.icons.filled.Error
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.BottomSheetDefaults
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
////import androidx.room.compiler.processing.util.Resource
//import coil.compose.AsyncImage
//import com.example.jetpackdemo.data.model.CourseFullResponse
//import com.example.jetpackdemo.data.model.GeneratedSubtopicContent
//import com.example.jetpackdemo.data.model.Subtopic
//import com.example.jetpackdemo.data.model.UnitWithSubtopics
//import com.example.jetpackdemo.data.model.Video
//import com.example.jetpackdemo.ui.theme.AppColors
//import com.example.jetpackdemo.ui.viewmodel.CourseViewModel
//import com.example.jetpackdemo.ui.viewmodel.Resource
//import com.google.gson.Gson
////import kotlinx.coroutines.DefaultExecutor.run
//import kotlinx.coroutines.Job
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val generationStatus by courseViewModel.generationStatus.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
//
//    var pollingStarted by remember { mutableStateOf(false) }
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState()
//    var showBottomSheet by remember { mutableStateOf(false) }
//
//    // Start polling when screen is launched
//    LaunchedEffect(courseId) {
//        if (courseId != null && !pollingStarted) {
//            courseViewModel.startPollingGenerationStatus(courseId!!)
//            pollingStarted = true
//        }
//    }
//
//    // Stop polling when leaving screen
//    DisposableEffect(Unit) {
//        onDispose {
//            courseViewModel.stopPolling()
//        }
//    }
//
//    // Check if generation is complete and load full content
//    LaunchedEffect(generationStatus) {
//        generationStatus?.let { resource ->
//            when (resource) {
//                is Resource.Success -> {
//                    val status = resource.data
//                    if (status != null) {
//                        if (status.status == "completed" && courseId != null) {
//                            courseViewModel.getFullCourseContent(courseId!!)
//                        }
//                    }
//                }
//                else -> {}
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Course Content", color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        }
//    ) { paddingValues ->
//        when {
//            fullCourseContent is Resource.Success -> {
//                val courseData = (fullCourseContent as Resource.Success).data
//                if (courseData != null) {
//                    CourseContentDisplay(
//                        courseData = courseData,
//                        onSubtopicClick = { subtopic ->
//                            selectedSubtopic = subtopic
//                            showBottomSheet = true
//                        },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//            }
//
//            generationStatus is Resource.Loading -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        CircularProgressIndicator(color = AppColors.primary)
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text("Generating course content...", color = AppColors.textSecondary)
//                    }
//                }
//            }
//
//            generationStatus is Resource.Success -> {
//                val status = (generationStatus as Resource.Success).data
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        CircularProgressIndicator(color = AppColors.primary)
//                        Spacer(modifier = Modifier.height(16.dp))
//                        if (status != null) {
//                            Text("Generating: ${status.generatedSubtopics}/${status.totalSubtopics} subtopics",
//                                color = AppColors.textSecondary)
//                        }
//                        Spacer(modifier = Modifier.height(8.dp))
//                        if (status != null) {
//                            Text("Status: ${status.status}", color = AppColors.textSecondary)
//                        }
//                    }
//                }
//            }
//
//            generationStatus is Resource.Error -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Icon(Icons.Default.Error, contentDescription = "Error")
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text("Error generating content", color = AppColors.textSecondary)
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Button(onClick = {
//                            courseId?.let { courseViewModel.startPollingGenerationStatus(it) }
//                        }) {
//                            Text("Retry")
//                        }
//                    }
//                }
//            }
//
//            else -> {
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator(color = AppColors.primary)
//                }
//            }
//        }
//    }
//
//    // Bottom Sheet for subtopic details
//    if (showBottomSheet && selectedSubtopic != null) {
//        ModalBottomSheet(
//            onDismissRequest = { showBottomSheet = false },
//            sheetState = sheetState,
//            dragHandle = { BottomSheetDefaults.DragHandle() }
//        ) {
//            selectedSubtopic?.let { subtopic ->
//                SubtopicBottomSheetContent(
//                    subtopic = subtopic,
//                    onDismiss = { showBottomSheet = false }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun CourseContentDisplay(
//    courseData: CourseFullResponse,
//    onSubtopicClick: (Subtopic) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    LazyColumn(modifier = modifier) {
//        item {
//            Text(
//                courseData.course.title,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary,
//                modifier = Modifier.padding(16.dp)
//            )
//            Text(
//                courseData.course.description ?: "",
//                fontSize = 16.sp,
//                color = AppColors.textSecondary,
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//            Spacer(modifier = Modifier.height(24.dp))
//        }
//
//        items(courseData.units) { unit ->
//            UnitContentCard(
//                unit = unit,
//                onSubtopicClick = onSubtopicClick
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//    }
//}
//
//@Composable
//fun UnitContentCard(
//    unit: UnitWithSubtopics,
//    onSubtopicClick: (Subtopic) -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                unit.title,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            unit.subtopics.forEach { subtopic ->
//                SubtopicItem(
//                    subtopic = subtopic,
//                    onClick = { onSubtopicClick(subtopic) }
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun SubtopicItem(
//    subtopic: Subtopic,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = AppColors.background),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    subtopic.title,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = AppColors.textPrimary
//                )
//
//                if (subtopic.videos.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        "${subtopic.videos.size} video(s) available",
//                        fontSize = 12.sp,
//                        color = AppColors.textSecondary
//                    )
//                }
//            }
//
//            Icon(
//                imageVector = Icons.Default.KeyboardArrowDown,
//                contentDescription = "View details",
//                tint = AppColors.textSecondary
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SubtopicBottomSheetContent(
//    subtopic: Subtopic,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    val generatedContent = remember(subtopic.content) {
//        subtopic.content?.let { parseGeneratedContent(it) }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        // Header
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                subtopic.title,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary,
//                modifier = Modifier.weight(1f)
//            )
//            IconButton(onClick = onDismiss) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "Close")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Videos section
//        if (subtopic.videos.isNotEmpty()) {
//            Text(
//                "Recommended Videos:",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = AppColors.textPrimary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            LazyRow(
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(subtopic.videos) { video ->
//                    VideoThumbnailItem(video = video)
//                }
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//
//        // Generated content
//        generatedContent?.let { content ->
//            GeneratedContentDisplay(content = content)
//        } ?: run {
//            Text(
//                "Content not generated yet",
//                fontStyle = FontStyle.Italic,
//                color = AppColors.textSecondary
//            )
//        }
//    }
//}
//
//@Composable
//fun GeneratedContentDisplay(content: GeneratedSubtopicContent) {
//    Column {
//        Text(
//            content.title,
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = AppColors.textPrimary
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // Why this matters
//        Text(
//            "Why this matters:",
//            fontSize = 16.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = AppColors.textPrimary
//        )
//        Text(
//            content.whyThisMatters,
//            color = AppColors.textSecondary,
//            modifier = Modifier.padding(vertical = 4.dp)
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // Core concepts
//        Text(
//            "Core Concepts:",
//            fontSize = 16.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = AppColors.textPrimary
//        )
//        content.coreConcepts.forEach { concept ->
//            Text(
//                "• ${concept.concept}: ${concept.explanation}",
//                color = AppColors.textSecondary,
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // Examples
//        if (content.examples.isNotEmpty()) {
//            Text(
//                "Examples:",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = AppColors.textPrimary
//            )
//            content.examples.forEachIndexed { index, example ->
//                Text(
//                    "${index + 1}. [${example.type.replace("_", " ").capitalize()}]: ${example.content}",
//                    color = AppColors.textSecondary,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//            Spacer(modifier = Modifier.height(12.dp))
//        }
//
//        // Code or Math
//        content.codeOrMath?.let { code ->
//            Text(
//                "Code Example:",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = AppColors.textPrimary
//            )
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
//            ) {
//                Text(
//                    code,
//                    color = Color.White,
//                    fontSize = 12.sp,
//                    modifier = Modifier.padding(12.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun VideoThumbnailItem(video: Video) {
//    val context = LocalContext.current
//    var showVideoDialog by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .width(160.dp)
//            .clickable { showVideoDialog = true },
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Column {
//            // Video thumbnail with play button overlay
//            Box {
//                AsyncImage(
//                    model = video.thumbnail,
//                    contentDescription = video.title,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(90.dp),
//                    contentScale = ContentScale.Crop
//                )
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.Black.copy(alpha = 0.4f)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        Icons.Default.PlayArrow,
//                        contentDescription = "Play",
//                        tint = Color.White,
//                        modifier = Modifier.size(32.dp)
//                    )
//                }
//            }
//
//            // Video info
//            Column(modifier = Modifier.padding(8.dp)) {
//                Text(
//                    video.title,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = AppColors.textPrimary,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                video.durationSec?.let { duration ->
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        formatDuration(duration),
//                        fontSize = 10.sp,
//                        color = AppColors.textSecondary
//                    )
//                }
//            }
//        }
//    }
//
//    if (showVideoDialog) {
//        AlertDialog(
//            onDismissRequest = { showVideoDialog = false },
//            title = { Text(video.title) },
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
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
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
//
//fun formatDuration(seconds: Int): String {
//    val minutes = seconds / 60
//    val remainingSeconds = seconds % 60
//    return String.format("%d:%02d", minutes, remainingSeconds)
//}
//
//fun parseGeneratedContent(jsonString: String): GeneratedSubtopicContent? {
//    return try {
//        Gson().fromJson(jsonString, GeneratedSubtopicContent::class.java)
//    } catch (e: Exception) {
//        null
//    }
//}

// WORKING CODE (BUT FETCHING THAT 2-3 SECONDS EVEN AFTER DATA IS COMPLETED)
//package com.example.jetpackdemo
//
//import android.content.Intent
//import android.net.Uri
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.example.jetpackdemo.data.model.*
//import com.example.jetpackdemo.ui.theme.AppColors
//import com.example.jetpackdemo.ui.viewmodel.CourseViewModel
//import com.example.jetpackdemo.ui.viewmodel.Resource
//import com.google.gson.Gson
//import kotlinx.coroutines.delay
//
//// UI Data classes matching your original design
//data class SubTopicContent(
//    val id: String,
//    val title: String,
//    val readTimeMinutes: Int,
//    val content: String,
//    val videos: List<Video>
//)
//
//data class Video(val title: String, val thumbnailUrl: String)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val generationStatus by courseViewModel.generationStatus.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
//
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var currentContent by remember { mutableStateOf<SubTopicContent?>(null) }
//
//    // Track generation state
//    var contentGenerationStarted by remember { mutableStateOf(false) }
//
//    // Start content generation when screen is launched
//    LaunchedEffect(courseId) {
//        if (courseId != null && !contentGenerationStarted) {
//            courseViewModel.generateCourseContent(courseId!!)
//            contentGenerationStarted = true
//            // Start polling after a short delay
//            delay(2000)
//            courseViewModel.startPollingGenerationStatus(courseId!!)
//        }
//    }
//
//    // Load course content when available
//    LaunchedEffect(fullCourseContent) {
//        if (fullCourseContent is Resource.Success) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null && courseData.units.isNotEmpty()) {
//                // Find first subtopic with content or first subtopic
//                val firstSubtopic = courseData.units
//                    .flatMap { it.subtopics }
//                    .firstOrNull { it.content != null }
//                    ?: courseData.units.firstOrNull()?.subtopics?.firstOrNull()
//
//                firstSubtopic?.let {
//                    currentContent = convertToSubTopicContent(it)
//                    selectedSubtopic = it
//                }
//            }
//        }
//    }
//
//    // Update content when selection changes
//    LaunchedEffect(selectedSubtopic) {
//        selectedSubtopic?.let {
//            currentContent = convertToSubTopicContent(it)
//        }
//    }
//
//    // Load full content when generation status updates
//    LaunchedEffect(generationStatus) {
//        if (generationStatus is Resource.Success && courseId != null) {
//            courseViewModel.getFullCourseContent(courseId!!)
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        currentContent?.title ?: "Course Content",
//                        color = AppColors.textPrimary,
//                        maxLines = 1
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Handle more options */ }) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "More",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            if (fullCourseContent is Resource.Success) {
//                FloatingActionButton(
//                    onClick = { showSheet = true },
//                    containerColor = AppColors.primary,
//                    contentColor = AppColors.onPrimary,
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.List, contentDescription = "View Topics")
//                }
//            }
//        },
//        bottomBar = {
//            if (currentContent != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp,
//                    actions = {
//                        Button(
//                            onClick = { /* Mark as Complete */ },
//                            modifier = Modifier.weight(1f).height(48.dp),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Icon(Icons.Default.Check, contentDescription = null)
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Mark Complete")
//                        }
//                        Spacer(modifier = Modifier.width(12.dp))
//                        OutlinedButton(
//                            onClick = { /* Save Note */ },
//                            modifier = Modifier.height(48.dp)
//                        ) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Note")
//                        }
//                    }
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                currentContent != null -> {
//                    // Display content with your exact original design
//                    CourseContentDisplay(
//                        content = currentContent!!,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                generationStatus is Resource.Loading -> {
//                    LoadingState(modifier = Modifier.padding(paddingValues))
//                }
//
//                generationStatus is Resource.Success -> {
//                    val status = (generationStatus as Resource.Success).data
//                    GeneratingState(
//                        status = status,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                generationStatus is Resource.Error -> {
//                    ErrorState(
//                        onRetry = {
//                            courseId?.let {
//                                contentGenerationStarted = false
//                            }
//                        },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                else -> {
//                    LoadingState(modifier = Modifier.padding(paddingValues))
//                }
//            }
//        }
//
//        // Bottom Sheet for navigation
//        if (showSheet && fullCourseContent is Resource.Success) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null) {
//                ModalBottomSheet(
//                    onDismissRequest = { showSheet = false },
//                    sheetState = sheetState
//                ) {
//                    TopicNavigationSheet(
//                        courseData = courseData,
//                        selectedSubtopicId = selectedSubtopic?.id,
//                        onSubTopicSelected = { subtopic ->
//                            selectedSubtopic = subtopic
//                            showSheet = false
//                        },
//                        onDismiss = { showSheet = false }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CourseContentDisplay(
//    content: SubTopicContent,
//    modifier: Modifier = Modifier
//) {
//    LazyColumn(
//        modifier = modifier,
//        contentPadding = PaddingValues(16.dp)
//    ) {
//        item {
//            Text(
//                content.title,
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    Icons.Default.Schedule,
//                    contentDescription = "Read time",
//                    tint = AppColors.textSecondary,
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    "${content.readTimeMinutes} min read",
//                    color = AppColors.textSecondary,
//                    fontSize = 14.sp
//                )
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                content.content,
//                color = AppColors.textPrimary,
//                fontSize = 16.sp,
//                lineHeight = 24.sp
//            )
//            Spacer(modifier = Modifier.height(24.dp))
//            if (content.videos.isNotEmpty()) {
//                Text(
//                    "Watch Related Videos",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = AppColors.textPrimary
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//        item {
//            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                items(content.videos) { video ->
//                    VideoThumbnailCard(video)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun VideoThumbnailCard(video: Video) {
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
//                maxLines = 2
//            )
//        }
//    }
//
//    if (showVideoDialog) {
//        AlertDialog(
//            onDismissRequest = { showVideoDialog = false },
//            title = { Text(video.title) },
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
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.thumbnailUrl.replace("/default.jpg", "")))
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
//
//@Composable
//fun TopicNavigationSheet(
//    courseData: CourseFullResponse,
//    selectedSubtopicId: String?,
//    onSubTopicSelected: (Subtopic) -> Unit,
//    onDismiss: () -> Unit
//) {
//    var expandedUnitId by remember { mutableStateOf<String?>(null) }
//
//    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
//        items(courseData.units) { unit ->
//            UnitItemView(
//                unit = unit,
//                selectedSubtopicId = selectedSubtopicId,
//                isExpanded = expandedUnitId == unit.id,
//                onExpand = {
//                    expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
//                },
//                onSubTopicSelected = onSubTopicSelected
//            )
//        }
//    }
//}
//
//@Composable
//fun UnitItemView(
//    unit: UnitWithSubtopics,
//    selectedSubtopicId: String?,
//    isExpanded: Boolean,
//    onExpand: () -> Unit,
//    onSubTopicSelected: (Subtopic) -> Unit
//) {
//    Column {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable(onClick = onExpand)
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(unit.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
//            Icon(
//                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                contentDescription = "Expand"
//            )
//        }
//        if (isExpanded) {
//            unit.subtopics.forEach { subTopic ->
//                Text(
//                    text = subTopic.title,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { onSubTopicSelected(subTopic) }
//                        .padding(start = 32.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
//                    color = AppColors.textSecondary
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun LoadingState(modifier: Modifier = Modifier) {
//    Box(
//        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            CircularProgressIndicator(color = AppColors.primary)
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("Generating course content...", color = AppColors.textSecondary)
//        }
//    }
//}
//
//@Composable
//fun GeneratingState(status: GenerationStatusResponse?, modifier: Modifier = Modifier) {
//    Box(
//        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            CircularProgressIndicator(color = AppColors.primary)
//            Spacer(modifier = Modifier.height(16.dp))
//            if (status != null) {
//                Text(
//                    "Generating: ${status.generatedSubtopics}/${status.totalSubtopics} subtopics",
//                    color = AppColors.textSecondary
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text("Status: ${status.status}", color = AppColors.textSecondary)
//            }
//        }
//    }
//}
//
//@Composable
//fun ErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
//    Box(
//        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Icon(Icons.Default.Error, contentDescription = "Error")
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("Error generating content", color = AppColors.textSecondary)
//            Spacer(modifier = Modifier.height(8.dp))
//            Button(onClick = onRetry) {
//                Text("Retry")
//            }
//        }
//    }
//}
//
//// Helper functions
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
//                        append("${index + 1}. [${example.type.replace("_", " ")}]: ${example.content}\n")
//                    }
//                }
//                it.codeOrMath?.let { code ->
//                    append("\nCode Example:\n$code")
//                }
//            }
//        } ?: "Content not generated yet",
//        videos = subtopic.videos.map { video ->
//            Video(video.title, video.thumbnail)
//        }
//    )
//}
//
//private fun estimateReadTime(text: String): Int {
//    val wordsPerMinute = 200
//    val wordCount = text.split("\\s+".toRegex()).size
//    return maxOf(1, wordCount / wordsPerMinute)
//}
//
//private fun parseGeneratedContent(jsonString: String?): GeneratedSubtopicContent? {
//    if (jsonString == null) return null
//    return try {
//        Gson().fromJson(jsonString, GeneratedSubtopicContent::class.java)
//    } catch (e: Exception) {
//        null
//    }
//}

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
import com.example.jetpackdemo.ui.viewmodel.CourseViewModel
import com.example.jetpackdemo.ui.viewmodel.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay

//// UI Data classes - Renamed to avoid conflict
//data class SubTopicContent(
//    val id: String,
//    val title: String,
//    val readTimeMinutes: Int,
//    val content: String,
//    val videos: List<UiVideo>  // Changed to UiVideo
//)
//
//data class UiVideo(  // Renamed from Video to UiVideo
//    val title: String,
//    val thumbnailUrl: String
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val generationStatus by courseViewModel.generationStatus.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
//
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var currentContent by remember { mutableStateOf<SubTopicContent?>(null) }
//
//    // Track if we need to load content
//    var shouldLoadContent by remember { mutableStateOf(true) }
//
//    // Load course content when screen is launched
//    LaunchedEffect(courseId) {
//        if (courseId != null && shouldLoadContent) {
//            courseViewModel.getFullCourseContent(courseId!!)
//            shouldLoadContent = false
//        }
//    }
//
//    // Process course content when it's available
//    LaunchedEffect(fullCourseContent) {
//        when (fullCourseContent) {
//            is Resource.Success -> {
//                val courseData = (fullCourseContent as Resource.Success).data
//                if (courseData != null) {
//                    // Find the first subtopic that has content (not null)
//                    val firstContentSubtopic = courseData.units
//                        .flatMap { it.subtopics }
//                        .firstOrNull { it.content != null }
//
//                    firstContentSubtopic?.let {
//                        currentContent = convertToSubTopicContent(it)
//                        selectedSubtopic = it
//                    }
//
//                    // If no content yet, start generation and polling
//                    if (firstContentSubtopic == null && courseId != null) {
//                        courseViewModel.generateCourseContent(courseId!!)
//                        delay(2000) // Wait a bit before starting polling
//                        courseViewModel.startPollingGenerationStatus(courseId!!)
//                    }
//                }
//            }
//            is Resource.Error -> {
//                // If loading fails, try to start generation
//                if (courseId != null) {
//                    courseViewModel.generateCourseContent(courseId!!)
//                    delay(2000)
//                    courseViewModel.startPollingGenerationStatus(courseId!!)
//                }
//            }
//            else -> {}
//        }
//    }
//
//    // Update content when selection changes
//    LaunchedEffect(selectedSubtopic) {
//        selectedSubtopic?.let {
//            currentContent = convertToSubTopicContent(it)
//        }
//    }
//
//    // Handle generation status to refresh content
//    LaunchedEffect(generationStatus) {
//        if (generationStatus is Resource.Success && courseId != null) {
//            // Refresh content to get newly generated subtopics
//            courseViewModel.getFullCourseContent(courseId!!)
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        currentContent?.title ?: "Course Content",
//                        color = AppColors.textPrimary,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Handle more options */ }) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "More",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            if (fullCourseContent is Resource.Success) {
//                FloatingActionButton(
//                    onClick = { showSheet = true },
//                    containerColor = AppColors.primary,
//                    contentColor = AppColors.onPrimary,
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.List, contentDescription = "View Topics")
//                }
//            }
//        },
//        bottomBar = {
//            if (currentContent != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp,
//                    actions = {
//                        Button(
//                            onClick = { /* Mark as Complete */ },
//                            modifier = Modifier.weight(1f).height(48.dp),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Icon(Icons.Default.Check, contentDescription = null)
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Mark Complete")
//                        }
//                        Spacer(modifier = Modifier.width(12.dp))
//                        OutlinedButton(
//                            onClick = { /* Save Note */ },
//                            modifier = Modifier.height(48.dp)
//                        ) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Note")
//                        }
//                    }
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                currentContent != null -> {
//                    // Display content with your exact original design
//                    CourseContentDisplay(
//                        content = currentContent!!,
//                        generationStatus = generationStatus,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Success -> {
//                    // We have course data but no content yet (all subtopics are null)
//                    val courseData = (fullCourseContent as Resource.Success).data
//                    GeneratingState(
//                        courseTitle = courseData?.course?.title ?: "Course",
//                        generationStatus = generationStatus,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Loading -> {
//                    LoadingState(
//                        message = "Loading course content...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Error -> {
//                    ErrorState(
//                        message = "Failed to load course content",
//                        onRetry = {
//                            shouldLoadContent = true
//                        },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                else -> {
//                    LoadingState(
//                        message = "Initializing...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//            }
//        }
//
//        // Bottom Sheet for navigation - show all subtopics with content status
//        if (showSheet && fullCourseContent is Resource.Success) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null) {
//                ModalBottomSheet(
//                    onDismissRequest = { showSheet = false },
//                    sheetState = sheetState
//                ) {
//                    TopicNavigationSheet(
//                        courseData = courseData,
//                        selectedSubtopicId = selectedSubtopic?.id,
//                        onSubTopicSelected = { subtopic ->
//                            // Only allow selection of subtopics that have content
//                            if (subtopic.content != null) {
//                                selectedSubtopic = subtopic
//                                showSheet = false
//                            }
//                        },
//                        onDismiss = { showSheet = false }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CourseContentDisplay(
//    content: SubTopicContent,
//    generationStatus: Resource<GenerationStatusResponse>?,
//    modifier: Modifier = Modifier
//) {
//    LazyColumn(
//        modifier = modifier,
//        contentPadding = PaddingValues(16.dp)
//    ) {
//        item {
//            // Show generation progress if still generating
//            if (generationStatus is Resource.Success) {
//                val status = generationStatus.data
//                if (status != null && status.status != "completed") {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            "Generating: ${status.generatedSubtopics}/${status.totalSubtopics}",
//                            color = AppColors.textSecondary,
//                            fontSize = 14.sp
//                        )
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(16.dp),
//                            color = AppColors.primary,
//                            strokeWidth = 2.dp
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//            }
//
//            Text(
//                content.title,
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    Icons.Default.Schedule,
//                    contentDescription = "Read time",
//                    tint = AppColors.textSecondary,
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    "${content.readTimeMinutes} min read",
//                    color = AppColors.textSecondary,
//                    fontSize = 14.sp
//                )
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                content.content,
//                color = AppColors.textPrimary,
//                fontSize = 16.sp,
//                lineHeight = 24.sp
//            )
//            Spacer(modifier = Modifier.height(24.dp))
//            if (content.videos.isNotEmpty()) {
//                Text(
//                    "Watch Related Videos",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = AppColors.textPrimary
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//        item {
//            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                items(content.videos) { video ->
//                    VideoThumbnailCard(video)
//                }
//            }
//        }
//    }
//}

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
    val thumbnailUrl: String
)

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val generationStatus by courseViewModel.generationStatus.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
//
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var currentContent by remember { mutableStateOf<SubTopicContent?>(null) }
//    var allContentGenerated by remember { mutableStateOf(false) }
//
//    // Track if we need to load content
//    var shouldLoadContent by remember { mutableStateOf(true) }
//
//    // Load course content when screen is launched
//    LaunchedEffect(courseId) {
//        if (courseId != null && shouldLoadContent) {
//            courseViewModel.getFullCourseContent(courseId!!)
//            shouldLoadContent = false
//        }
//    }
//
//    // Process course content when it's available
//    LaunchedEffect(fullCourseContent) {
//        when (fullCourseContent) {
//            is Resource.Success -> {
//                val courseData = (fullCourseContent as Resource.Success).data
//                if (courseData != null) {
//                    // Check if all subtopics have content
//                    val allSubtopicIds = courseData.units.flatMap { it.subtopics }.map { it.id }
//                    val subtopicsWithContent = courseData.units.flatMap { it.subtopics }
//                        .filter { it.content != null }
//
//                    // Stop polling if all content is generated
//                    if (subtopicsWithContent.size == allSubtopicIds.size) {
//                        allContentGenerated = true
//                        courseViewModel.stopPolling()
//                    }
//
//                    // Find the first subtopic that has content (not null)
//                    val firstContentSubtopic = subtopicsWithContent.firstOrNull()
//
//                    firstContentSubtopic?.let {
//                        currentContent = convertToSubTopicContent(it)
//                        selectedSubtopic = it
//                    }
//
//                    // If no content yet, start generation and polling
//                    if (firstContentSubtopic == null && courseId != null && !allContentGenerated) {
//                        courseViewModel.generateCourseContent(courseId!!)
//                        delay(2000)
//                        courseViewModel.startPollingGenerationStatus(courseId!!)
//                    }
//                }
//            }
//            is Resource.Error -> {
//                // If loading fails, try to start generation
//                if (courseId != null && !allContentGenerated) {
//                    courseViewModel.generateCourseContent(courseId!!)
//                    delay(2000)
//                    courseViewModel.startPollingGenerationStatus(courseId!!)
//                }
//            }
//            else -> {}
//        }
//    }
//
//    // Update content when selection changes
//    LaunchedEffect(selectedSubtopic) {
//        selectedSubtopic?.let {
//            currentContent = convertToSubTopicContent(it)
//        }
//    }
//
//    // Handle generation status to refresh content - but stop when all is done
//    LaunchedEffect(generationStatus) {
//        if (generationStatus is Resource.Success && courseId != null && !allContentGenerated) {
//            val status = (generationStatus as Resource.Success).data
//            // Check if generation is complete
//            if (status?.status == "completed") {
//                allContentGenerated = true
//                courseViewModel.stopPolling()
//            }
//            // Refresh content to get newly generated subtopics
//            courseViewModel.getFullCourseContent(courseId!!)
//        }
//    }
//
//    // Stop polling when leaving screen
//    DisposableEffect(Unit) {
//        onDispose {
//            courseViewModel.stopPolling()
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        currentContent?.title ?: "Course Content",
//                        color = AppColors.textPrimary,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Handle more options */ }) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "More",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            if (fullCourseContent is Resource.Success) {
//                FloatingActionButton(
//                    onClick = { showSheet = true },
//                    containerColor = AppColors.primary,
//                    contentColor = AppColors.onPrimary,
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.List, contentDescription = "View Topics")
//                }
//            }
//        },
//        bottomBar = {
//            if (currentContent != null) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp,
//                    actions = {
//                        Button(
//                            onClick = { /* Mark as Complete */ },
//                            modifier = Modifier.weight(1f).height(48.dp),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Icon(Icons.Default.Check, contentDescription = null)
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Mark Complete", fontWeight = FontWeight.SemiBold)
//                        }
//                        Spacer(modifier = Modifier.width(12.dp))
//                        OutlinedButton(
//                            onClick = { /* Save Note */ },
//                            modifier = Modifier.height(48.dp)
//                        ) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Note")
//                        }
//                    }
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                currentContent != null -> {
//                    // Display content with your exact original design
//                    CourseContentDisplay(
//                        content = currentContent!!,
//                        generationStatus = if (allContentGenerated) null else generationStatus,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Success -> {
//                    // We have course data but no content yet (all subtopics are null)
//                    val courseData = (fullCourseContent as Resource.Success).data
//                    GeneratingState(
//                        courseTitle = courseData?.course?.title ?: "Course",
//                        generationStatus = generationStatus,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Loading -> {
//                    LoadingState(
//                        message = "Loading course content...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Error -> {
//                    ErrorState(
//                        message = "Failed to load course content",
//                        onRetry = {
//                            shouldLoadContent = true
//                        },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                else -> {
//                    LoadingState(
//                        message = "Initializing...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//            }
//        }
//
//        // Bottom Sheet for navigation - show all subtopics with content status
//        if (showSheet && fullCourseContent is Resource.Success) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null) {
//                ModalBottomSheet(
//                    onDismissRequest = { showSheet = false },
//                    sheetState = sheetState
//                ) {
//                    TopicNavigationSheet(
//                        courseData = courseData,
//                        selectedSubtopicId = selectedSubtopic?.id,
//                        onSubTopicSelected = { subtopic ->
//                            // Only allow selection of subtopics that have content
//                            if (subtopic.content != null) {
//                                selectedSubtopic = subtopic
//                                showSheet = false
//                            }
//                        },
//                        onDismiss = { showSheet = false }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CourseContentDisplay(
//    content: SubTopicContent,
//    generationStatus: Resource<GenerationStatusResponse>?,
//    modifier: Modifier = Modifier
//) {
//    LazyColumn(
//        modifier = modifier,
//        contentPadding = PaddingValues(16.dp)
//    ) {
//        item {
//            // Show generation progress if still generating
//            if (generationStatus is Resource.Success) {
//                val status = generationStatus.data
//                if (status != null && status.status != "completed") {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            "Generating: ${status.generatedSubtopics}/${status.totalSubtopics}",
//                            color = AppColors.textSecondary,
//                            fontSize = 14.sp
//                        )
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(16.dp),
//                            color = AppColors.primary,
//                            strokeWidth = 2.dp
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//            }
//
//            Text(
//                content.title,
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    Icons.Default.Schedule,
//                    contentDescription = "Read time",
//                    tint = AppColors.textSecondary,
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    "${content.readTimeMinutes} min read",
//                    color = AppColors.textSecondary,
//                    fontSize = 14.sp
//                )
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Display formatted content
//            FormattedContentDisplay(content.content)
//
//            Spacer(modifier = Modifier.height(24.dp))
//            if (content.videos.isNotEmpty()) {
//                Text(
//                    "Watch Related Videos",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = AppColors.textPrimary
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//        item {
//            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                items(content.videos) { video ->
//                    VideoThumbnailCard(video)
//                }
//            }
//        }
//    }
//}
// WORKING BUT NOT UPDATING THE NEW CONTENT
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val generationStatus by courseViewModel.generationStatus.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
//
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var currentContent by remember { mutableStateOf<SubTopicContent?>(null) }
//    var allContentGenerated by remember { mutableStateOf(false) }
//    var hasContentToShow by remember { mutableStateOf(false) }
//
//    // Track if we need to load content
//    var shouldLoadContent by remember { mutableStateOf(true) }
//
//    // Load course content when screen is launched
//    LaunchedEffect(courseId) {
//        if (courseId != null && shouldLoadContent) {
//            courseViewModel.getFullCourseContent(courseId!!)
//            shouldLoadContent = false
//        }
//    }
//
//    // Process course content when it's available - ONLY ONCE when we first get content
//    LaunchedEffect(fullCourseContent) {
//        if (fullCourseContent is Resource.Success && !hasContentToShow) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null) {
//                // Find the first subtopic that has content (not null)
//                val firstContentSubtopic = courseData.units
//                    .flatMap { it.subtopics }
//                    .firstOrNull { it.content != null }
//
//                firstContentSubtopic?.let {
//                    currentContent = convertToSubTopicContent(it)
//                    selectedSubtopic = it
//                    hasContentToShow = true
//
//                    // STOP POLLING once we have content to show
//                    courseViewModel.stopPolling()
//                }
//
//                // If no content yet, start generation and polling
//                if (firstContentSubtopic == null && courseId != null && !allContentGenerated) {
//                    courseViewModel.generateCourseContent(courseId!!)
//                    delay(2000)
//                    courseViewModel.startPollingGenerationStatus(courseId!!)
//                }
//            }
//        }
//    }


//LaunchedEffect(fullCourseContent) {
//    if (fullCourseContent is Resource.Success) {
//        val courseData = (fullCourseContent as Resource.Success).data
//        if (courseData != null) {
//            // Find the first subtopic that has content (not null)
//            val firstContentSubtopic = courseData.units
//                .flatMap { it.subtopics }
//                .firstOrNull { it.content != null }
//
//            if (firstContentSubtopic != null) {
//                // ✅ Always update when new valid content appears
//                currentContent = convertToSubTopicContent(firstContentSubtopic)
//                selectedSubtopic = firstContentSubtopic
//                hasContentToShow = true
//
//                // Stop polling once we have content
//                courseViewModel.stopPolling()
//            } else if (courseId != null && !allContentGenerated) {
//                // 🟡 If still no content yet, start generation and polling
//                courseViewModel.generateCourseContent(courseId!!)
//                delay(2000)
//                courseViewModel.startPollingGenerationStatus(courseId!!)
//            }
//        }
//    }
//}


//
//    // Update content when selection changes - but only if we're not in the middle of polling
//    LaunchedEffect(selectedSubtopic) {
//        if (hasContentToShow && selectedSubtopic != null) {
//            currentContent = convertToSubTopicContent(selectedSubtopic!!)
//        }
//    }
//
//    // Handle generation status updates - but only if we don't have content yet
//    LaunchedEffect(generationStatus) {
//        if (generationStatus is Resource.Success && courseId != null && !hasContentToShow) {
//            val status = (generationStatus as Resource.Success).data
//            // Check if generation is complete
//            if (status?.status == "completed") {
//                allContentGenerated = true
//                courseViewModel.stopPolling()
//                // Refresh to get the final content
//                courseViewModel.getFullCourseContent(courseId!!)
//            } else if (!hasContentToShow) {
//                // Only refresh if we don't have content yet
//                courseViewModel.getFullCourseContent(courseId!!)
//            }
//        }
//    }
//
//    // Stop polling when leaving screen
//    DisposableEffect(Unit) {
//        onDispose {
//            courseViewModel.stopPolling()
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        currentContent?.title ?: "Course Content",
//                        color = AppColors.textPrimary,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Handle more options */ }) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "More",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            // Show FAB only when we have content and it's stable (not being refreshed)
//            if (hasContentToShow && fullCourseContent is Resource.Success) {
//                FloatingActionButton(
//                    onClick = { showSheet = true },
//                    containerColor = AppColors.primary,
//                    contentColor = AppColors.onPrimary,
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.List, contentDescription = "View Topics")
//                }
//            }
//        },
//        bottomBar = {
//            if (currentContent != null && hasContentToShow) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp,
//                    actions = {
//                        Button(
//                            onClick = { /* Mark as Complete */ },
//                            modifier = Modifier.weight(1f).height(48.dp),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Icon(Icons.Default.Check, contentDescription = null)
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Mark Complete", fontWeight = FontWeight.SemiBold)
//                        }
//                        Spacer(modifier = Modifier.width(12.dp))
//                        OutlinedButton(
//                            onClick = { /* Save Note */ },
//                            modifier = Modifier.height(48.dp)
//                        ) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Note")
//                        }
//                    }
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                currentContent != null && hasContentToShow -> {
//                    // Display content with your exact original design
//                    CourseContentDisplay(
//                        content = currentContent!!,
//                        // Don't show generation status if we have stable content
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Success && !hasContentToShow -> {
//                    // We have course data but no content yet (all subtopics are null)
//                    val courseData = (fullCourseContent as Resource.Success).data
//                    GeneratingState(
//                        courseTitle = courseData?.course?.title ?: "Course",
//                        generationStatus = generationStatus,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Loading -> {
//                    LoadingState(
//                        message = "Loading course content...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Error -> {
//                    ErrorState(
//                        message = "Failed to load course content",
//                        onRetry = {
//                            shouldLoadContent = true
//                            hasContentToShow = false
//                        },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                else -> {
//                    LoadingState(
//                        message = "Initializing...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//            }
//        }
//
//        // Bottom Sheet for navigation - show all subtopics with content status
//        if (showSheet && fullCourseContent is Resource.Success && hasContentToShow) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null) {
//                ModalBottomSheet(
//                    onDismissRequest = { showSheet = false },
//                    sheetState = sheetState
//                ) {
//                    TopicNavigationSheet(
//                        courseData = courseData,
//                        selectedSubtopicId = selectedSubtopic?.id,
//                        onSubTopicSelected = { subtopic ->
//                            // Only allow selection of subtopics that have content
//                            if (subtopic.content != null) {
//                                selectedSubtopic = subtopic
//                                showSheet = false
//                            }
//                        },
//                        onDismiss = { showSheet = false }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CourseContentDisplay(
//    content: SubTopicContent,
//    modifier: Modifier = Modifier
//) {
//    LazyColumn(
//        modifier = modifier,
//        contentPadding = PaddingValues(16.dp)
//    ) {
//        item {
//            Text(
//                content.title,
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                color = AppColors.textPrimary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    Icons.Default.Schedule,
//                    contentDescription = "Read time",
//                    tint = AppColors.textSecondary,
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    "${content.readTimeMinutes} min read",
//                    color = AppColors.textSecondary,
//                    fontSize = 14.sp
//                )
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Display formatted content
//            FormattedContentDisplay(content.content)
//
//            Spacer(modifier = Modifier.height(24.dp))
//            if (content.videos.isNotEmpty()) {
//                Text(
//                    "Watch Related Videos",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = AppColors.textPrimary
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//        item {
//            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                items(content.videos) { video ->
//                    VideoThumbnailCard(video)
//                }
//            }
//        }
//    }
//}

//------------------------------100% working --------------------
// CourseContentScreen.kt
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
//
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var currentContent by remember { mutableStateOf<SubTopicContent?>(null) }
//    var hasContentToShow by remember { mutableStateOf(false) }
//
//    // Streaming state
//    val streamingEvents by courseViewModel.streamingEvents.collectAsState(initial = null)
//    val generationProgress by courseViewModel.generationProgress.collectAsState(initial = null)
//
//
//
//    // Load full course once
//    LaunchedEffect(courseId) {
//        if (courseId != null) {
//            courseViewModel.getFullCourseContent(courseId!!)
//        }
//    }
//
//
//
//    // Update UI when new subtopic is generated
////    LaunchedEffect(streamingEvents) {
////        streamingEvents?.let { event ->
////            when (event.type) {
////                "progress" -> {
////                    courseViewModel.updateProgress(event)
////                }
////                "complete" -> {
////                    courseViewModel.stopStreaming()
////                    courseViewModel.getFullCourseContent(courseId!!)
////                }
////            }
////        }
////    }
//    LaunchedEffect(Unit) {
//        courseViewModel.streamingEvents.collect { event ->
//            if (event == null) return@collect
//
//            when (event.type) {
//                "progress" -> courseViewModel.updateProgress(event)
//                "complete" -> {
//                    courseViewModel.stopStreaming()
//                    courseViewModel.getFullCourseContent(courseId!!)
//                }
//                "error" -> courseViewModel.stopStreaming()
//            }
//        }
//    }
//
//
//    // Auto-select first generated subtopic
//    LaunchedEffect(fullCourseContent) {
//        if (fullCourseContent is Resource.Success && !hasContentToShow) {
//            val data = fullCourseContent.data
//            val first = data.units.flatMap { it.subtopics }
//                .firstOrNull { it.content != null }
//
//            if (first != null) {
//                selectedSubtopic = first
//                currentContent = convertToSubTopicContent(first)
//                hasContentToShow = true
//            }
//        }
//    }
//
//    // Update content when selection changes
//    LaunchedEffect(selectedSubtopic) {
//        selectedSubtopic?.let {
//            currentContent = convertToSubTopicContent(it)
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        currentContent?.title ?: "Course Content",
//                        color = AppColors.textPrimary,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* More */ }) {
//                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = AppColors.textPrimary)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            if (hasContentToShow && fullCourseContent is Resource.Success) {
//                FloatingActionButton(
//                    onClick = { showSheet = true },
//                    containerColor = AppColors.primary,
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.List, contentDescription = "Topics")
//                }
//            }
//        },
//        bottomBar = {
//            if (currentContent != null && hasContentToShow) {
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
//                        Spacer(Modifier.width(12.dp))
//                        OutlinedButton(onClick = { /* Save Note */ }, modifier = Modifier.height(48.dp)) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
//                        }
//                    }
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                currentContent != null && hasContentToShow -> {
//                    CourseContentDisplay(content = currentContent!!, modifier = Modifier.padding(paddingValues))
//                }
//
//                fullCourseContent is Resource.Success && !hasContentToShow -> {
//                    val data = (fullCourseContent as Resource.Success).data
//                    GeneratingStateWithStreaming(
//                        courseTitle = data?.course?.title ?: "Course",
//                        progress = generationProgress,
//                        viewModel = courseViewModel,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Loading -> {
//                    LoadingState("Loading course...", Modifier.padding(paddingValues))
//                }
//
//                fullCourseContent is Resource.Error -> {
//                    ErrorState(
//                        message = "Failed to load course",
//                        onRetry = { courseId?.let { courseViewModel.getFullCourseContent(it) } },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                else -> {
//                    LoadingState("Initializing...", Modifier.padding(paddingValues))
//                }
//            }
//        }
//
//        // Bottom Sheet
//        if (showSheet && fullCourseContent is Resource.Success && hasContentToShow) {
//            val data = (fullCourseContent as Resource.Success).data
//            data?.let {
//                ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState) {
//                    TopicNavigationSheet(
//                        courseData = it,
//                        selectedSubtopicId = selectedSubtopic?.id,
//                        onSubTopicSelected = { sub ->
//                            if (sub.content != null) {
//                                selectedSubtopic = sub
//                                showSheet = false
//                            }
//                        },
//                        onDismiss = { showSheet = false }
//                    )
//                }
//            }
//        }
//    }
//}

//@Composable
//fun GeneratingStateWithStreaming(
//    courseTitle: String,
//    progress: CourseViewModel.GenerationProgress?,
//    viewModel: CourseViewModel, // ← Add this
//    modifier: Modifier = Modifier
//) {
//    val streamingText by viewModel.currentStreamingText.collectAsState()
//    val currentSubtopic by viewModel.currentStreamingSubtopic.collectAsState()
//
//    val scrollState = rememberScrollState()
//    LaunchedEffect(streamingText) {
//        scrollState.animateScrollTo(scrollState.maxValue)
//    }
//
//    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.padding(24.dp)
//        ) {
//            CircularProgressIndicator(color = AppColors.primary, strokeWidth = 6.dp)
//            Spacer(Modifier.height(24.dp))
//            Text("Generating: $courseTitle", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
//
//            progress?.let {
//                Spacer(Modifier.height(16.dp))
//                LinearProgressIndicator(
//                    progress = { it.progress / 100f },
//                    modifier = Modifier.fillMaxWidth(0.8f),
//                    color = AppColors.primary,
//                    trackColor = AppColors.surface
//                )
//                Spacer(Modifier.height(8.dp))
//                Text(it.subtopic, color = AppColors.textSecondary, fontSize = 14.sp)
//                Text("${it.generated}/${it.total} subtopics", color = AppColors.textSecondary)
//            }
//
//            currentSubtopic?.let { sub ->
//                Spacer(Modifier.height(24.dp))
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .heightIn(max = 300.dp),
//                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
//                    elevation = CardDefaults.cardElevation(4.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            text = sub,
//                            fontWeight = FontWeight.SemiBold,
//                            color = AppColors.primary,
//                            fontSize = 15.sp
//                        )
//                        Spacer(Modifier.height(8.dp))
//                        Text(
//                            text = streamingText.ifEmpty { "..." },
//                            color = AppColors.textPrimary,
//                            fontSize = 14.sp,
//                            lineHeight = 20.sp,
//                            modifier = Modifier
//                                .verticalScroll(scrollState)
//                        )
//                    }
//                }
//            } ?: run {
//                if (progress != null) {
//                    Spacer(Modifier.height(16.dp))
//                    Text("Starting generation...", color = AppColors.textSecondary)
//                }
//            }
//        }
//    }
//}

// -------------------------------------------------------
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()   // non-nullable Resource
//
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var currentContent by remember { mutableStateOf<SubTopicContent?>(null) }
//    var hasContentToShow by remember { mutableStateOf(false) }
//
//    val streamingEvents by courseViewModel.streamingEvents.collectAsState(initial = null)
//    val generationProgress by courseViewModel.generationProgress.collectAsState(initial = null)
//    val isGenerating = generationProgress != null || courseViewModel.isPollingActive()
//
//    /* -------------------------------------------------------------
//       RESET ON NEW COURSE
//       ------------------------------------------------------------- */
//    LaunchedEffect(courseId) {
//        hasContentToShow = false
//        selectedSubtopic = null
//        currentContent = null
//        courseId?.let { courseViewModel.getFullCourseContent(it) }
//    }
//
//    /* -------------------------------------------------------------
//       STREAMING EVENTS
//       ------------------------------------------------------------- */
//    LaunchedEffect(Unit) {
//        courseViewModel.streamingEvents.collect { event ->
//            if (event == null) return@collect
//            when (event.type) {
//                "progress" -> courseViewModel.updateProgress(event)
//                "complete" -> {
//                    courseViewModel.stopStreaming()
//                    courseId?.let { courseViewModel.startPollingFullContent(it) }
//                }
//                "error" -> courseViewModel.stopStreaming()
//            }
//        }
//    }
//
//    /* -------------------------------------------------------------
//       AUTO-SELECT + REFRESH CONTENT
//       ------------------------------------------------------------- */
//    LaunchedEffect(selectedSubtopic?.id, fullCourseContent) {
//        val resource = fullCourseContent                     // local copy → smart-cast works
//        if (resource is Resource.Success) {
//            val data = resource.data
//
//            selectedSubtopic?.let { sub ->
//                val fresh = data?.units!!
//                    .flatMap { it.subtopics }
//                    .find { it.id == sub.id }
//
//                fresh?.let {
//                    currentContent = convertToSubTopicContent(it)
//                }
//                return@LaunchedEffect
//            }
//
//            if (!hasContentToShow) {
//                val first = data?.units!!
//                    .flatMap { it.subtopics }
//                    .firstOrNull { it.content != null && it.contentGeneratedAt != null }
//
//                if (first != null) {
//                    selectedSubtopic = first
//                    currentContent = convertToSubTopicContent(first)
//                    hasContentToShow = true
//                }
//            }
//        }
//    }
//
//    /* -------------------------------------------------------------
//       ALL CONTENT READY?
//       ------------------------------------------------------------- */
//    val allContentReady = when (fullCourseContent) {
//        is Resource.Success -> fullCourseContent?.data!!.units
//            .flatMap { it.subtopics }
//            .all { it.content != null }
//        else -> false
//    }
//
//    /* -------------------------------------------------------------
//       UI
//       ------------------------------------------------------------- */
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        currentContent?.title ?: "Course Content",
//                        color = AppColors.textPrimary,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* More */ }) {
//                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = AppColors.textPrimary)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            if (allContentReady && hasContentToShow) {
//                FloatingActionButton(
//                    onClick = { showSheet = true },
//                    containerColor = AppColors.primary,
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.List, contentDescription = "Topics")
//                }
//            }
//        },
//        bottomBar = {
//            if (currentContent != null && hasContentToShow) {
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
//                        Spacer(Modifier.width(12.dp))
//                        OutlinedButton(onClick = { /* Save Note */ }, modifier = Modifier.height(48.dp)) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
//                        }
//                    }
//                )
//            }
//        }
//    ) { paddingValues ->
//
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                currentContent != null && hasContentToShow -> {
//                    CourseContentDisplay(
//                        content = currentContent!!,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                isGenerating -> {
//                    GeneratingStateWithStreaming(
//                        courseTitle = when (val r = fullCourseContent) {
//                            is Resource.Success -> r.data!!.course.title
//                            else -> "Course"
//                        },
//                        progress = generationProgress,
//                        viewModel = courseViewModel,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Loading -> {
//                    LoadingState(
//                        "Loading course...",
//                        Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Error -> {
//                    ErrorState(
//                        message = fullCourseContent!!.message ?: "Failed to load",
//                        onRetry = { courseId?.let { courseViewModel.getFullCourseContent(it) } },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                else -> {
//                    Box(
//                        modifier = Modifier
//                            .padding(paddingValues)
//                            .fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("Initializing...", color = AppColors.textSecondary)
//                    }
//                }
//            }
//        }
//
//        /* ---------------------------------------------------------
//           BOTTOM SHEET – safe, no smart-cast warnings
//           --------------------------------------------------------- */
//        if (showSheet && hasContentToShow) {
//            when (val res = fullCourseContent) {
//                is Resource.Success -> {
//                    ModalBottomSheet(
//                        onDismissRequest = { showSheet = false },
//                        sheetState = sheetState
//                    ) {
//                        TopicNavigationSheet(
//                            courseData = res.data!!,
//                            selectedSubtopicId = selectedSubtopic?.id,
//                            onSubTopicSelected = { sub ->
//                                if (sub.content != null) {
//                                    selectedSubtopic = sub
//                                    showSheet = false
//                                }
//                            },
//                            onDismiss = { showSheet = false }
//                        )
//                    }
//                }
//                else -> Unit   // not Success → sheet stays hidden
//            }
//        }
//    }
//
//    DisposableEffect(Unit) {
//        onDispose { courseViewModel.stopStreaming() }
//    }
//}
//@Composable
//fun GeneratingStateWithStreaming(
//    courseTitle: String,
//    progress: CourseViewModel.GenerationProgress?,
//    viewModel: CourseViewModel,
//    modifier: Modifier = Modifier
//) {
//    val streamingText by viewModel.currentStreamingText.collectAsState()
//    val currentSubtopic by viewModel.currentStreamingSubtopic.collectAsState()
//
//    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
//            CircularProgressIndicator(color = AppColors.primary, strokeWidth = 6.dp)
//            Spacer(Modifier.height(24.dp))
//            Text("Generating: $courseTitle", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
//
//            progress?.let {
//                Spacer(Modifier.height(16.dp))
//                LinearProgressIndicator(
//                    progress = { it.progress / 100f },
//                    modifier = Modifier.fillMaxWidth(0.8f),
//                    color = AppColors.primary,
//                    trackColor = AppColors.surface
//                )
//                Spacer(Modifier.height(8.dp))
//                Text(it.subtopic, color = AppColors.textSecondary, fontSize = 14.sp)
//                Text("${it.generated}/${it.total} subtopics", color = AppColors.textSecondary)
//            }
//
//            currentSubtopic?.let { sub ->
//                Spacer(Modifier.height(24.dp))
//                Card(
//                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
//                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
//                    elevation = CardDefaults.cardElevation(4.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(sub, fontWeight = FontWeight.SemiBold, color = AppColors.primary, fontSize = 15.sp)
//                        Spacer(Modifier.height(8.dp))
//                        val scrollState = rememberScrollState()
//                        LaunchedEffect(streamingText) {
//                            scrollState.animateScrollTo(scrollState.maxValue)
//                        }
//                        Text(
//                            text = streamingText.ifEmpty { "..." },
//                            color = AppColors.textPrimary,
//                            fontSize = 14.sp,
//                            lineHeight = 20.sp,
//                            modifier = Modifier
//                                .weight(1f)
//                                .verticalScroll(scrollState)
//                        )
//                    }
//                }
//            } ?: run {
//                if (progress != null) {
//                    Spacer(Modifier.height(16.dp))
//                    Text("Starting generation...", color = AppColors.textSecondary)
//                }
//            }
//        }
//    }
//}
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
                            // FIXED: Better content detection
                            it.content != null &&
                                    it.content != "null" &&
                                    it.content.isNotEmpty() &&
                                    !it.content.trim().equals("[]", ignoreCase = true) &&
                                    !it.content.contains("Content is being generated") &&
                                    (it.content.trim().startsWith("[") || it.content.trim().startsWith("{")) &&
                                    parseGeneratedContent(it.content) != null
                        }

                    if (firstContent != null) {
                        selectedSubtopic = firstContent
                        Log.d("UI_AUTO_SELECT", "✅ Auto-selected subtopic: ${firstContent.title} with content: ${firstContent.content?.take(50)}...")
                    } else {
                        Log.d("UI_AUTO_SELECT", "❌ No valid content found in subtopics")
                        // Debug: log all subtopics
                        data.units.flatMap { it.subtopics }.forEach { sub ->
                            Log.d("UI_AUTO_SELECT", "Subtopic: ${sub.title}, Content: ${sub.content?.take(100)}")
                        }
                    }
                }
            }
            else -> { /* Do nothing */ }
        }
    }


    // Get current content for selected subtopic - FIXED
//    val currentContent = remember(selectedSubtopic, fullCourseContent) {
//        when (fullCourseContent) {
//            is Resource.Success -> {
//                val data = (fullCourseContent as Resource.Success<CourseFullResponse>).data
//                selectedSubtopic?.let { sub ->
//                    data!!.units
//                        .flatMap { it.subtopics }
//                        .find { it.id == sub.id }
//                        ?.let { convertToSubTopicContent(it) }
//                }
//            }
//            else -> null
//        }
//    }
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
                            // FIXED: Check if content is valid before converting
                            if (it.content != null &&
                                it.content != "null" &&
                                it.content.isNotEmpty() &&
                                !it.content.trim().equals("[]", ignoreCase = true) &&
                                !it.content.contains("Content is being generated") &&
                                (it.content.trim().startsWith("[") || it.content.trim().startsWith("{")) &&
                                parseGeneratedContent(it.content) != null) {
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
        bottomBar = {
            if (currentContent != null) {
                BottomAppBar(
                    containerColor = AppColors.surface,
                    tonalElevation = 8.dp,
                    actions = {
                        Button(
                            onClick = { /* Mark Complete */ },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Mark Complete", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(onClick = { /* Save Note */ }, modifier = Modifier.height(48.dp)) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
                        }
                    }
                )
            }
        }
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
                                // FIXED: Better content detection
                                val hasValidContent = sub.content != null &&
                                        sub.content != "null" &&
                                        sub.content.isNotEmpty() &&
                                        !sub.content.trim().equals("[]", ignoreCase = true) &&
                                        !sub.content.contains("Content is being generated") &&
                                        (sub.content.trim().startsWith("[") || sub.content.trim().startsWith("{")) &&
                                        parseGeneratedContent(sub.content) != null

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

//@Composable
//fun GeneratingStateWithStreaming(
//    courseTitle: String,
//    progress: CourseViewModel.GenerationProgress?,
//    viewModel: CourseViewModel,
//    modifier: Modifier = Modifier
//) {
//    val streamingText by viewModel.currentStreamingText.collectAsState()
//    val currentSubtopic by viewModel.currentStreamingSubtopic.collectAsState()
//
//    // Remove the derivedStateOf and LaunchedEffect that were causing warnings
//    // Just use the state values directly
//
//    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
//            CircularProgressIndicator(color = AppColors.primary, strokeWidth = 6.dp)
//            Spacer(Modifier.height(24.dp))
//            Text("Generating: $courseTitle", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
//
//            progress?.let {
//                Spacer(Modifier.height(16.dp))
//                LinearProgressIndicator(
//                    progress = { it.progress / 100f },
//                    modifier = Modifier.fillMaxWidth(0.8f),
//                    color = AppColors.primary,
//                    trackColor = AppColors.surface
//                )
//                Spacer(Modifier.height(8.dp))
//                Text(it.subtopic, color = AppColors.textSecondary, fontSize = 14.sp)
//                Text("${it.generated}/${it.total} subtopics", color = AppColors.textSecondary)
//            }
//
//            if (currentSubtopic != null) {
//                Spacer(Modifier.height(24.dp))
//                Card(
//                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
//                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
//                    elevation = CardDefaults.cardElevation(4.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            currentSubtopic!!,
//                            fontWeight = FontWeight.SemiBold,
//                            color = AppColors.primary,
//                            fontSize = 15.sp
//                        )
//                        Spacer(Modifier.height(8.dp))
//                        val scrollState = rememberScrollState()
//
//                        LaunchedEffect(streamingText) {
//                            scrollState.animateScrollTo(scrollState.maxValue)
//                        }
//
//                        Text(
//                            text = streamingText.ifEmpty { "Starting generation..." },
//                            color = AppColors.textPrimary,
//                            fontSize = 14.sp,
//                            lineHeight = 20.sp,
//                            modifier = Modifier
//                                .weight(1f)
//                                .verticalScroll(scrollState)
//                        )
//                    }
//                }
//            } else {
//                if (progress != null) {
//                    Spacer(Modifier.height(16.dp))
//                    Text("Starting generation...", color = AppColors.textSecondary)
//                }
//            }
//        }
//    }
//}


// WORKING BUT NEED TO ROTATE THE SCREEN TO UPDATE THE CONTENT OF NEW ADDEED
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CourseContentScreen(
//    courseViewModel: CourseViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val courseId by courseViewModel.courseId.collectAsState()
//    val generationStatus by courseViewModel.generationStatus.collectAsState()
//    val fullCourseContent by courseViewModel.fullCourseContent.collectAsState()
//
//    var selectedSubtopic by remember { mutableStateOf<Subtopic?>(null) }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSheet by remember { mutableStateOf(false) }
//    var currentContent by remember { mutableStateOf<SubTopicContent?>(null) }
//    var allContentGenerated by remember { mutableStateOf(false) }
//    var hasContentToShow by remember { mutableStateOf(false) }
//
//    // Track if we need to load content
//    var shouldLoadContent by remember { mutableStateOf(true) }
//
//    // Load course content when screen is launched
//    LaunchedEffect(courseId) {
//        if (courseId != null && shouldLoadContent) {
//            courseViewModel.getFullCourseContent(courseId!!)
//            shouldLoadContent = false
//        }
//    }
//
//    // Process course content when it's available - ONLY ONCE when we first get content
//    LaunchedEffect(fullCourseContent) {
//        if (fullCourseContent is Resource.Success && !hasContentToShow) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null) {
//                // Find the first subtopic that has content (not null)
//                val firstContentSubtopic = courseData.units
//                    .flatMap { it.subtopics }
//                    .firstOrNull { it.content != null }
//
//                firstContentSubtopic?.let {
//                    currentContent = convertToSubTopicContent(it)
//                    selectedSubtopic = it
//                    hasContentToShow = true
//
//                    // STOP POLLING once we have content to show
//                    courseViewModel.stopPolling()
//                }
//
//                // If no content yet, start generation and polling
//                if (firstContentSubtopic == null && courseId != null && !allContentGenerated) {
//                    courseViewModel.generateCourseContent(courseId!!)
//                    delay(2000)
//                    courseViewModel.startPollingGenerationStatus(courseId!!)
//                }
//            }
//        }
//    }
//
//    // Update content when selection changes - but only if we're not in the middle of polling
//    LaunchedEffect(selectedSubtopic) {
//        if (hasContentToShow && selectedSubtopic != null) {
//            currentContent = convertToSubTopicContent(selectedSubtopic!!)
//        }
//    }
//
//    // Handle generation status updates - but only if we don't have content yet
//    LaunchedEffect(generationStatus) {
//        if (generationStatus is Resource.Success && courseId != null && !hasContentToShow) {
//            val status = (generationStatus as Resource.Success).data
//            // Check if generation is complete
//            if (status?.status == "completed") {
//                allContentGenerated = true
//                courseViewModel.stopPolling()
//                // Refresh to get the final content
//                courseViewModel.getFullCourseContent(courseId!!)
//            } else if (!hasContentToShow) {
//                // Only refresh if we don't have content yet
//                courseViewModel.getFullCourseContent(courseId!!)
//            }
//        }
//    }
//
//    // Stop polling when leaving screen
//    DisposableEffect(Unit) {
//        onDispose {
//            courseViewModel.stopPolling()
//        }
//    }
//
//    Scaffold(
//        containerColor = AppColors.background,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        currentContent?.title ?: "Course Content",
//                        color = AppColors.textPrimary,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Handle more options */ }) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "More",
//                            tint = AppColors.textPrimary
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
//            )
//        },
//        floatingActionButton = {
//            // Show FAB only when we have content and it's stable (not being refreshed)
//            if (hasContentToShow && fullCourseContent is Resource.Success) {
//                FloatingActionButton(
//                    onClick = { showSheet = true },
//                    containerColor = AppColors.primary,
//                    contentColor = AppColors.onPrimary,
//                    shape = CircleShape
//                ) {
//                    Icon(Icons.Default.List, contentDescription = "View Topics")
//                }
//            }
//        },
//        bottomBar = {
//            if (currentContent != null && hasContentToShow) {
//                BottomAppBar(
//                    containerColor = AppColors.surface,
//                    tonalElevation = 8.dp,
//                    actions = {
//                        Button(
//                            onClick = { /* Mark as Complete */ },
//                            modifier = Modifier.weight(1f).height(48.dp),
//                            shape = RoundedCornerShape(12.dp)
//                        ) {
//                            Icon(Icons.Default.Check, contentDescription = null)
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Mark Complete", fontWeight = FontWeight.SemiBold)
//                        }
//                        Spacer(modifier = Modifier.width(12.dp))
//                        OutlinedButton(
//                            onClick = { /* Save Note */ },
//                            modifier = Modifier.height(48.dp)
//                        ) {
//                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Note")
//                        }
//                    }
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                currentContent != null && hasContentToShow -> {
//                    // Display content with your exact original design
//                    CourseContentDisplay(
//                        content = currentContent!!,
//                        // Don't show generation status if we have stable content
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Success && !hasContentToShow -> {
//                    // We have course data but no content yet (all subtopics are null)
//                    val courseData = (fullCourseContent as Resource.Success).data
//                    GeneratingState(
//                        courseTitle = courseData?.course?.title ?: "Course",
//                        generationStatus = generationStatus,
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Loading -> {
//                    LoadingState(
//                        message = "Loading course content...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                fullCourseContent is Resource.Error -> {
//                    ErrorState(
//                        message = "Failed to load course content",
//                        onRetry = {
//                            shouldLoadContent = true
//                            hasContentToShow = false
//                        },
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//
//                else -> {
//                    LoadingState(
//                        message = "Initializing...",
//                        modifier = Modifier.padding(paddingValues)
//                    )
//                }
//            }
//        }
//
//        // Bottom Sheet for navigation - show all subtopics with content status
//        if (showSheet && fullCourseContent is Resource.Success && hasContentToShow) {
//            val courseData = (fullCourseContent as Resource.Success).data
//            if (courseData != null) {
//                ModalBottomSheet(
//                    onDismissRequest = { showSheet = false },
//                    sheetState = sheetState
//                ) {
//                    TopicNavigationSheet(
//                        courseData = courseData,
//                        selectedSubtopicId = selectedSubtopic?.id,
//                        onSubTopicSelected = { subtopic ->
//                            // Only allow selection of subtopics that have content
//                            if (subtopic.content != null) {
//                                selectedSubtopic = subtopic
//                                showSheet = false
//                            }
//                        },
//                        onDismiss = { showSheet = false }
//                    )
//                }
//            }
//        }
//    }
//}

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


//@Composable
//fun FormattedContentDisplay(content: String) {
//    val lines = content.split("\n")
//    var currentSection = ""
//
//    Column {
//        lines.forEach { line ->
//            when {
//                line.startsWith("Why this matters:") -> {
//                    currentSection = "why_matters"
//                    Text(
//                        "Why this matters:",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = AppColors.textPrimary,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//                line.startsWith("Core Concepts:") -> {
//                    currentSection = "core_concepts"
//                    Text(
//                        "Core Concepts:",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = AppColors.textPrimary,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//                line.startsWith("Examples:") -> {
//                    currentSection = "examples"
//                    Text(
//                        "Examples:",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = AppColors.textPrimary,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//                line.startsWith("Code Example:") -> {
//                    currentSection = "code"
//                    Text(
//                        "Code Example:",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = AppColors.textPrimary,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//                line.startsWith("•") && currentSection == "core_concepts" -> {
//                    val parts = line.removePrefix("•").split(":", limit = 2)
//                    if (parts.size == 2) {
//                        val concept = parts[0].trim()
//                        val explanation = parts[1].trim()
//                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
//                            Text(
//                                "• $concept:",
//                                fontWeight = FontWeight.SemiBold,
//                                color = AppColors.textPrimary
//                            )
//                            Text(
//                                explanation,
//                                color = AppColors.textSecondary,
//                                modifier = Modifier.padding(start = 16.dp)
//                            )
//                        }
//                    } else {
//                        Text(line, color = AppColors.textSecondary)
//                    }
//                }
//                line.matches(Regex("\\d+\\. \\[.*\\]:.*")) && currentSection == "examples" -> {
//                    val parts = line.split("]:", limit = 2)
//                    if (parts.size == 2) {
//                        val numberAndType = parts[0].removePrefix("[").split(" [")
//                        val exampleContent = parts[1].trim()
//                        if (numberAndType.size == 2) {
//                            val number = numberAndType[0]
//                            val type = numberAndType[1]
//                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
//                                Text(
//                                    "$number [${type.replaceFirstChar { it.uppercase() }}]:",
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = AppColors.textPrimary
//                                )
//                                Text(
//                                    exampleContent,
//                                    color = AppColors.textSecondary,
//                                    modifier = Modifier.padding(start = 16.dp),
//                                    fontStyle = if (type.contains("analog", ignoreCase = true))
//                                        FontStyle.Italic else FontStyle.Normal
//                                )
//                            }
//                        }
//                    }
//                }
//                currentSection == "code" && line.isNotBlank() -> {
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
//                    ) {
//                        Text(
//                            line,
//                            color = Color.White,
//                            fontSize = 12.sp,
//                            modifier = Modifier.padding(12.dp),
//                            fontFamily = FontFamily.Monospace
//                        )
//                    }
//                }
//                line.isNotBlank() -> {
//                    Text(
//                        line,
//                        color = when (currentSection) {
//                            "why_matters" -> AppColors.textSecondary
//                            else -> AppColors.textPrimary
//                        },
//                        fontSize = 16.sp,
//                        lineHeight = 24.sp
//                    )
//                }
//            }
//        }
//    }
//}

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



@Composable
fun VideoThumbnailCard(video: UiVideo) {  // Changed parameter type to UiVideo
    val context = LocalContext.current
    var showVideoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
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
            Text(
                text = video.title,
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showVideoDialog) {
        AlertDialog(
            onDismissRequest = { showVideoDialog = false },
            title = { Text(video.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            text = {
                Column {
                    Text(
                        "Open this video in YouTube?",
                        color = AppColors.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Convert thumbnail URL to watch URL
                        val videoId = video.thumbnailUrl.substringAfter("vi/").substringBefore("/")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Watch on YouTube")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVideoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
        // First parse the backend JSON
        val parsedContent = parseGeneratedContent(subtopic.content)
        if (parsedContent == null) {
            Log.e("CONTENT_CONVERSION", "Failed to parse content for: ${subtopic.title}")
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
                UiVideo(video.title, video.thumbnail)
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

//private fun parseGeneratedContent(jsonString: String?): GeneratedSubtopicContent? {
//    if (jsonString == null) return null
//    return try {
//        Gson().fromJson(jsonString, GeneratedSubtopicContent::class.java)
//    } catch (e: Exception) {
//        null
//    }
//}
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