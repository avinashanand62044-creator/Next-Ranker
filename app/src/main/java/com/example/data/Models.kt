package com.example.data

import java.util.UUID

data class Batch(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetExam: String, // e.g., "JEE Advanced", "NEET-UG", "IIT-JEE 2027"
    val facultyName: String,
    val price: String = "Free",
    val rating: Float = 4.9f,
    val lecturesCount: Int = 45,
    val isEnrolled: Boolean = false,
    val dppsCount: Int = 30
)

data class Lecture(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val duration: String,
    val subject: String,
    val dppFile: String = "Kinematics_DPP_01.pdf",
    val isWatched: Boolean = false
)

data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    val subject: String, // "Physics", "Chemistry", "Mathematics", "Biology"
    val topic: String,
    val instructor: String,
    val time: String, // "10:30 AM - 12:00 PM"
    val date: String, // "Today", "Tomorrow"
    val isLive: Boolean = false
)

data class Doubt(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = true,
    val isPending: Boolean = false,
    val subject: String? = "Physics"
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val userSelectedIndex: Int? = null,
    val explanation: String
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val score: Int,
    val accuracy: Int,
    val isSelf: Boolean = false
)

data class UserMetrics(
    val solvedDoubts: Int = 0,
    val completedLectures: Int = 0,
    val testRank: Int = 125,
    val accuracyPercentage: Int = 84,
    val streakDays: Int = 3
)

data class QuizAttempt(
    val id: String = UUID.randomUUID().toString(),
    val questionTitle: String,
    val selectedOption: String,
    val correctOption: String,
    val isCorrect: Boolean,
    val pointsGained: Int,
    val subject: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WpsNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val author: String = "Prof. Alok Pandey",
    val subject: String = "Physics",
    val timestamp: Long = System.currentTimeMillis()
)
