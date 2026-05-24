package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Doubt
import com.example.viewmodel.NextRankerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DoubtSolverScreen(
    viewModel: NextRankerViewModel,
    modifier: Modifier = Modifier
) {
    val doubtHistory by viewModel.doubtHistory.collectAsState()
    var currentSubject by remember { mutableStateOf("Physics") }
    var doubtInputText by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val subjectsList = listOf("Physics", "Chemistry", "Mathematics")

    // Automatic scroll to top when new doubt arrives
    LaunchedEffect(doubtHistory.size) {
        if (doubtHistory.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Headline Powered Banner
        AIIntroHeader()

        // Horizontal Subject Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            subjectsList.forEach { subject ->
                val isSelected = currentSubject == subject
                FilterChip(
                    selected = isSelected,
                    onClick = { currentSubject = subject },
                    label = { Text(subject) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (subject) {
                            "Physics" -> Color(0xFF1E88E5).copy(alpha = 0.2f)
                            "Chemistry" -> Color(0xFFE53935).copy(alpha = 0.2f)
                            else -> Color(0xFF43A047).copy(alpha = 0.2f)
                        },
                        selectedLabelColor = when (subject) {
                            "Physics" -> Color(0xFF1E88E5)
                            "Chemistry" -> Color(0xFFE53935)
                            else -> Color(0xFF43A047)
                        }
                    ),
                    modifier = Modifier.testTag("subject_chip_$subject")
                )
            }
        }

        // Live Doubts chat history
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            reverseLayout = true // Chat look
        ) {
            items(doubtHistory) { doubt ->
                DoubtChatBubble(doubt)
            }

            // Introduction when history is clean
            if (doubtHistory.isEmpty()) {
                item {
                    EmptyDoubtGreeting()
                }
            } else {
                item {
                    // Preset suggestions to tap
                    PresetSuggestionsScroller(
                        subject = currentSubject,
                        onSelectPreset = { preset ->
                            doubtInputText = preset
                        }
                    )
                }
            }
        }

        // Instructions indicator regarding client API configuring
        WarningBannerAPIKey()

        // Chat text entry block
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = doubtInputText,
                    onValueChange = { doubtInputText = it },
                    placeholder = { Text("Ask your $currentSubject doubt here...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("doubt_input_field"),
                    shape = RoundedCornerShape(28.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (doubtInputText.trim().isNotEmpty()) {
                                viewModel.sendDoubt(doubtInputText, currentSubject)
                                doubtInputText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    maxLines = 3
                )

                FloatingActionButton(
                    onClick = {
                        if (doubtInputText.trim().isNotEmpty()) {
                            viewModel.sendDoubt(doubtInputText, currentSubject)
                            doubtInputText = ""
                            focusManager.clearFocus()
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("doubt_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AIIntroHeader() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = "AI Doubt Solver (Doubtwala)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Powered by Google Gemini-3.5-Flash for STEM concepts",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun PresetSuggestionsScroller(
    subject: String,
    onSelectPreset: (String) -> Unit
) {
    val presets = when (subject) {
        "Physics" -> listOf(
            "Explain Newton's third law of motion",
            "What is orbital escape velocity formula?",
            "Derive range of a projectile fired at angle theta"
        )
        "Chemistry" -> listOf(
            "Why is ice floating on liquid water?",
            "Explain carbonyl in ketone functional groups",
            "What is Ideal Gas equation of state?"
        )
        else -> listOf(
            "Graph of SHM velocity versus displacement",
            "How to integrate cos(x) under substitution?",
            "Formula for gravity variation with height h"
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Frequently Checked Templates:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(presets) { template ->
                Card(
                    onClick = { onSelectPreset(template) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = template,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDoubtGreeting() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Forum,
            contentDescription = "Empty Doubts",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(72.dp)
        )

        Text(
            text = "No Active Doubts Active",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Type any complex physics equation, organic chemistry response, or math substitution problem. We'll solve it instantly with a detailed step-by-step masterclass answer.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun DoubtChatBubble(doubt: Doubt) {
    val isUser = false // doubt object splits into Question (User) and Answer (AI)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doubt_bubble_${doubt.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // User's Asked doubt message
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = doubt.question,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // AI Answer doubt message
        if (doubt.isPending) {
            // Loading skeleton
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    text = "Next Ranker Physics Expert is calculating formulas...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            doubt.answer?.let { ans ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Next Ranker Virtual Coach Solution",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Custom Simplified MD formatting parse
                        Text(
                            text = ans,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WarningBannerAPIKey() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Secure Key",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Key status: Secured via AI Studio Secrets. Fallback Simulator active if key is default.",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp
            )
        }
    }
}
