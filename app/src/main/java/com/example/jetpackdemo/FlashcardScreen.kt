package com.example.jetpackdemo

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jetpackdemo.data.model.FlashcardItem
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.FlashcardViewModel
import com.example.jetpackdemo.viewmodels.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    navController: NavController,
    viewModel: FlashcardViewModel,
    subtopicId: String,
    subtopicTitle: String? = null
) {
    val flashcardsState by viewModel.flashcards.collectAsState()
    val currentIndex by viewModel.currentCardIndex.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val reviewResult by viewModel.reviewResult.collectAsState()

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        subtopicTitle ?: "Flashcards",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (val state = flashcardsState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Generating flashcards...", color = AppColors.textSecondary)
                        Text("This may take a moment", color = AppColors.textSecondary, fontSize = 12.sp)
                    }
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message ?: "Unknown error", color = AppColors.textSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadFlashcards(subtopicId) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is Resource.Success -> {
                val cards = state.data ?: emptyList()
                if (cards.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No flashcards available", color = AppColors.textSecondary)
                    }
                } else {
                    FlashcardContent(
                        cards = cards,
                        currentIndex = currentIndex,
                        isFlipped = isFlipped,
                        onFlip = { viewModel.flipCard() },
                        onNext = { viewModel.nextCard() },
                        onPrevious = { viewModel.previousCard() },
                        onReview = { cardId, quality -> viewModel.reviewFlashcard(cardId, quality) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    // Show review result snackbar
    LaunchedEffect(reviewResult) {
        reviewResult?.let {
            if (it is Resource.Success) {
                Log.d("FlashcardScreen", "Review submitted, next review in ${it.data?.intervalDays} days")
            }
        }
    }
}

@Composable
private fun FlashcardContent(
    cards: List<FlashcardItem>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onReview: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val card = cards[currentIndex]
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "flipAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        Text(
            "${currentIndex + 1} / ${cards.size}",
            color = AppColors.textSecondary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / cards.size },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = AppColors.primary,
            trackColor = AppColors.primary.copy(alpha = 0.15f)
        )

        Spacer(Modifier.height(24.dp))

        // Card type badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppColors.primary.copy(alpha = 0.1f)
        ) {
            Text(
                card.cardType.replaceFirstChar { it.uppercase() },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = AppColors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))

        // Flashcard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { onFlip() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlipped) Color(0xFFF0F9FF) else AppColors.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            "Question",
                            tint = AppColors.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            card.front,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.textPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Tap to reveal answer",
                            color = AppColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // Back side (text needs to be mirrored to appear correctly)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "Answer",
                            tint = AppColors.progressGreen.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            card.back,
                            fontSize = 18.sp,
                            color = AppColors.textPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Review buttons (only when flipped)
        if (isFlipped) {
            Text("How well did you know this?", color = AppColors.textSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewButton(
                    text = "Again",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = { onReview(card.id, 1) }
                )
                ReviewButton(
                    text = "Hard",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { onReview(card.id, 2) }
                )
                ReviewButton(
                    text = "Good",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { onReview(card.id, 4) }
                )
                ReviewButton(
                    text = "Easy",
                    color = AppColors.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onReview(card.id, 5) }
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = currentIndex > 0
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Previous",
                    tint = if (currentIndex > 0) AppColors.textPrimary else AppColors.textSecondary.copy(alpha = 0.3f)
                )
            }

            Text(
                "Tap card to flip",
                color = AppColors.textSecondary,
                fontSize = 12.sp
            )

            IconButton(
                onClick = onNext,
                enabled = currentIndex < cards.size - 1
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    "Next",
                    tint = if (currentIndex < cards.size - 1) AppColors.textPrimary else AppColors.textSecondary.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun ReviewButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.12f),
            contentColor = color
        ),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
