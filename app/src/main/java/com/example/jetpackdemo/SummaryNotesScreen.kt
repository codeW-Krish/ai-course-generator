package com.example.jetpackdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jetpackdemo.data.model.GeneratedNotes
import com.example.jetpackdemo.data.model.MiniQA
import com.example.jetpackdemo.data.model.TechnicalExample
import com.example.jetpackdemo.ui.theme.AppColors
import com.example.jetpackdemo.viewmodels.NotesViewModel
import com.example.jetpackdemo.viewmodels.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryNotesScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    subtopicId: String,
    subtopicTitle: String? = null
) {
    val notesState by viewModel.notesState.collectAsState()

    Scaffold(
        containerColor = AppColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        subtopicTitle ?: "AI Notes",
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
        when (val state = notesState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Generating AI notes...", color = AppColors.textSecondary)
                        Text("This may take a moment", fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                }
            }
            is Resource.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message ?: "Failed to load notes", color = AppColors.textSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadNotes(subtopicId) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) { Text("Retry") }
                    }
                }
            }
            is Resource.Success -> {
                val notes = state.data ?: return@Scaffold
                NotesContent(notes = notes, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun NotesContent(
    notes: GeneratedNotes,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary
        if (notes.summary.isNotBlank()) {
            NotesSection(
                icon = Icons.Default.Summarize,
                title = "Summary",
                color = AppColors.primary
            ) {
                Text(notes.summary, color = AppColors.textPrimary, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }

        // The Problem
        if (notes.theProblem.isNotBlank()) {
            NotesSection(
                icon = Icons.Default.ErrorOutline,
                title = "The Problem",
                color = Color(0xFFEF4444)
            ) {
                Text(notes.theProblem, color = AppColors.textPrimary, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }

        // Previous Approaches
        if (notes.previousApproaches.isNotBlank()) {
            NotesSection(
                icon = Icons.Default.History,
                title = "Previous Approaches",
                color = Color(0xFFF59E0B)
            ) {
                Text(notes.previousApproaches, color = AppColors.textPrimary, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }

        // The Solution
        if (notes.theSolution.isNotBlank()) {
            NotesSection(
                icon = Icons.Default.Lightbulb,
                title = "The Solution",
                color = AppColors.progressGreen
            ) {
                Text(notes.theSolution, color = AppColors.textPrimary, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }

        // Key Points
        if (!notes.keyPoints.isNullOrEmpty()) {
            NotesSection(
                icon = Icons.Default.Star,
                title = "Key Points",
                color = Color(0xFF8B5CF6)
            ) {
                notes.keyPoints!!.forEachIndexed { index, point ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5CF6)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(point, color = AppColors.textPrimary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Analogy
        if (notes.analogy.isNotBlank()) {
            NotesSection(
                icon = Icons.Default.Psychology,
                title = "Analogy",
                color = Color(0xFFEC4899)
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8))
                ) {
                    Text(
                        "💡 ${notes.analogy}",
                        modifier = Modifier.padding(12.dp),
                        color = AppColors.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Real World Example
        if (notes.realWorldExample.isNotBlank()) {
            NotesSection(
                icon = Icons.Default.Public,
                title = "Real World Example",
                color = Color(0xFF06B6D4)
            ) {
                Text(notes.realWorldExample, color = AppColors.textPrimary, fontSize = 15.sp, lineHeight = 24.sp)
            }
        }

        // Technical Example
        notes.technicalExample?.let { example ->
            if (example.code.isNotBlank()) {
                NotesSection(
                    icon = Icons.Default.Code,
                    title = "Technical Example${if (!example.language.isNullOrBlank()) " (${example.language})" else ""}",
                    color = Color(0xFF10B981)
                ) {
                    TechnicalExampleView(example)
                }
            }
        }

        // Workflow
        if (!notes.workflow.isNullOrEmpty()) {
            NotesSection(
                icon = Icons.Default.AccountTree,
                title = "Workflow",
                color = Color(0xFF6366F1)
            ) {
                notes.workflow!!.forEachIndexed { index, step ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF6366F1).copy(alpha = 0.12f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${index + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                                }
                            }
                            if (index < notes.workflow!!.size - 1) {
                                Box(
                                    Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(step, color = AppColors.textPrimary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Common Mistakes
        if (!notes.commonMistakes.isNullOrEmpty()) {
            NotesSection(
                icon = Icons.Default.Warning,
                title = "Common Mistakes",
                color = Color(0xFFEF4444)
            ) {
                notes.commonMistakes!!.forEach { mistake ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("⚠️", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(mistake, color = AppColors.textPrimary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Common Confusions
        if (!notes.commonConfusions.isNullOrEmpty()) {
            NotesSection(
                icon = Icons.Default.HelpOutline,
                title = "Common Confusions",
                color = Color(0xFFF59E0B)
            ) {
                notes.commonConfusions!!.forEach { confusion ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("🤔", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(confusion, color = AppColors.textPrimary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Mini Q&A
        if (!notes.miniQa.isNullOrEmpty()) {
            NotesSection(
                icon = Icons.Default.QuestionAnswer,
                title = "Quick Q&A",
                color = AppColors.primary
            ) {
                notes.miniQa!!.forEach { qa ->
                    MiniQACard(qa)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NotesSection(
    icon: ImageVector,
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, title, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.textPrimary)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun TechnicalExampleView(example: TechnicalExample) {
    Column {
        // Code block
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(Modifier.padding(12.dp)) {
                if (!example.language.isNullOrBlank()) {
                    Text(
                        example.language,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    example.code,
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                )
            }
        }
        // Explanation
        if (example.explanation.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(example.explanation, color = AppColors.textSecondary, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun MiniQACard(qa: MiniQA) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.primary.copy(alpha = 0.04f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Q:", fontWeight = FontWeight.Bold, color = AppColors.primary, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(qa.question, color = AppColors.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AppColors.primary.copy(alpha = 0.1f))
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("A:", fontWeight = FontWeight.Bold, color = AppColors.progressGreen, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(qa.answer, color = AppColors.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    if (expanded) "Hide Answer" else "Show Answer",
                    fontSize = 12.sp,
                    color = AppColors.primary
                )
            }
        }
    }
}
