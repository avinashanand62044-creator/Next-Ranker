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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LeaderboardUser
import com.example.data.QuizQuestion
import com.example.viewmodel.NextRankerViewModel

@Composable
fun PracticeTestScreen(
    viewModel: NextRankerViewModel,
    modifier: Modifier = Modifier
) {
    val quizQuestions by viewModel.quizQuestions.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val metrics by viewModel.userMetrics.collectAsState()

    var activeQuizIndex by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Performance Dashboard
        item {
            Spacer(modifier = Modifier.height(10.dp))
            TestAnalyticsCard(metrics)
        }

        // Active MCQ Question Card
        item {
            HeaderSection(title = "Daily Practice Quiz Series")
            Spacer(modifier = Modifier.height(4.dp))
            if (quizQuestions.isNotEmpty() && activeQuizIndex in quizQuestions.indices) {
                val activeQuestion = quizQuestions[activeQuizIndex]
                InteractiveQACard(
                    question = activeQuestion,
                    onOptionSelect = { idx ->
                        viewModel.answerQuizQuestion(activeQuestion.id, idx)
                    },
                    onNext = {
                        if (activeQuizIndex < quizQuestions.size - 1) {
                            activeQuizIndex++
                        } else {
                            activeQuizIndex = 0 // loop
                        }
                    },
                    onReset = {
                        viewModel.resetQuiz()
                        activeQuizIndex = 0
                    },
                    currentIndex = activeQuizIndex,
                    totalQuestions = quizQuestions.size
                )
            }
        }

        // Gamified Leaderboard Card
        item {
            HeaderSection(title = "All India Student Leaderboard")
            Spacer(modifier = Modifier.height(6.dp))
            LeaderboardTrackCard(leaderboard)
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun HeaderSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun TestAnalyticsCard(metrics: com.example.data.UserMetrics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("test_analytics_card"),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aspirant Performance Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "National Rating: Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // estimated rank column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estimated AIR",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "#${metrics.testRank}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Next Target: < #100",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Accuracy column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quiz Accuracy",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${metrics.accuracyPercentage}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "High Consistency!",
                        fontSize = 9.sp,
                        color = Color(0xFF2E7D32).copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveQACard(
    question: QuizQuestion,
    onOptionSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    currentIndex: Int,
    totalQuestions: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_qa_card"),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: question counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUESTION ${currentIndex + 1} OF $totalQuestions",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp
                )

                // Streak crown decoration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "+10 points",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                }
            }

            // Question Statement
            Text(
                text = question.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Multiple choice options
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                question.options.forEachIndexed { index, option ->
                    val isUserSelected = question.userSelectedIndex == index
                    val isCorrect = question.correctIndex == index
                    val hasAnswered = question.userSelectedIndex != null

                    val itemColor = when {
                        hasAnswered && isCorrect -> Color(0xFFE8F5E9)                     // highlight correct in green
                        hasAnswered && isUserSelected && !isCorrect -> Color(0xFFFFEBEE)   // highlight wrong selection in red
                        isUserSelected -> MaterialTheme.colorScheme.primaryContainer       // generic selected
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val borderColor = when {
                        hasAnswered && isCorrect -> Color(0xFF2E7D32)
                        hasAnswered && isUserSelected && !isCorrect -> Color(0xFFC62828)
                        isUserSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(itemColor)
                            .border(1.2.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = !hasAnswered) { onOptionSelect(index) }
                            .padding(14.dp)
                            .testTag("qa_option_$index")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Letter bubble (A, B, C, D)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            hasAnswered && isCorrect -> Color(0xFF2E7D32)
                                            hasAnswered && isUserSelected && !isCorrect -> Color(0xFFC62828)
                                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + index).toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasAnswered && (isCorrect || (isUserSelected && !isCorrect))) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // Status Icons
                            if (hasAnswered) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, "Correct", tint = Color(0xFF2E7D32))
                                } else if (isUserSelected) {
                                    Icon(Icons.Default.Cancel, "Wrong", tint = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }
            }

            // Expandable textbook explanation
            AnimatedVisibility(
                visible = question.userSelectedIndex != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Next Ranker Explanation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = question.explanation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.outline)
                ) {
                    Text("Reset Quizzes")
                }

                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("next_quiz_button")
                ) {
                    Text(
                        text = if (currentIndex < totalQuestions - 1) "Next Question" else "Loop Back"
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardTrackCard(leaderboard: List<com.example.data.LeaderboardUser>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leader_board_card"),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leaderboard.forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (user.isSelf) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Position rank
                    Text(
                        text = "#${user.rank}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = when (user.rank) {
                            1 -> Color(0xFFFFB300)
                            2 -> Color(0xFF78909C)
                            3 -> Color(0xFF8D6E63)
                            else -> MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.width(36.dp)
                    )

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (user.isSelf) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.substring(0, 1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (user.isSelf) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name
                    Text(
                        text = user.name,
                        fontSize = 13.sp,
                        fontWeight = if (user.isSelf) FontWeight.ExtraBold else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Score
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${user.score} pts",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${user.accuracy}% accurate",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
