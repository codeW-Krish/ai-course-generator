package com.example.jetpackdemo

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetpackdemo.data.model.FollowUser
import com.example.jetpackdemo.data.model.UserCoursePreview
import com.example.jetpackdemo.data.model.UserProfile
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.Resource
import com.example.jetpackdemo.viewmodels.UserViewModel

// === VIEW OTHER USER'S PROFILE ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorProfileScreen(
    userId: String,
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onCourseClick: (String) -> Unit = {},
    onFollowersClick: (String) -> Unit = {},
    onFollowingClick: (String) -> Unit = {}
) {
    val profileState by userViewModel.userProfile.collectAsStateWithLifecycle()
    val followAction by userViewModel.followAction.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        userViewModel.loadUserProfile(userId)
    }

    LaunchedEffect(followAction) {
        when (followAction) {
            is Resource.Success -> {
                Toast.makeText(context, (followAction as Resource.Success).data?.message ?: "Done", Toast.LENGTH_SHORT).show()
                userViewModel.clearFollowAction()
            }
            is Resource.Error -> {
                Toast.makeText(context, (followAction as Resource.Error).message ?: "Failed", Toast.LENGTH_SHORT).show()
                userViewModel.clearFollowAction()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when (profileState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(profileState.message ?: "Failed to load profile", color = AppColors.textSecondary)
                    }
                }
            }
            is Resource.Success -> {
                val profile = profileState.data ?: return@Scaffold
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile header
                    item {
                        CreatorProfileHeader(
                            profile = profile,
                            onFollowToggle = {
                                if (profile.isFollowing) userViewModel.unfollowUser(userId)
                                else userViewModel.followUser(userId)
                            },
                            onFollowersClick = { onFollowersClick(userId) },
                            onFollowingClick = { onFollowingClick(userId) }
                        )
                    }

                    // Courses section
                    if (!profile.courses.isNullOrEmpty()) {
                        item {
                            Text(
                                "Courses by ${profile.username}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = AppColors.textPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        items(profile.courses) { course ->
                            CreatorCourseCard(
                                course = course,
                                onClick = { onCourseClick(course.id) }
                            )
                        }
                    } else {
                        item {
                            Box(
                                Modifier.fillMaxWidth().height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No public courses yet", color = AppColors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorProfileHeader(
    profile: UserProfile,
    onFollowToggle: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Profile picture
        if (profile.profileImageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profile.profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile picture",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AppColors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile.username.first().uppercase(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primary
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(profile.username, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)

        profile.bio?.let { bio ->
            Spacer(Modifier.height(6.dp))
            Text(
                bio,
                fontSize = 14.sp,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Social stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(count = profile.coursesCount, label = "Courses", onClick = {})
                Box(Modifier.width(1.dp).height(40.dp).background(AppColors.textSecondary.copy(alpha = 0.2f)))
                StatItem(count = profile.followersCount, label = "Followers", onClick = onFollowersClick)
                Box(Modifier.width(1.dp).height(40.dp).background(AppColors.textSecondary.copy(alpha = 0.2f)))
                StatItem(count = profile.followingCount, label = "Following", onClick = onFollowingClick)
            }
        }

        // Follow / Unfollow button
        if (!profile.isOwnProfile) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onFollowToggle,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = if (profile.isFollowing) {
                    ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textPrimary)
                } else {
                    ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                },
                border = if (profile.isFollowing) BorderStroke(1.dp, AppColors.textSecondary.copy(alpha = 0.3f)) else null
            ) {
                Icon(
                    if (profile.isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (profile.isFollowing) "Unfollow" else "Follow",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CreatorCourseCard(
    course: UserCoursePreview,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AppColors.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.School, null, tint = AppColors.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    course.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = AppColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (course.description != null) {
                    Text(
                        course.description,
                        fontSize = 13.sp,
                        color = AppColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    course.difficulty?.let { diff ->
                        Text(
                            diff.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            color = when (diff.lowercase()) {
                                "beginner" -> Color(0xFF10B981)
                                "intermediate" -> Color(0xFFF59E0B)
                                "advanced" -> Color(0xFFEF4444)
                                else -> AppColors.textSecondary
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                    course.status?.let { status ->
                        Text("• $status", fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

// === FOLLOWERS / FOLLOWING LIST SCREEN ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    userId: String,
    isFollowers: Boolean,
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val followersState by userViewModel.followers.collectAsStateWithLifecycle()
    val followingState by userViewModel.following.collectAsStateWithLifecycle()

    LaunchedEffect(userId, isFollowers) {
        if (isFollowers) userViewModel.loadFollowers(userId)
        else userViewModel.loadFollowing(userId)
    }

    val title = if (isFollowers) "Followers" else "Following"

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = { Text(title, color = AppColors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        val state = if (isFollowers) followersState else followingState

        when (state) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.message ?: "Error", color = Color.Red)
                }
            }
            is Resource.Success -> {
                val users: List<FollowUser> = if (isFollowers) {
                    (state as? Resource.Success<*>)?.data?.let { data ->
                        (data as? com.example.jetpackdemo.data.model.FollowersResponse)?.followers
                    } ?: emptyList()
                } else {
                    (state as? Resource.Success<*>)?.data?.let { data ->
                        (data as? com.example.jetpackdemo.data.model.FollowingResponse)?.following
                    } ?: emptyList()
                }

                if (users.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, null, tint = AppColors.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (isFollowers) "No followers yet" else "Not following anyone",
                                color = AppColors.textSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(users) { user ->
                            FollowUserCard(
                                user = user,
                                onClick = { onUserClick(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FollowUserCard(
    user: FollowUser,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (user.profileImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profileImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.username.first().uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.primary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                user.username,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}
