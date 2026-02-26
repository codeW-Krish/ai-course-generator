package com.example.jetpackdemo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackdemo.data.model.Course
import com.example.jetpackdemo.ui.theme.AppColors

@Composable
fun PremiumCourseCard(
    course: Course,
    onCardClick: () -> Unit,
    onJoinClick: () -> Unit,
    isEnrolling: Boolean = false,
    onCreatorClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header row: difficulty badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                course.difficulty?.let { diff ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (diff.lowercase()) {
                            "beginner" -> Color(0xFF10B981).copy(alpha = 0.12f)
                            "intermediate" -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                            "advanced" -> Color(0xFFEF4444).copy(alpha = 0.12f)
                            else -> AppColors.primary.copy(alpha = 0.12f)
                        }
                    ) {
                        Text(
                            diff.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = when (diff.lowercase()) {
                                "beginner" -> Color(0xFF10B981)
                                "intermediate" -> Color(0xFFF59E0B)
                                "advanced" -> Color(0xFFEF4444)
                                else -> AppColors.primary
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (course.totalUsersJoined > 0) {
                    Text(
                        "${course.totalUsersJoined} enrolled",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                course.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                course.description ?: "No description available",
                fontSize = 13.sp,
                color = AppColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            // Creator row
            course.creatorName?.let { name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (onCreatorClick != null && course.createdBy != null)
                                Modifier.clickable { onCreatorClick(course.createdBy!!) }
                            else Modifier
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = AppColors.primary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        name,
                        fontSize = 12.sp,
                        color = AppColors.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = onJoinClick,
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                contentPadding = PaddingValues(horizontal = 12.dp),
                enabled = !isEnrolling
            ) {
                if (isEnrolling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AppColors.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Enrolling...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Join Course", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
