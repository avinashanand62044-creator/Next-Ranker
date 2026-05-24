package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.NextRankerViewModel
import com.example.viewmodel.ScreenTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherPortalScreen(
    viewModel: NextRankerViewModel,
    modifier: Modifier = Modifier
) {
    val batches by viewModel.batches.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    var activeCreatorTab by remember { mutableStateOf("Quiz") } // "Quiz", "Live", "Wps", "DPP"
    val creatorTabs = listOf("Quiz" to Icons.Default.Quiz, "Live" to Icons.Default.CoPresent, "Wps" to Icons.Default.NoteAlt, "DPP" to Icons.Default.LibraryBooks)

    // Form inputs state
    // Quiz Form
    var quizQuestionText by remember { mutableStateOf("") }
    var quizOptA by remember { mutableStateOf("") }
    var quizOptB by remember { mutableStateOf("") }
    var quizOptC by remember { mutableStateOf("") }
    var quizOptD by remember { mutableStateOf("") }
    var quizCorrectOptionIdx by remember { mutableStateOf(0) }
    var quizExplanationText by remember { mutableStateOf("") }

    // Live Class Form
    var liveSubject by remember { mutableStateOf("Physics") }
    var liveTopic by remember { mutableStateOf("") }
    var liveInstructor by remember { mutableStateOf("Prof. Alok Pandey") }
    var liveTime by remember { mutableStateOf("04:30 PM - 06:00 PM") }
    var liveDate by remember { mutableStateOf("Today") }
    var isLiveNow by remember { mutableStateOf(true) }

    // WPS Note Form
    var wpsTitle by remember { mutableStateOf("") }
    var wpsSubject by remember { mutableStateOf("Physics") }
    var wpsAuthor by remember { mutableStateOf("Prof. Alok Pandey") }
    var wpsContentText by remember { mutableStateOf("") }

    // Lecture/DPP Form
    var dppBatchSelected by remember { mutableStateOf(batches.firstOrNull()?.id ?: "") }
    var dppLectTitle by remember { mutableStateOf("") }
    var dppSubject by remember { mutableStateOf("Physics") }
    var dppDuration by remember { mutableStateOf("1h 40m") }
    var dppFileName by remember { mutableStateOf("DPP_Practice_Sheet_01.pdf") }

    var formMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Hero Title
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupervisorAccount,
                            contentDescription = "Teacher Port",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Next Ranker National Teacher Desk",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Instantly design classes, write study materials, and build quizzes.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Segmented Creator selector Row
        item {
            Text(
                text = "Select Workspace Utility",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                creatorTabs.forEach { (tab, icon) ->
                    val isSelected = activeCreatorTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                            )
                            .clickable {
                                activeCreatorTab = tab
                                formMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = when (tab) {
                                    "Quiz" -> "Add Test"
                                    "Live" -> "Add Timetable"
                                    "Wps" -> "WPS Notes"
                                    else -> "Lect & DPP"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Form Success alerts
        if (formMessage.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF2E7D32)
                        )
                        Text(
                            text = formMessage,
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Workspace Forms
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_creation_form_card"),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeCreatorTab) {
                        "Quiz" -> {
                            Text("Form: Add Daily Practice MCQ", fontWeight = FontWeight.Black, fontSize = 14.sp)

                            OutlinedTextField(
                                value = quizQuestionText,
                                onValueChange = { quizQuestionText = it },
                                label = { Text("Question Formulation") },
                                placeholder = { Text("e.g. Find net force acting on a block...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_quiz_question"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = quizOptA,
                                onValueChange = { quizOptA = it },
                                label = { Text("Option A") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = quizOptB,
                                onValueChange = { quizOptB = it },
                                label = { Text("Option B") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = quizOptC,
                                onValueChange = { quizOptC = it },
                                label = { Text("Option C") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = quizOptD,
                                onValueChange = { quizOptD = it },
                                label = { Text("Option D") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Text(
                                text = "Correct Answer option Index:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("A" to 0, "B" to 1, "C" to 2, "D" to 3).forEach { (label, idx) ->
                                    val isSelected = quizCorrectOptionIdx == idx
                                    Button(
                                        onClick = { quizCorrectOptionIdx = idx },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(label)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = quizExplanationText,
                                onValueChange = { quizExplanationText = it },
                                label = { Text("Next Ranker Step-by-Step Explanation") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 4
                            )

                            Button(
                                onClick = {
                                    if (quizQuestionText.isNotBlank() && quizOptA.isNotBlank() && quizOptB.isNotBlank()) {
                                        val newQuestion = QuizQuestion(
                                            id = viewModel.quizQuestions.value.size + 1,
                                            question = quizQuestionText,
                                            options = listOf(quizOptA, quizOptB, quizOptC.ifBlank { "N/A" }, quizOptD.ifBlank { "N/A" }),
                                            correctIndex = quizCorrectOptionIdx,
                                            explanation = quizExplanationText.ifBlank { "Standard textbook derivative solution follows." }
                                        )
                                        viewModel.addQuizQuestion(newQuestion)
                                        formMessage = "New Practice MCQ saved under Quizzes tab!"
                                        // clear inputs
                                        quizQuestionText = ""
                                        quizOptA = ""
                                        quizOptB = ""
                                        quizOptC = ""
                                        quizOptD = ""
                                        quizExplanationText = ""
                                        focusManager.clearFocus()
                                    } else {
                                        formMessage = "Error: Question text and binary options required!"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_quiz_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Publish Test MCQ to Aspirants")
                            }
                        }

                        "Live" -> {
                            Text("Form: Add Timetable Schedule", fontWeight = FontWeight.Black, fontSize = 14.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Physics", "Chemistry", "Mathematics", "Biology").forEach { sub ->
                                    val isSelected = liveSubject == sub
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { liveSubject = sub },
                                        label = { Text(sub, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = liveTopic,
                                onValueChange = { liveTopic = it },
                                label = { Text("Topic Headline") },
                                placeholder = { Text("e.g. Rotational Inertia Calculations") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_live_topic"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = liveInstructor,
                                onValueChange = { liveInstructor = it },
                                label = { Text("Star Host Instructor Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = liveTime,
                                onValueChange = { liveTime = it },
                                label = { Text("Class Hours Timing") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Live broadcast immediately:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = isLiveNow,
                                    onCheckedChange = { isLiveNow = it }
                                )
                            }

                            Button(
                                onClick = {
                                    if (liveTopic.isNotBlank()) {
                                        val sched = Schedule(
                                            subject = liveSubject,
                                            topic = liveTopic,
                                            instructor = liveInstructor,
                                            time = liveTime,
                                            date = liveDate,
                                            isLive = isLiveNow
                                        )
                                        viewModel.addSchedule(sched)
                                        formMessage = "Live timetable schedule broadcast active!"
                                        liveTopic = ""
                                        focusManager.clearFocus()
                                    } else {
                                        formMessage = "Error: Please check topic headline value!"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_schedule_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Publish Dynamic Lecture to Timetable")
                            }
                        }

                        "Wps" -> {
                            Text("Workspace: Next WPS Notes Creator", fontWeight = FontWeight.Black, fontSize = 14.sp)

                            OutlinedTextField(
                                value = wpsTitle,
                                onValueChange = { wpsTitle = it },
                                label = { Text("Document Study-Note Title") },
                                placeholder = { Text("e.g. Kinematics Revision sheet") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_wps_title"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Physics", "Chemistry", "Mathematics", "Biology").forEach { sub ->
                                    val isSelected = wpsSubject == sub
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { wpsSubject = sub },
                                        label = { Text(sub, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = wpsAuthor,
                                onValueChange = { wpsAuthor = it },
                                label = { Text("Faculty/Author Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = wpsContentText,
                                onValueChange = { wpsContentText = it },
                                label = { Text("WPS Note Editor Content (Use '###' for Headers, '-' for bullet lines)") },
                                placeholder = { Text("### Projectiles\n- T = 2usinθ/g\n### Advanced Tips:\n1. Solve limits...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 12
                            )

                            Button(
                                onClick = {
                                    if (wpsTitle.isNotBlank() && wpsContentText.isNotBlank()) {
                                        val newNote = WpsNote(
                                            title = wpsTitle,
                                            subject = wpsSubject,
                                            author = wpsAuthor,
                                            content = wpsContentText
                                        )
                                        viewModel.addWpsNote(newNote)
                                        formMessage = "WPS document successfully published to Library & WPS shelf!"
                                        wpsTitle = ""
                                        wpsContentText = ""
                                        focusManager.clearFocus()
                                    } else {
                                        formMessage = "Error: Input title and content text!"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_wps_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Publish Document to student Library Shelf")
                            }
                        }

                        else -> {
                            Text("Form: Add Video Lecture with DPP PDF", fontWeight = FontWeight.Black, fontSize = 14.sp)

                            Text("Select Batch Target Course", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                batches.forEach { b ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (dppBatchSelected == b.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else Color.Transparent
                                            )
                                            .clickable { dppBatchSelected = b.id }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = dppBatchSelected == b.id,
                                            onClick = { dppBatchSelected = b.id }
                                        )
                                        Text(b.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = dppLectTitle,
                                onValueChange = { dppLectTitle = it },
                                label = { Text("Lecture Class Video Title") },
                                placeholder = { Text("e.g. Rotation Part 4: Moment of Inertia") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_lect_title"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Physics", "Chemistry", "Mathematics", "Biology").forEach { sub ->
                                    val isSelected = dppSubject == sub
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { dppSubject = sub },
                                        label = { Text(sub, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = dppDuration,
                                onValueChange = { dppDuration = it },
                                label = { Text("Video play duration") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = dppFileName,
                                onValueChange = { dppFileName = it },
                                label = { Text("Associated DPP File Name (PDF)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = {
                                    if (dppLectTitle.isNotBlank() && dppBatchSelected.isNotEmpty()) {
                                        val newLecture = Lecture(
                                            title = dppLectTitle,
                                            duration = dppDuration,
                                            subject = dppSubject,
                                            dppFile = dppFileName,
                                            isWatched = false
                                        )
                                        viewModel.addLecture(dppBatchSelected, newLecture)
                                        formMessage = "Lecture & Daily Practice Paper (DPP) attached to Course!"
                                        dppLectTitle = ""
                                        focusManager.clearFocus()
                                    } else {
                                        formMessage = "Error: Input title and select course batch!"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_dpp_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Publish Lecture and Attach DPP Sheet")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "info",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Broadcast Mode: Real-time. Added items are instantly integrated in student study portals locally.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
