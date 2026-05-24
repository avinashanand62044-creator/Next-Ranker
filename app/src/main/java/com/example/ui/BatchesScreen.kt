package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Batch
import com.example.data.Lecture
import com.example.viewmodel.NextRankerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchesScreen(
    viewModel: NextRankerViewModel,
    modifier: Modifier = Modifier
) {
    val batches by viewModel.batches.collectAsState()
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val lecturesMap by viewModel.lectures.collectAsState()
    val activeLecture by viewModel.activeLecture.collectAsState()

    var showDppSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Horizontal Batch Selector Tabs
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            batches.forEach { batch ->
                val isSelected = selectedBatch?.id == batch.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectBatchDetail(batch) },
                    label = { Text(batch.title.substringBefore(" ")) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.testTag("batch_chip_${batch.id}")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedBatch?.let { batch ->
                // Batch Summary Header Card
                item {
                    BatchSubHeaderCard(
                        batch = batch,
                        onEnroll = { viewModel.enrollBatch(batch.id) }
                    )
                }

                if (batch.isEnrolled) {
                    val lectures = lecturesMap[batch.id] ?: emptyList()

                    // Simulated Live Video Classroom Player
                    item {
                        Text(
                            text = "Now Streaming Interactive Lecture",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MockVideoClassroomPlayer(
                            lecture = activeLecture ?: lectures.firstOrNull(),
                            onToggleWatched = { lecture ->
                                viewModel.setWatched(lecture.id, batch.id)
                            },
                            onOpenDpp = { showDppSheet = true }
                        )
                    }

                    // Syllabus Lecture List
                    item {
                        Text(
                            text = "Course Curriculum (${lectures.size} lectures)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(lectures) { lecture ->
                        val isCurrentActive = activeLecture?.id == lecture.id
                        LectureCurriculumRow(
                            lecture = lecture,
                            isActive = isCurrentActive,
                            onClick = { viewModel.selectLecture(lecture) }
                        )
                    }
                } else {
                    // Non-enrolled Empty Screen Layout
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Lock",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Classroom Locked",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Join this batch to access high-definition live lectures, homework PDF folders, daily testing, and expert doubt solvers.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.enrollBatch(batch.id) },
                                    modifier = Modifier.testTag("enroll_batch_classroom_button")
                                ) {
                                    Text("Enroll in Class Free Trial")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Sheet or Drawer representing DPP contents
    if (showDppSheet) {
        AlertDialog(
            onDismissRequest = { showDppSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Daily Practice Problems (DPP)")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Batch: " + (selectedBatch?.title ?: ""),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Topic: " + (activeLecture?.title ?: "Class 01 Intro"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "🔥 Homework Assignment:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "1. If a projectile's maximum range is 40m, what is its maximum coordinate height?\n" +
                                        "2. Derive v^2 = u^2 + 2as using vector integral equations.\n" +
                                        "3. Solve Chapter MCQ exercises 12 through 20.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDppSheet = false }) {
                    Text("Complete DPP")
                }
            }
        )
    }
}

@Composable
fun BatchSubHeaderCard(
    batch: Batch,
    onEnroll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("batch_subheader_card"),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = batch.targetExam,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text(
                        text = "${batch.rating} (5.2k Votes)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = batch.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )

            Text(
                text = batch.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Lead Professor", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text(batch.facultyName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                if (!batch.isEnrolled) {
                    Button(
                        onClick = onEnroll,
                        modifier = Modifier.testTag("enroll_now_button_subheader")
                    ) {
                        Text("Add to Class for ${batch.price}")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Enrolled", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        Text("Class Admitted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
fun MockVideoClassroomPlayer(
    lecture: Lecture?,
    onToggleWatched: (Lecture) -> Unit,
    onOpenDpp: () -> Unit
) {
    if (lecture == null) return

    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0.45f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mock_video_player"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column {
            // Screen box representing active stream
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF151922)),
                contentAlignment = Alignment.Center
            ) {
                // Interactive video details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                        contentDescription = "Play/Pause",
                        tint = if (isPlaying) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier
                            .size(64.dp)
                            .clickable { isPlaying = !isPlaying }
                    )

                    Text(
                        text = if (isPlaying) "Streaming Class: ${lecture.title}" else "Video Class Paused",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Next Ranker Stream CDN Server 1 (HLS)",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }

                // Streaming label
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPlaying) Color.Red else Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPlaying) "1080p LIVE" else "READY",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Player bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "00:45:12",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Slider(
                        value = currentProgress,
                        onValueChange = { currentProgress = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = lecture.duration,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { isPlaying = !isPlaying }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = onOpenDpp) {
                            Icon(Icons.Default.Description, contentDescription = "Homework")
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onToggleWatched(lecture) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (lecture.isWatched) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("mark_watched_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = if (lecture.isWatched) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(if (lecture.isWatched) "Class Attended ✓" else "Mark as Attended")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LectureCurriculumRow(
    lecture: Lecture,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("lecture_row_${lecture.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (lecture.isWatched) Color(0xFFE8F5E9)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (lecture.isWatched) Icons.Default.Check else Icons.Default.OndemandVideo,
                    contentDescription = null,
                    tint = if (lecture.isWatched) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lecture.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${lecture.subject} • ${lecture.duration} • ${lecture.dppFile}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isActive) {
                Text(
                    text = "Playing",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
