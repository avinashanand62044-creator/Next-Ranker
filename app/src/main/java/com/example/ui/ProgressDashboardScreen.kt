package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuizAttempt
import com.example.data.WpsNote
import com.example.viewmodel.NextRankerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressDashboardScreen(
    viewModel: NextRankerViewModel,
    modifier: Modifier = Modifier
) {
    val quizAttempts by viewModel.quizAttempts.collectAsState()
    val wpsNotes by viewModel.wpsNotes.collectAsState()
    val userMetrics by viewModel.userMetrics.collectAsState()

    var selectedWpsNote by remember { mutableStateOf<WpsNote?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title block
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Analytics",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "Aspirant Performance Board",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Visualize quiz graphs, syllabus metrics, and WPS study notes",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Metrics Overview Panel
        item {
            ProgressStatsOverview(attempts = quizAttempts, initialAccuracy = userMetrics.accuracyPercentage)
        }

        // Gorgeous Custom Charts Card
        item {
            PerformanceChartsCard(quizAttempts = quizAttempts)
        }

        // WPS Note Reader overlay / detail if selected
        item {
            AnimatedVisibility(
                visible = selectedWpsNote != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedWpsNote?.let { note ->
                    WpsNoteReaderView(
                        note = note,
                        onClose = { selectedWpsNote = null }
                    )
                }
            }
        }

        // WPS Library notes from faculty
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "WPS",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "WPS Study Material & DPPs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE91E63).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "WPS Editor Loaded",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (wpsNotes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No study notes written by teachers yet. Tap switch at top right to write study cards!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(wpsNotes) { note ->
                        WpsNoteItemCard(
                            note = note,
                            isSelected = selectedWpsNote?.id == note.id,
                            onClick = {
                                selectedWpsNote = if (selectedWpsNote?.id == note.id) null else note
                            }
                        )
                    }
                }
            }
        }

        // Detailed Quiz Attempts log
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Historical Practice Sheets Scorecard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (quizAttempts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AssignmentTurnedIn,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Quiz Performance Sheets Exist",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Solve questions inside the 'Quizzes' tab of the app to start tracking detailed accuracy models instantly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(quizAttempts.reversed()) { attempt ->
                PastAttemptItemRow(attempt = attempt)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProgressStatsOverview(attempts: List<QuizAttempt>, initialAccuracy: Int) {
    val totalQuizzes = attempts.size
    val correctCount = attempts.count { it.isCorrect }
    val pointsSum = attempts.sumOf { it.pointsGained }
    val calculatedAccuracy = if (totalQuizzes > 0) {
        (correctCount * 100) / totalQuizzes
    } else {
        initialAccuracy
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Total Solved", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text("$totalQuizzes Qs", fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("Acc: $calculatedAccuracy%", fontSize = 9.sp, color = if (calculatedAccuracy >= 75) Color(0xFF2E7D32) else Color(0xFFC62828))
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Solved Right", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text("$correctCount correct", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                Text("Error rate: ${100 - calculatedAccuracy}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Study XP Points", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Text("$pointsSum pts", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text("IIT-JEE goal multiplier", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun PerformanceChartsCard(quizAttempts: List<QuizAttempt>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("performance_charts_card"),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Performance Metric Curves (Progress Tracking)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Performance over time simulator: line chart plotted with dots
            Text(
                text = "Interactive Level Growth over Last Attempts (%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                // If efforts are less, display a baseline; otherwise show nice trajectory
                val dataPoints = remember(quizAttempts) {
                    if (quizAttempts.size <= 1) {
                        listOf(0.5f, 0.7f, 0.6f, 0.85f, 0.9f) // Preset illustrative timeline starting points
                    } else {
                        quizAttempts.map { if (it.isCorrect) 1f else 0f }
                            .runningFold(0.5f) { acc, correct ->
                                // running average of accuracy
                                (acc * 3f + correct) / 4f
                            }
                    }
                }

                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw grid lines
                    val stepsHorizontal = 4
                    for (i in 0..stepsHorizontal) {
                        val y = (height / stepsHorizontal) * i
                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Plot accuracy points
                    val sizePoints = dataPoints.size
                    val spacing = width / (sizePoints - 1).coerceAtLeast(1)

                    val path = Path()
                    dataPoints.forEachIndexed { idx, value ->
                        val cx = spacing * idx
                        val cy = height - (value * height)

                        if (idx == 0) {
                            path.moveTo(cx, cy)
                        } else {
                            path.lineTo(cx, cy)
                        }
                    }

                    // Stroke line
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 4f)
                    )

                    // Draw plotted dots in circles
                    dataPoints.forEachIndexed { idx, value ->
                        val cx = spacing * idx
                        val cy = height - (value * height)

                        drawCircle(
                            color = primaryColor,
                            radius = 6f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

            // Highlighting Strengths by Subject Bar Charts
            Text(
                text = "Conceptual Subject Proficiency Scale",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )

            // Calculate subject percentages
            val subjects = listOf("Physics", "Chemistry", "Mathematics", "Biology")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subjects.forEach { sub ->
                    val subAttempts = quizAttempts.filter { it.subject.equals(sub, true) }
                    val accuracy = if (subAttempts.isNotEmpty()) {
                        (subAttempts.count { it.isCorrect } * 100) / subAttempts.size
                    } else {
                        // Default estimated pre-loaded starting percentages based on PW baseline
                        when (sub) {
                            "Physics" -> 85
                            "Chemistry" -> 72
                            "Mathematics" -> 90
                            else -> 82
                        }
                    }

                    val color = when (sub) {
                        "Physics" -> Color(0xFF1E88E5)
                        "Chemistry" -> Color(0xFFE53935)
                        "Mathematics" -> Color(0xFF43A047)
                        else -> Color(0xFF8E24AA)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(90.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(accuracy / 100f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "$accuracy%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = color,
                            modifier = Modifier.width(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WpsNoteItemCard(
    note: WpsNote,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accent = when (note.subject) {
        "Physics" -> Color(0xFF1E88E5)
        "Chemistry" -> Color(0xFFE53935)
        "Mathematics" -> Color(0xFF43A047)
        else -> Color(0xFF8E24AA)
    }

    Card(
        modifier = Modifier
            .width(240.dp)
            .height(130.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accent.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(accent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = note.subject,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "doc icon",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = note.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = note.author,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun WpsNoteReaderView(
    note: WpsNote,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wps_reader_view"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "WPS Note Reader",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Note",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "By ${note.author}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "•",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Subject: ${note.subject}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Expanded Rich Formatted Note Page content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Simple Markdown-like Renderer
                    val lines = note.content.split("\n")
                    lines.forEach { line ->
                        when {
                            line.startsWith("###") -> {
                                Text(
                                    text = line.replace("###", "").trim(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            line.startsWith("-") -> {
                                BulletPointRow(text = line.substring(1).trim())
                            }
                            line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") -> {
                                Text(
                                    text = line,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            line.trim().isNotEmpty() -> {
                                Text(
                                    text = line,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BulletPointRow(text: String) {
    Row(
        modifier = Modifier.padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PastAttemptItemRow(attempt: QuizAttempt) {
    val subColor = when (attempt.subject) {
        "Physics" -> Color(0xFF1E88E5)
        "Chemistry" -> Color(0xFFE53935)
        "Mathematics" -> Color(0xFF43A047)
        else -> Color(0xFF8E24AA)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("past_attempt_row_${attempt.id}"),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status icon circular container
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (attempt.isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (attempt.isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (attempt.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(subColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = attempt.subject,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = subColor
                        )
                    }

                    Text(
                        text = if (attempt.isCorrect) "Solved Perfectly" else "Wrong Entry Resolved",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (attempt.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = attempt.questionTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Chose: ${attempt.selectedOption}  (Correct: ${attempt.correctOption})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (attempt.isCorrect) "+10 pts" else "0 pts",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (attempt.isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Checked",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
