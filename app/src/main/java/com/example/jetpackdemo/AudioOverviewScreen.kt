package com.example.jetpackdemo

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.example.jetpackdemo.data.model.AudioData
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.AudioViewModel
import com.example.jetpackdemo.viewmodels.Resource
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioOverviewScreen(
    navController: NavController,
    viewModel: AudioViewModel,
    subtopicId: String? = null,
    courseId: String? = null,
    title: String? = null
) {
    val audioState by viewModel.audioState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title ?: "Audio Overview",
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
        when (val state = audioState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Generating audio...", color = AppColors.textSecondary)
                        Text("This may take a minute", fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message ?: "Failed to load audio", color = AppColors.textSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                when {
                                    subtopicId != null -> viewModel.loadSubtopicAudio(subtopicId)
                                    courseId != null -> viewModel.loadCourseAudio(courseId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) { Text("Retry") }
                    }
                }
            }
            is Resource.Success -> {
                val audioData = state.data?.audio ?: return@Scaffold
                AudioContent(
                    audioData = audioData,
                    isPlaying = isPlaying,
                    onPlayingChanged = { viewModel.setPlaying(it) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun AudioContent(
    audioData: AudioData,
    isPlaying: Boolean,
    onPlayingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    var isPrepared by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Create ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            isPrepared = true
                            isBuffering = false
                            duration = this@apply.duration.toFloat().coerceAtLeast(0f)
                        }
                        Player.STATE_BUFFERING -> {
                            isBuffering = true
                        }
                        Player.STATE_ENDED -> {
                            onPlayingChanged(false)
                        }
                        Player.STATE_IDLE -> { /* no-op */ }
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    onPlayingChanged(playing)
                }

                override fun onPlayerError(playerError: PlaybackException) {
                    error = "Playback error: ${playerError.message}"
                    isBuffering = false
                    onPlayingChanged(false)
                    Log.e("Audio", "ExoPlayer error", playerError)
                }
            })
        }
    }

    // Set media source when URL is available
    LaunchedEffect(audioData.audioUrl) {
        if (audioData.audioUrl.isNotBlank()) {
            val mediaItem = MediaItem.fromUri(audioData.audioUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    // Timer to track position
    LaunchedEffect(isPlaying) {
        while (isPlaying && isPrepared) {
            currentPosition = exoPlayer.currentPosition.toFloat().coerceAtLeast(0f)
            duration = exoPlayer.duration.toFloat().coerceAtLeast(0f)
            delay(500)
        }
    }

    // Clean up on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Audio info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Audio icon
                Surface(
                    shape = CircleShape,
                    color = AppColors.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Headphones,
                            "Audio",
                            tint = AppColors.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Title
                Text(
                    audioData.subtopicTitle ?: audioData.courseTitle ?: "Audio Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                if (audioData.unitTitle != null) {
                    Text(audioData.unitTitle, fontSize = 14.sp, color = AppColors.textSecondary)
                }

                Spacer(Modifier.height(8.dp))

                // Metadata row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (audioData.estimatedDuration > 0) {
                        InfoChip(
                            icon = Icons.Default.Timer,
                            text = formatDuration(audioData.estimatedDuration)
                        )
                    }
                    if (audioData.segmentCount > 0) {
                        InfoChip(
                            icon = Icons.Default.Segment,
                            text = "${audioData.segmentCount} segments"
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Progress slider
                if (isPrepared && duration > 0f) {
                    Slider(
                        value = currentPosition / duration,
                        onValueChange = { fraction ->
                            exoPlayer.seekTo((fraction * duration).toLong())
                            currentPosition = fraction * duration
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AppColors.primary,
                            activeTrackColor = AppColors.primary,
                            inactiveTrackColor = AppColors.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatMillis(currentPosition.toLong()), color = AppColors.textSecondary, fontSize = 12.sp)
                        Text(formatMillis(duration.toLong()), color = AppColors.textSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Error display
                error?.let { err ->
                    Text(err, color = Color(0xFFEF4444), fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                }

                // Play/Pause button
                if (audioData.audioUrl.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 10s
                        IconButton(
                            onClick = {
                                val newPos = maxOf(0L, exoPlayer.currentPosition - 10000)
                                exoPlayer.seekTo(newPos)
                            },
                            enabled = isPrepared
                        ) {
                            Icon(Icons.Default.Replay10, "Rewind", tint = AppColors.textSecondary)
                        }

                        // Play/Pause
                        FloatingActionButton(
                            onClick = {
                                if (isBuffering) return@FloatingActionButton
                                error = null
                                if (isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            },
                            containerColor = AppColors.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    if (isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Forward 10s
                        IconButton(
                            onClick = {
                                val newPos = minOf(exoPlayer.duration, exoPlayer.currentPosition + 10000)
                                exoPlayer.seekTo(newPos)
                            },
                            enabled = isPrepared
                        ) {
                            Icon(Icons.Default.Forward10, "Forward", tint = AppColors.textSecondary)
                        }
                    }
                } else {
                    Text("No audio URL available", color = AppColors.textSecondary, fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Script text
        if (audioData.script.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Article,
                            "Script",
                            tint = AppColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Audio Script", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        audioData.script,
                        color = AppColors.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AppColors.primary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = AppColors.primary, fontSize = 12.sp)
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins}m ${secs}s"
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%d:%02d".format(mins, secs)
}
