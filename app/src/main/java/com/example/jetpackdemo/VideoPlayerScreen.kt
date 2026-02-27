package com.example.jetpackdemo

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetpackdemo.data.model.VideoManifest
import com.example.jetpackdemo.data.model.VideoScene
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.Resource
import com.example.jetpackdemo.viewmodels.VideoViewModel
import kotlinx.coroutines.delay

// ─────────────────────────────────────────
//  Color constants for a cinematic player
// ─────────────────────────────────────────

private val PlayerBg = Color(0xFF0A0A1A)
private val PlayerSurface = Color(0xFF151528)
private val PlayerAccent = Color(0xFF4F86F7)
private val PlayerTextPrimary = Color(0xFFFFFFFF)
private val PlayerTextSecondary = Color(0xFFA0A0C0)
private val PlayerOverlay = Color(0x99000000)

// ─────────────────────────────────────────
//  Scene type → display info
// ─────────────────────────────────────────

private fun sceneTypeLabel(type: String): String = when (type) {
    "diagram" -> "Diagram"
    "code" -> "Code"
    "timeline" -> "Timeline"
    "comparison" -> "Comparison"
    "quote" -> "Key Quote"
    "illustration" -> "Illustration"
    else -> type.replaceFirstChar { it.uppercase() }
}

private fun sceneTypeColor(type: String): Color = when (type) {
    "diagram" -> Color(0xFF4F86F7)
    "code" -> Color(0xFFC792EA)
    "timeline" -> Color(0xFF28A745)
    "comparison" -> Color(0xFFFFB400)
    "quote" -> Color(0xFFE74C3C)
    "illustration" -> Color(0xFF00BCD4)
    else -> PlayerAccent
}

