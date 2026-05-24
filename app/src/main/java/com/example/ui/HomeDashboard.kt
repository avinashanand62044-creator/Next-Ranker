package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Batch
import com.example.data.Schedule
import com.example.data.UserMetrics
import com.example.viewmodel.NextRankerViewModel
import com.example.viewmodel.ScreenTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard(
    viewModel: NextRankerViewModel,
    modifier: Modifier = Modifier
) {
    val userMetrics by viewModel.userMetrics.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val batches by viewModel.batches.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Core Hero Welcome Banner
        item {
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeHeroBanner(userMetrics)
        }

        // Daily Tracker Badges (Accuracy, Streaks)
        item {
            MetricsOverviewGrid(userMetrics)
        }

        // Action Quick Access
        item {
            QuickActionsRow(onNavigate = { viewModel.selectTab(it) })
        }

        // Trending Masterclasses
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Batches & Classes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                TextButton(
                    onClick = { viewModel.selectTab(ScreenTab.Batches) },
                    modifier = Modifier.testTag("all_batches_button")
                ) {
                    Text("View All", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(batches) { batch ->
                    BatchHeroCard(
                        batch = batch,
                        onClick = {
                            viewModel.selectBatchDetail(batch)
                            viewModel.selectTab(ScreenTab.Batches)
                        }
                    )
                }
            }
        }

        // Today's Live/Upcoming Timeline
        item {
            Text(
                text = "Today's Live & Upcoming classes",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(schedules.filter { it.date == "Today" }) { schedule ->
            ScheduleRowItem(
                schedule = schedule,
                onJoin = {
                    val target = batches.firstOrNull { it.title.contains("Alpha") }
                    viewModel.selectBatchDetail(target)
                    viewModel.selectTab(ScreenTab.Batches)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Next Ranker Elite Faculty team
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Our National Star Faculty",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            FacultyTeamGrid()
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun WelcomeHeroBanner(metrics: UserMetrics) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("welcome_hero_banner"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Rank Icon",
                        tint = Color.White
                    )
                    Text(
                        text = "NEXT RANKER ACADEMY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        letterSpacing = 1.5.sp
                    )
                }

                Text(
                    text = "Empowering National AIR Ranks!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Welcome back, Ranker Aspirant. You are in the top 5% streak this week! Target: JEE/NEET perfection.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = "Streak Icon",
                        tint = Color.Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Study Streak: ${metrics.streakDays} Days 🔥",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MetricsOverviewGrid(metrics: UserMetrics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Solved card
        MetricMiniCard(
            title = "Solved Doubts",
            value = metrics.solvedDoubts.toString(),
            detail = "AI Assistant solver",
            icon = Icons.Outlined.Lightbulb,
            accentColor = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )

        // Class stats card
        MetricMiniCard(
            title = "Lectures Watched",
            value = metrics.completedLectures.toString(),
            detail = "Video records",
            icon = Icons.Outlined.PlayCircle,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        // Score card
        MetricMiniCard(
            title = "Quiz Practice",
            value = "${metrics.accuracyPercentage}%",
            detail = "Est. accuracy",
            icon = Icons.Outlined.AssignmentTurnedIn,
            accentColor = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    detail: String,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detail,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(onNavigate: (ScreenTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_actions_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            label = "Ask Doubts",
            icon = Icons.Default.ChatBubbleOutline,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = { onNavigate(ScreenTab.DoubtSolver) },
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            label = "Mock Exams",
            icon = Icons.Default.Quiz,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = { onNavigate(ScreenTab.Practice) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = contentColor),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier.height(60.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun BatchHeroCard(
    batch: Batch,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .testTag("batch_hero_card_${batch.title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = batch.targetExam,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = batch.rating.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = batch.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = batch.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fees Structure",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = batch.price,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (batch.isEnrolled) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.primary
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (batch.isEnrolled) "Enrolled ✓" else "Join Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (batch.isEnrolled) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleRowItem(
    schedule: Schedule,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${schedule.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isLive) MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lecture Subject indicator
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (schedule.subject) {
                            "Physics" -> Color(0xFF1E88E5).copy(alpha = 0.15f)
                            "Chemistry" -> Color(0xFFE53935).copy(alpha = 0.15f)
                            "Mathematics" -> Color(0xFF43A047).copy(alpha = 0.15f)
                            else -> Color(0xFF8E24AA).copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = schedule.subject.substring(0, 3).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (schedule.subject) {
                        "Physics" -> Color(0xFF1E88E5)
                        "Chemistry" -> Color(0xFFE53935)
                        "Mathematics" -> Color(0xFF43A047)
                        else -> Color(0xFF8E24AA)
                    }
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = schedule.topic,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (schedule.isLive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Text(
                    text = "${schedule.instructor} • ${schedule.time}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (schedule.isLive) Icons.Default.PlayArrow else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (schedule.isLive) Color.Red else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = schedule.isLive, onClick = onJoin)
            )
        }
    }
}

@Composable
fun FacultyTeamGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FacultyAvatarCard(
            name = "Prof. Pandey",
            role = "Physics HOD",
            exRank = "Ex-IITian Kanpur",
            avatarColor = Color(0xFFE3F2FD),
            textColor = Color(0xFF0D47A1),
            modifier = Modifier.weight(1f)
        )

        FacultyAvatarCard(
            name = "Dr. Roy",
            role = "Chemistry Head",
            exRank = "Ph.D. Org Chemistry",
            avatarColor = Color(0xFFFFEBEE),
            textColor = Color(0xFFB71C1C),
            modifier = Modifier.weight(1f)
        )

        FacultyAvatarCard(
            name = "Prof. Verma",
            role = "Math Innovator",
            exRank = "JEE AIR 25 Coached",
            avatarColor = Color(0xFFE8F5E9),
            textColor = Color(0xFF1B5E20),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FacultyAvatarCard(
    name: String,
    role: String,
    exRank: String,
    avatarColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.substringAfter(" ").substring(0,1),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = role,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(textColor.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = exRank,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}
