package com.example.jetpackdemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetpackdemo.ui.theme.AppColors

// --- Data Models for this screen ---
data class Video(val title: String, val thumbnailUrl: String)

data class SubTopicContent(
    val id: Int,
    val title: String,
    val readTimeMinutes: Int,
    val content: String,
    val videos: List<Video>
)

// --- Expanded Mock Data with longer text explanations ---
private val allSubTopicContents = mapOf(
    101 to SubTopicContent(
        id = 101,
        title = "What is Machine Learning?",
        readTimeMinutes = 8,
        content = "Machine Learning is a transformative subset of artificial intelligence (AI) that empowers computers to learn from data and make decisions without being explicitly programmed for every single scenario. Instead of relying on a rigid set of hard-coded rules, machine learning algorithms are designed to identify patterns, relationships, and insights within large datasets.\n\nThink of it like teaching a child to recognize a cat. You don't list out every possible feature of a cat (pointy ears, whiskers, four legs, a tail). Instead, you show them hundreds of pictures of different cats. Over time, the child's brain learns to identify the underlying patterns that define a 'cat' on its own. Machine learning works in a similar fashion; we feed algorithms vast amounts of data, and they learn to perform tasks like classification, prediction, and clustering by recognizing these inherent patterns.\n\nThis capability is revolutionary because it allows us to solve problems that are too complex or would change too frequently for traditional programming. From recommending movies you might like, to detecting fraudulent credit card transactions, machine learning is the engine behind many of the intelligent systems we interact with every day.",
        videos = listOf(
            Video("Machine Learning Explained", "https://img.youtube.com/vi/ukzFI9rgMRA/0.jpg"),
            Video("Intro to AI", "https://img.youtube.com/vi/ad79nYk2keg/0.jpg")
        )
    ),
    102 to SubTopicContent(
        id = 102,
        title = "Types of ML Algorithms",
        readTimeMinutes = 12,
        content = "Machine learning algorithms are generally categorized into three main types: Supervised, Unsupervised, and Reinforcement Learning. The choice of which to use depends heavily on the nature of the problem you're trying to solve and the kind of data you have available.\n\nSupervised Learning is the most common type. In this approach, the algorithm is trained on a labeled dataset, meaning each piece of data is tagged with a correct answer or outcome. The goal is for the algorithm to learn the mapping function that can predict the output for new, unseen data. Common examples include spam detection in emails (labeled as 'spam' or 'not spam') and predicting house prices based on features like size and location (labeled with the actual sale price).\n\nUnsupervised Learning, on the other hand, deals with unlabeled data. The algorithm tries to find patterns and structures within the data on its own, without any predefined outcomes. This is often used for exploratory data analysis. Key applications include customer segmentation (grouping customers with similar purchasing habits) and anomaly detection (finding unusual data points that don't fit with the rest of the dataset).",
        videos = listOf(Video("ML Algorithms Overview", "https://img.youtube.com/vi/I74o3p_fK_c/0.jpg"))
    ),
    201 to SubTopicContent(
        id = 201,
        title = "Data Cleaning",
        readTimeMinutes = 10,
        content = "Data cleaning, also known as data cleansing or data scrubbing, is the process of detecting and correcting (or removing) corrupt, inaccurate, or irrelevant records from a dataset. It is arguably one of the most important and time-consuming steps in the entire machine learning workflow, as the quality of the data directly and significantly impacts the quality of the model's predictions. The principle of 'garbage in, garbage out' is especially true in machine learning.\n\nThis process involves several key tasks. One of the most common is handling missing values, where some data points are empty. A data scientist might choose to fill these gaps with the mean or median value of the column, or in some cases, remove the entire row if the missing data is too significant. Another task is correcting structural errors, such as typos or inconsistent naming conventions (e.g., 'New York' vs. 'NY').\n\nFinally, data cleaning also involves identifying and handling outliers—data points that are significantly different from other observations. These can be legitimate data points or errors, and deciding how to treat them requires careful domain knowledge. By performing these steps, we ensure that the machine learning model is trained on accurate, consistent, and reliable data, leading to more trustworthy results.",
        videos = emptyList()
    ),
    202 to SubTopicContent(
        id = 202,
        title = "Feature Scaling",
        readTimeMinutes = 7,
        content = "Feature scaling is a critical data preprocessing step used to normalize the range of independent variables or features of data. When a dataset contains features that vary widely in magnitudes, units, and range, some machine learning algorithms might not perform correctly. For instance, algorithms that compute the distance between data points, like K-Nearest Neighbors (KNN) or Support Vector Machines (SVM), are highly sensitive to the scale of the data.\n\nA feature with a broad range of values can dominate the distance calculation, making the model biased towards that feature. To prevent this, we scale the features to bring them all into a similar range. The two most common methods of feature scaling are Normalization and Standardization.\n\nNormalization (or Min-Max Scaling) scales the data to a fixed range, usually 0 to 1. Standardization, on the other hand, transforms the data to have a mean of 0 and a standard deviation of 1. The choice between them depends on the algorithm and the distribution of the data, but performing one of them is often essential for building a high-performing model.",
        videos = listOf(Video("Normalization vs Standardization", "https://img.youtube.com/vi/mnk_N_fL-5E/0.jpg"))
    )
)