// ─────────────────────────────────────────
//  Main Screen
// ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    viewModel: VideoViewModel,
    subtopicId: String,
    title: String? = null
) {
    val manifestState by viewModel.manifestState.collectAsState()
    val currentSceneIndex by viewModel.currentSceneIndex.collectAsState()
    val currentSubsceneIndex by viewModel.currentSubsceneIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val overallProgress by viewModel.playbackProgress.collectAsState()
    val sceneProgress by viewModel.sceneProgress.collectAsState()

    // Load manifest on first composition
    LaunchedEffect(subtopicId) {
        viewModel.loadManifest(subtopicId)
    }

    Scaffold(
        containerColor = PlayerBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title ?: "Video Lesson",
                        color = PlayerTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PlayerTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (val state = manifestState) {
            is Resource.Loading -> {
                LoadingState(Modifier.padding(padding))
            }
            is Resource.Error -> {
                VideoErrorState(
                    message = state.message ?: "Failed to load video",
                    onRetry = { viewModel.loadManifest(subtopicId) },
                    modifier = Modifier.padding(padding)
                )
            }
            is Resource.Success -> {
                val manifest = state.data ?: return@Scaffold
                PlayerContent(
                    manifest = manifest,
                    currentSceneIndex = currentSceneIndex,
                    currentSubsceneIndex = currentSubsceneIndex,
                    isPlaying = isPlaying,
                    overallProgress = overallProgress,
                    sceneProgress = sceneProgress,
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

// ─────────────────────────────────────────
//  Loading State
// ─────────────────────────────────────────

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PlayerAccent, strokeWidth = 3.dp)
            Spacer(Modifier.height(20.dp))
            Text("Generating video lesson...", color = PlayerTextPrimary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Creating script, visuals & audio",
                color = PlayerTextSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "This may take 1–2 minutes",
                color = PlayerTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

// ─────────────────────────────────────────
//  Error State
// ─────────────────────────────────────────

@Composable
private fun VideoErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Warning, "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = PlayerTextSecondary, textAlign = TextAlign.Center, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PlayerAccent)
            ) { Text("Retry") }
        }
    }
}

// ─────────────────────────────────────────
//  Main Player Content
// ─────────────────────────────────────────

@Composable
private fun PlayerContent(
    manifest: VideoManifest,
    currentSceneIndex: Int,
    currentSubsceneIndex: Int,
    isPlaying: Boolean,
    overallProgress: Float,
    sceneProgress: Float,
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val scene = manifest.scenes.getOrNull(currentSceneIndex) ?: return

    Column(modifier = modifier.fillMaxSize()) {
        // === Visual display area ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            SceneVisualDisplay(
                scene = scene,
                currentSubsceneIndex = currentSubsceneIndex,
                isPlaying = isPlaying,
                viewModel = viewModel
            )

            // Scene type badge (top-left)
            SceneTypeBadge(
                sceneType = scene.sceneType,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )

            // Scene counter (top-right)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PlayerOverlay,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text(
                    "${currentSceneIndex + 1} / ${manifest.scenes.size}",
                    color = PlayerTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Subscene text overlay (bottom of visual)
            val subscene = scene.subscenes.getOrNull(currentSubsceneIndex)
            if (subscene != null && subscene.textOverlay.isNotBlank()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC000000))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            subscene.textOverlay,
                            color = PlayerTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // === Info + Controls area ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PlayerSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Key concept
            if (scene.keyConcept.isNotBlank()) {
                Text(
                    scene.keyConcept,
                    color = PlayerTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
            }

            // Narration text (subtitle)
            Text(
                scene.narrationText,
                color = PlayerTextSecondary,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            // Scene progress bar
            LinearProgressIndicator(
                progress = { sceneProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PlayerAccent,
                trackColor = Color(0xFF2A2A4A),
            )

            Spacer(Modifier.height(4.dp))

            // Time labels
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    formatTime((sceneProgress * scene.durationSeconds).toInt()),
                    color = PlayerTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    formatTime(scene.durationSeconds.toInt()),
                    color = PlayerTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Playback controls
            PlaybackControls(
                isPlaying = isPlaying,
                canGoPrevious = currentSceneIndex > 0,
                canGoNext = currentSceneIndex < manifest.scenes.size - 1,
                onPlayPause = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.previousScene() },
                onNext = { viewModel.nextScene() }
            )

            Spacer(Modifier.height(8.dp))

            // Overall progress
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = Color(0xFF28A745),
                trackColor = Color(0xFF1A1A2E),
            )

            Spacer(Modifier.height(4.dp))

            // Overall duration
            Text(
                "Total: ${formatTime(manifest.totalDurationSeconds.toInt())}",
                color = PlayerTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────
//  Scene Visual Display with ExoPlayer
// ─────────────────────────────────────────

@Composable
private fun SceneVisualDisplay(
    scene: VideoScene,
    currentSubsceneIndex: Int,
    isPlaying: Boolean,
    viewModel: VideoViewModel
) {
    val context = LocalContext.current

    // ExoPlayer for this scene's audio
    val exoPlayer = remember(scene.sceneIndex) {
        ExoPlayer.Builder(context).build()
    }

    // Set up audio when scene changes
    LaunchedEffect(scene.audioUrl, scene.sceneIndex) {
        if (scene.audioUrl.isNotBlank()) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = MediaItem.fromUri(scene.audioUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    // Play/pause sync
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Progress tracking
    LaunchedEffect(isPlaying, scene.sceneIndex) {
        while (isPlaying) {
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration.coerceAtLeast(1)
            viewModel.onAudioProgress(pos, dur)
            delay(100)
        }
    }

    // Listen for completion
    DisposableEffect(scene.sceneIndex) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.onSceneAudioComplete()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoPlayer", "Audio error for scene ${scene.sceneIndex}", error)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Visual display with animation
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBg),
        contentAlignment = Alignment.Center
    ) {
        // Animated scene image
        val animScale = remember { Animatable(1f) }

        LaunchedEffect(scene.animationStyle, isPlaying) {
            if (!isPlaying) return@LaunchedEffect
            when (scene.animationStyle) {
                "zoom" -> {
                    // Ken Burns: slow zoom from 1.0 to 1.08
                    animScale.animateTo(
                        targetValue = 1.08f,
                        animationSpec = tween(
                            durationMillis = (scene.durationSeconds * 1000).toInt(),
                            easing = LinearEasing
                        )
                    )
                }
                "parallax" -> {
                    // Subtle slow zoom
                    animScale.animateTo(
                        targetValue = 1.04f,
                        animationSpec = tween(
                            durationMillis = (scene.durationSeconds * 1000).toInt(),
                            easing = LinearEasing
                        )
                    )
                }
                else -> {
                    // No scale animation for other styles
                    animScale.snapTo(1f)
                }
            }
        }

        // Reset scale when scene changes
        LaunchedEffect(scene.sceneIndex) {
            animScale.snapTo(1f)
        }

        // Crossfade between scenes (AnimatedContent handles transition)
        AnimatedContent(
            targetState = scene.sceneIndex,
            transitionSpec = {
                fadeIn(tween(600)) togetherWith fadeOut(tween(600))
            },
            label = "scene_transition"
        ) { _ ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(scene.visualUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = scene.keyConcept,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = animScale.value,
                        scaleY = animScale.value
                    )
            )
        }

        // Subscene transition overlay (visual change indicator)
        AnimatedContent(
            targetState = currentSubsceneIndex,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "subscene_transition"
        ) { subIdx ->
            // Subtle pulse when subscene changes
            val subscene = scene.subscenes.getOrNull(subIdx)
            if (subscene != null && scene.subscenes.size > 1) {
                // Subscene indicator dots (bottom of visual area)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    scene.subscenes.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(if (idx == subIdx) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == subIdx) PlayerAccent
                                    else PlayerTextSecondary.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────
//  Scene Type Badge
// ─────────────────────────────────────────

@Composable
private fun SceneTypeBadge(sceneType: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = sceneTypeColor(sceneType).copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Text(
            sceneTypeLabel(sceneType),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────
//  Playback Controls
// ─────────────────────────────────────────

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous
        IconButton(
            onClick = onPrevious,
            enabled = canGoPrevious
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                "Previous",
                tint = if (canGoPrevious) PlayerTextPrimary else PlayerTextSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // Play/Pause (large)
        Surface(
            shape = CircleShape,
            color = PlayerAccent,
            modifier = Modifier
                .size(56.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPlayPause
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // Next
        IconButton(
            onClick = onNext,
            enabled = canGoNext
        ) {
            Icon(
                Icons.Default.SkipNext,
                "Next",
                tint = if (canGoNext) PlayerTextPrimary else PlayerTextSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ─────────────────────────────────────────
//  Time formatter
// ─────────────────────────────────────────

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
