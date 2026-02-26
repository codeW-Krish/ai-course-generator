package com.example.jetpackdemo

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jetpackdemo.data.model.ChatMessage
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.InteractiveViewModel
import com.example.jetpackdemo.viewmodels.InteractiveViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorChatScreen(
    navController: NavController,
    subtopicId: String,
    subtopicTitle: String? = null
) {
    val context = LocalContext.current.applicationContext
    val factory = InteractiveViewModelFactory(context as Application)
    val viewModel: InteractiveViewModel = viewModel(factory = factory)
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Set subtopic context on first load
    LaunchedEffect(subtopicId) {
        viewModel.loadSession(subtopicId)
    }

    // Auto-scroll to bottom
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AI Tutor",
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            subtopicTitle ?: "Ask me anything",
                            color = AppColors.textSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            // Input field
            Surface(
                shadowElevation = 8.dp,
                color = AppColors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask a question...", color = AppColors.textSecondary) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = AppColors.background,
                            unfocusedContainerColor = AppColors.background
                        ),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank() && !isChatLoading) {
                                viewModel.sendChatMessage(messageText.trim())
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (messageText.isNotBlank()) AppColors.primary else AppColors.primary.copy(alpha = 0.4f),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        if (chatMessages.isEmpty() && !isChatLoading) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SmartToy,
                                "AI Tutor",
                                tint = AppColors.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "AI Tutor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AppColors.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ask me anything about this topic. I can explain concepts, give examples, or help you understand tricky parts.",
                        color = AppColors.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    // Suggested prompts
                    val suggestions = listOf(
                        "Explain this topic simply",
                        "Give me a real-world example",
                        "What are common mistakes?",
                        "How does this connect to other topics?"
                    )
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            text = suggestion,
                            onClick = {
                                viewModel.sendChatMessage(suggestion)
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        } else {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
                    ChatBubble(message)
                }
                if (isChatLoading) {
                    item {
                        Row(
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.SmartToy, null, tint = AppColors.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AppColors.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Thinking...", color = AppColors.textSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = AppColors.primary.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SmartToy, null, tint = AppColors.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) AppColors.primary else AppColors.surface
            ),
            elevation = CardDefaults.cardElevation(if (isUser) 0.dp else 1.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                message.text,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else AppColors.textPrimary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = AppColors.primary.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = AppColors.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text, fontSize = 13.sp, color = AppColors.textPrimary)
    }
}