private val mockUnitsForContent = listOf(
    UnitItem(1, "Unit 1: Getting Started", 3, 45, listOf(
        SubTopic(101, "What is Machine Learning?"),
        SubTopic(102, "Types of ML Algorithms"),
    )),
    UnitItem(2, "Unit 2: Data Preprocessing", 4, 60, listOf(
        SubTopic(201, "Data Cleaning"),
        SubTopic(202, "Feature Scaling"),
    ))
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseContentScreen(onNavigateBack: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var expandedUnitId by remember { mutableStateOf<Int?>(null) }
    var currentSubTopic by remember { mutableStateOf(allSubTopicContents.values.first()) }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text(currentSubTopic.title, color = AppColors.textPrimary, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle more options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = AppColors.primary,
                contentColor = AppColors.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.List, contentDescription = "View Topics")
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = AppColors.surface,
                tonalElevation = 8.dp,
                actions = {
                    Button(
                        onClick = { /* Mark as Complete */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Complete")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { /* Save Note */ },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Note")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(currentSubTopic.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = "Read time", tint = AppColors.textSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${currentSubTopic.readTimeMinutes} min read", color = AppColors.textSecondary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(currentSubTopic.content, color = AppColors.textPrimary, fontSize = 16.sp, lineHeight = 24.sp)
                Spacer(modifier = Modifier.height(24.dp))
                if (currentSubTopic.videos.isNotEmpty()) {
                    Text("Watch Related Videos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(currentSubTopic.videos) { video ->
                        VideoThumbnailCard(video)
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(mockUnitsForContent) { unit ->
                        UnitItemView(
                            unit = unit,
                            isExpanded = expandedUnitId == unit.id,
                            onExpand = {
                                expandedUnitId = if (expandedUnitId == unit.id) null else unit.id
                            },
                            onSubTopicSelected = { subTopic ->
                                // Find the new content from our mock data map
                                val newContent = allSubTopicContents[subTopic.id]
                                if (newContent != null) {
                                    currentSubTopic = newContent
                                }
                                showSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoThumbnailCard(video: Video) {
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
                maxLines = 2
            )
        }
    }
}

@Composable
fun UnitItemView(
    unit: UnitItem,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onSubTopicSelected: (SubTopic) -> Unit
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
            unit.subTopics.forEach { subTopic ->
                Text(
                    text = subTopic.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubTopicSelected(subTopic) }
                        .padding(start = 32.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun CourseContentScreenPreview() {
    CourseContentScreen(onNavigateBack = {})
}
