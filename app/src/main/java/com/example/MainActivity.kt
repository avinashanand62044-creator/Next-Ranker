package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BatchesScreen
import com.example.ui.DoubtSolverScreen
import com.example.ui.HomeDashboard
import com.example.ui.PracticeTestScreen
import com.example.ui.ProgressDashboardScreen
import com.example.ui.TeacherPortalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NextRankerViewModel
import com.example.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppFrame()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppFrame() {
    val viewModel: NextRankerViewModel = viewModel()
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Graduation Cap Logo",
                            tint = Color(0xFFFFC107), // Golden accent matching logo
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "NEXT RANKER",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    // Teacher Desk Switch Button
                    TextButton(
                        onClick = {
                            if (currentTab == ScreenTab.Teacher) {
                                viewModel.selectTab(ScreenTab.Home)
                            } else {
                                viewModel.selectTab(ScreenTab.Teacher)
                            }
                        },
                        modifier = Modifier.testTag("action_teacher_portal_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (currentTab == ScreenTab.Teacher) Icons.Default.School else Icons.Default.CoPresent,
                                contentDescription = "Teacher Desk Panel",
                                tint = if (currentTab == ScreenTab.Teacher) MaterialTheme.colorScheme.primary else Color(0xFFE91E63),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (currentTab == ScreenTab.Teacher) "Aspirant Mode" else "Teacher Desk",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == ScreenTab.Teacher) MaterialTheme.colorScheme.primary else Color(0xFFE91E63)
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.selectTab(ScreenTab.Practice) }) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Leaderboard",
                            tint = Color(0xFFFFB300)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.Home,
                    onClick = { viewModel.selectTab(ScreenTab.Home) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.Home) Icons.Default.GridView else Icons.Outlined.GridView,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.Batches,
                    onClick = { viewModel.selectTab(ScreenTab.Batches) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.Batches) Icons.Default.VideoLibrary else Icons.Outlined.VideoLibrary,
                            contentDescription = "Classroom"
                        )
                    },
                    label = { Text("Classes") },
                    modifier = Modifier.testTag("nav_item_batches")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.DoubtSolver,
                    onClick = { viewModel.selectTab(ScreenTab.DoubtSolver) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.DoubtSolver) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI solver"
                        )
                    },
                    label = { Text("AI doubts") },
                    modifier = Modifier.testTag("nav_item_doubts")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.Practice,
                    onClick = { viewModel.selectTab(ScreenTab.Practice) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.Practice) Icons.Default.AssignmentTurnedIn else Icons.Outlined.AssignmentTurnedIn,
                            contentDescription = "Quizzes"
                        )
                    },
                    label = { Text("Quizzes") },
                    modifier = Modifier.testTag("nav_item_practice")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.Progress,
                    onClick = { viewModel.selectTab(ScreenTab.Progress) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.Progress) Icons.Default.Analytics else Icons.Outlined.Analytics,
                            contentDescription = "Student Analytics Progress Dashboard"
                        )
                    },
                    label = { Text("Progress") },
                    modifier = Modifier.testTag("nav_item_progress")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    ScreenTab.Home -> HomeDashboard(viewModel = viewModel)
                    ScreenTab.Batches -> BatchesScreen(viewModel = viewModel)
                    ScreenTab.DoubtSolver -> DoubtSolverScreen(viewModel = viewModel)
                    ScreenTab.Practice -> PracticeTestScreen(viewModel = viewModel)
                    ScreenTab.Progress -> ProgressDashboardScreen(viewModel = viewModel)
                    ScreenTab.Teacher -> TeacherPortalScreen(viewModel = viewModel)
                }
            }
        }
    }
}
