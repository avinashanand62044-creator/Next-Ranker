package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScreenTab {
    Home,
    Batches,
    DoubtSolver,
    Practice,
    Progress,
    Teacher
}

class NextRankerViewModel(application: Application) : AndroidViewModel(application) {

    // Bottom Tab State
    private val _currentTab = MutableStateFlow(ScreenTab.Home)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Enrolled Courses Track
    private val _batches = MutableStateFlow<List<Batch>>(emptyList())
    val batches: StateFlow<List<Batch>> = _batches.asStateFlow()

    // Lectures
    private val _lectures = MutableStateFlow<Map<String, List<Lecture>>>(emptyMap())
    val lectures: StateFlow<Map<String, List<Lecture>>> = _lectures.asStateFlow()

    // Selected Lecture for Demo Player
    private val _activeLecture = MutableStateFlow<Lecture?>(null)
    val activeLecture: StateFlow<Lecture?> = _activeLecture.asStateFlow()

    // Doubts
    private val _doubtHistory = MutableStateFlow<List<Doubt>>(emptyList())
    val doubtHistory: StateFlow<List<Doubt>> = _doubtHistory.asStateFlow()

    // Quizzes (Mock series)
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    // Quiz Attempt Logs (For student progress & analytics graph)
    private val _quizAttempts = MutableStateFlow<List<QuizAttempt>>(emptyList())
    val quizAttempts: StateFlow<List<QuizAttempt>> = _quizAttempts.asStateFlow()

    // WPS Notes written by teachers
    private val _wpsNotes = MutableStateFlow<List<WpsNote>>(emptyList())
    val wpsNotes: StateFlow<List<WpsNote>> = _wpsNotes.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardUser>> = _leaderboard.asStateFlow()

    // Metrics
    private val _userMetrics = MutableStateFlow(UserMetrics())
    val userMetrics: StateFlow<UserMetrics> = _userMetrics.asStateFlow()

    // Daily Schedule Items
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    // Currently selected batch detail
    private val _selectedBatch = MutableStateFlow<Batch?>(null)
    val selectedBatch: StateFlow<Batch?> = _selectedBatch.asStateFlow()

    init {
        loadInitialMockData()
    }

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun enrollBatch(batchId: String) {
        _batches.value = _batches.value.map {
            if (it.id == batchId) {
                _selectedBatch.value = it.copy(isEnrolled = true)
                it.copy(isEnrolled = true)
            } else it
        }
        val enrolledCount = _batches.value.count { it.isEnrolled }
        _userMetrics.value = _userMetrics.value.copy(completedLectures = enrolledCount * 2)
    }

    fun selectBatchDetail(batch: Batch?) {
        _selectedBatch.value = batch
    }

    fun setWatched(lectureId: String, batchId: String) {
        val currentLectures = _lectures.value[batchId] ?: return
        val updated = currentLectures.map {
            if (it.id == lectureId) {
                if (!it.isWatched) {
                    _userMetrics.value = _userMetrics.value.copy(
                        completedLectures = _userMetrics.value.completedLectures + 1
                    )
                }
                _activeLecture.value = it.copy(isWatched = true)
                it.copy(isWatched = true)
            } else it
        }
        _lectures.value = _lectures.value.toMutableMap().apply {
            put(batchId, updated)
        }
    }

    fun selectLecture(lecture: Lecture?) {
        _activeLecture.value = lecture
    }

    // AI Doubt solver action
    fun sendDoubt(rawQuestion: String, subject: String = "Physics") {
        if (rawQuestion.trim().isEmpty()) return

        // 1. Add pending doubt to list
        val pendingId = java.util.UUID.randomUUID().toString()
        val userDoubt = Doubt(
            id = pendingId,
            question = rawQuestion,
            answer = null,
            isAiGenerated = true,
            isPending = true,
            subject = subject
        )
        _doubtHistory.value = listOf(userDoubt) + _doubtHistory.value

        _userMetrics.value = _userMetrics.value.copy(
            solvedDoubts = _userMetrics.value.solvedDoubts + 1
        )

        // 2. Perform API call
        viewModelScope.launch {
            val answer = queryGeminiModel(rawQuestion, subject)

            _doubtHistory.value = _doubtHistory.value.map {
                if (it.id == pendingId) {
                    it.copy(answer = answer, isPending = false)
                } else it
            }
        }
    }

    private suspend fun queryGeminiModel(question: String, subject: String): String = withContext(Dispatchers.IO) {
        // Safe access to API Key via reflection/BuildConfig
        val apiKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Standard prompt preparation
        val systemPrompt = "You are 'Next Ranker' AI Virtual Teacher, an elite, energetic, and highly analytical physics, chemistry, and math tutor " +
                "specializing in IIT-JEE and NEET-UG coaching. Provide beautifully clear, mathematically sound, " +
                "and conceptual answers. Use rich bullet points, headers, and step-by-step formulas where needed."

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Emulate high quality science response for instant local demo
            return@withContext getTeacherEmulatedResponse(question, subject)
        }

        try {
            val contentList = listOf(
                ValueContent(
                    parts = listOf(Part(text = question))
                )
            )
            val request = GenerateContentRequest(
                contents = contentList,
                systemInstruction = SystemInstruction(parts = listOf(Part(text = systemPrompt)))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, but I couldn't formulate an answer. Let me re-verify the conceptual parameters."
        } catch (e: Exception) {
            getTeacherEmulatedResponse(question, subject) + "\n\n*(Visualized Simulator Mode triggered)*"
        }
    }

    private fun getTeacherEmulatedResponse(question: String, subject: String): String {
        val cleanQ = question.lowercase()
        return when {
            cleanQ.contains("newton") || cleanQ.contains("force") || cleanQ.contains("law") -> {
                "### 🎯 Newton's Laws of Motion Analysis (Next Ranker Masterclass)\n\n" +
                        "Let's break down Newton's laws systematically:\n\n" +
                        "1. **First Law (Inertia):** A body remains at rest or uniform motion unless acted on by an external force (F_net = 0, so v = constant).\n" +
                        "2. **Second Law (Core Dynamics):** The rate of change of momentum is directly proportional to applied force:\n" +
                        "   F = m * a (constant mass)\n" +
                        "3. **Third Law (Action-Reaction):** Forces occur in matched pairs (F_AB = -F_BA). Important: These act on different bodies!\n\n" +
                        "**🔥 Ranker Tip:** When solving 3-body pulley problems, write Newton's equations (F - T = ma) for each block independently, then solve simultaneously."
            }
            cleanQ.contains("gravity") || cleanQ.contains("g ") || cleanQ.contains("orbit") -> {
                "### 🌌 Gravitational Field & Mechanics (Next Ranker Series)\n\n" +
                        "Gravitational interaction is governed by Newton's Universal Law:\n" +
                        "F = G * m1 * m2 / r^2\n\n" +
                        "*   **Acceleration due to Gravity (g):** On Earth's surface, g = G * M / R^2 approx 9.8 m/s^2.\n" +
                        "*   **Variation with Altitude (h):** g_altitude = g * (1 - 2h/R) for h << R.\n" +
                        "*   **Escape Velocity:** Min speed to escape field: V_escape = sqrt(2GM/R) approx 11.2 km/s\n\n" +
                        "What this means is that gravity decreases as you rise or dig down. Do you have a specific numerical on satellite orbits to solve?"
            }
            cleanQ.contains("light") || cleanQ.contains("lens") || cleanQ.contains("refract") -> {
                "### ⚡ Ray Optics & Wave Mechanics (IIT-JEE Peak Topic)\n\n" +
                        "Optics has high weightage. Here are the core relationships:\n\n" +
                        "1. **Snell's Law:** mu1 * sin(i) = mu2 * sin(r).\n" +
                        "2. **Lens Maker's Formula:**\n" +
                        "   1/f = (mu - 1) * (1/R1 - 1/R2)\n" +
                        "3. **Apparent Depth:** d_apparent = d / mu (when view normal from rarer medium).\n\n" +
                        "**⚡ High-Scoring Alert:** Total Internal Reflection (TIR) occurs when angle i > critical_angle where sin(critical_angle) = 1/mu."
            }
            else -> {
                "### 🧬 Next Ranker Academic Solution\n\n" +
                        "Here is the conceptual schematic for your question on **$subject**:\n\n" +
                        "*   **Fundamentals:** Identify variables and draw a free-body diagram/reaction map immediately.\n" +
                        "*   **Equation Matching:** Apply core equations matching your constraints.\n" +
                        "*   **AI Response:** Your active query is loaded successfully. Re-verify values or try querying standard textbook concepts (e.g. \"Explain gravity\", \"Newton's laws\", \"Optics rules\")."
            }
        }
    }

    // Practice Quiz interaction
    fun answerQuizQuestion(questionId: Int, selectedIndex: Int) {
        _quizQuestions.value = _quizQuestions.value.map { q ->
            if (q.id == questionId) {
                val correctedUser = q.copy(userSelectedIndex = selectedIndex)
                val isCorrect = selectedIndex == q.correctIndex
                val accuracyChange = if (isCorrect) 3 else -1
                val newAccuracy = (_userMetrics.value.accuracyPercentage + accuracyChange).coerceIn(40, 99)
                val addScore = if (isCorrect) 10 else 0
                _userMetrics.value = _userMetrics.value.copy(
                    accuracyPercentage = newAccuracy,
                    testRank = (_userMetrics.value.testRank - (if (isCorrect) 4 else -1)).coerceIn(1, 400)
                )

                // update leaderboard
                updateLeaderboardMyScore(addScore)

                // log attempt for student progress tracking
                val sub = if (q.id % 3 == 1) "Physics" else if (q.id % 3 == 2) "Chemistry" else "Mathematics"
                val attempt = QuizAttempt(
                    questionTitle = q.question,
                    selectedOption = q.options.getOrNull(selectedIndex) ?: "Unknown",
                    correctOption = q.options.getOrNull(q.correctIndex) ?: "Unknown",
                    isCorrect = isCorrect,
                    pointsGained = addScore,
                    subject = sub
                )
                _quizAttempts.value = _quizAttempts.value + attempt

                correctedUser
            } else q
        }
    }

    // Teacher Portal Addition Functions
    fun addQuizQuestion(question: QuizQuestion) {
        _quizQuestions.value = _quizQuestions.value + question
    }

    fun addSchedule(schedule: Schedule) {
        _schedules.value = _schedules.value + schedule
    }

    fun addWpsNote(note: WpsNote) {
        _wpsNotes.value = _wpsNotes.value + note
    }

    fun addLecture(batchId: String, lecture: Lecture) {
        val currentList = _lectures.value[batchId] ?: emptyList()
        val updatedMap = _lectures.value.toMutableMap().apply {
            put(batchId, currentList + lecture)
        }
        _lectures.value = updatedMap

        // Update target batch lecture count
        _batches.value = _batches.value.map {
            if (it.id == batchId) {
                it.copy(lecturesCount = it.lecturesCount + 1)
            } else it
        }
    }

    fun resetQuiz() {
        _quizQuestions.value = _quizQuestions.value.map {
            it.copy(userSelectedIndex = null)
        }
        _quizAttempts.value = emptyList() // Clear progress tracker logs for visual resets
    }

    private fun updateLeaderboardMyScore(addScore: Int) {
        val updatedList = _leaderboard.value.map {
            if (it.isSelf) it.copy(score = it.score + addScore) else it
        }
        val sortedList = updatedList.sortedByDescending { it.score }
        _leaderboard.value = sortedList.mapIndexed { idx, u ->
            u.copy(rank = idx + 1)
        }
    }

    private fun loadInitialMockData() {
        // Init Batches
        val sampleBatches = listOf(
            Batch(
                title = "Alpha Rankers (IIT-JEE Masterclass)",
                description = "Complete mechanics, electromagnetism, organic chemistry, and integral calculus for advanced JEE aspirants.",
                targetExam = "JEE Advanced 2027",
                facultyName = "Prof. Alok Pandey & Team",
                price = "₹4,999 (Standard)",
                rating = 4.9f,
                lecturesCount = 56,
                isEnrolled = true
            ),
            Batch(
                title = "Conqueror NEET 2027 Premium",
                description = "Simplified physical chemistry, anatomy visualized, and high-scoring biological taxonomy classes.",
                targetExam = "NEET Prep",
                facultyName = "Dr. Shreya Roy & Chemistry Crew",
                price = "₹3,499",
                rating = 4.8f,
                lecturesCount = 48,
                isEnrolled = false
            ),
            Batch(
                title = "Foundation Science (Class 10th NTSE)",
                description = "Core Physics, Chemistry and Math fundamentals designed to build logical clarity starting early.",
                targetExam = "Foundation",
                facultyName = "Sanjay Kumar Sir",
                price = "Free Trial",
                rating = 4.7f,
                lecturesCount = 24,
                isEnrolled = false
            )
        )
        _batches.value = sampleBatches
        _selectedBatch.value = sampleBatches.first()

        // Init Lectures mapping to batch titles
        _lectures.value = mapOf(
            sampleBatches[0].id to listOf(
                Lecture(title = "Class 01: Kinematics 1D - Graphs & Equations of Motion", duration = "1h 45m", subject = "Physics", isWatched = true),
                Lecture(title = "Class 02: Kinematics 2D - Projectile Trajectory Calculation", duration = "1h 50m", subject = "Physics"),
                Lecture(title = "Class 03: Gaseous State - Ideal Gas Equations & Real Deviations", duration = "1h 35m", subject = "Chemistry"),
                Lecture(title = "Class 04: Vector Matrices - Dot, Cross Products Applications", duration = "1h 40m", subject = "Mathematics")
            ),
            sampleBatches[1].id to listOf(
                Lecture(title = "Class 01: Anatomy - Cell Structures & Mitosis Visualized", duration = "1h 30m", subject = "Biology"),
                Lecture(title = "Class 02: Periodic Table - S & P Block Atomic Radii", duration = "1h 45m", subject = "Chemistry")
            ),
            sampleBatches[2].id to listOf(
                Lecture(title = "Class 01: Light Refraction & Total Internal Reflection Basics", duration = "1h 10m", subject = "Physics"),
                Lecture(title = "Class 02: Acids, Bases, & Salts Indicators Mastery", duration = "1h 15m", subject = "Chemistry")
            )
        )
        _activeLecture.value = _lectures.value[sampleBatches[0].id]?.first()

        // Init Schedule (Timetable)
        _schedules.value = listOf(
            Schedule(subject = "Physics", topic = "Newton's Pulley Constraints (Advanced)", instructor = "Alok Pandey Sir", time = "09:00 AM - 10:30 AM", date = "Today", isLive = true),
            Schedule(subject = "Chemistry", topic = "Symmetrical Hydrocarbons & IUPAC", instructor = "Sanjay Kumar Sir", time = "11:00 AM - 12:30 PM", date = "Today"),
            Schedule(subject = "Mathematics", topic = "Indefinite Integrals: Substitution tricks", instructor = "Prof. Verma", time = "03:00 PM - 04:30 PM", date = "Today"),
            Schedule(subject = "Physics", topic = "Electrostatics: Gauss Law Integration", instructor = "Alok Pandey Sir", time = "09:00 AM - 10:30 AM", date = "Tomorrow")
        )

        // Init Doubt Solves (Presets)
        _doubtHistory.value = listOf(
            Doubt(
                question = "How do we calculate the orbital speed of a satellite near the Earth's surface?",
                answer = "To find the orbital speed close to surface (h approx 0):\n\n" +
                        "1. Centripetal Force = Gravitational Force:\n" +
                        "   m * (v_orbital)^2 / R = G * M * m / R^2\n" +
                        "2. Simplify for orbital speed:\n" +
                        "   v_orbital = sqrt(G*M/R)\n" +
                        "3. Express in terms of surface acceleration (g = G*M/R^2):\n" +
                        "   v_orbital = sqrt(g * R)\n\n" +
                        "Plugging Earth's values (g approx 9.8 m/s^2, R approx 6400 km):\n" +
                        "v_orbital approx 7.92 km/s.\n" +
                        "This is the precise surface orbital speed!",
                subject = "Physics"
            ),
            Doubt(
                question = "Why does ice float on water from molecular density aspects?",
                answer = "Ice floats because it is less dense than liquid water.\n\n" +
                        "When liquid water cools down to 4 degrees C, it reaches maximum density. " +
                        "As it freezes, hydrogen bonding causes the water molecules to space out " +
                        "rigidly into an open hexagonal crystalline lattice. This layout contains empty space, " +
                        "making ice have roughly 9% greater volume but lower density than its liquid counterpart.",
                subject = "Chemistry"
            )
        )

        // Init Quizzes MCQ
        _quizQuestions.value = listOf(
            QuizQuestion(
                id = 1,
                question = "A particle experiences a force F = -kx. If it is released from displacement x = A, what is its maximum speed?",
                options = listOf(
                    "v = A * sqrt(k / m)",
                    "v = A * (k / m)",
                    "v = k * sqrt(A / m)",
                    "v = sqrt(k * A / m)"
                ),
                correctIndex = 0,
                explanation = "This is Simple Harmonic Motion (SHM). Inside SHM, total energy is constant: E = 1/2 k A^2. Maximum velocity occurs at mean position (displacement x = 0), so 1/2 m v_max^2 = 1/2 k A^2. Solving this returns v_max = A * sqrt(k/m)."
            ),
            QuizQuestion(
                id = 2,
                question = "Which of the following organic functional groups contains a carbonyl group bonded directly to two carbon atoms?",
                options = listOf(
                    "Aldehyde",
                    "Ketone",
                    "Carboxylic Acid",
                    "Ester"
                ),
                correctIndex = 1,
                explanation = "A ketone features a carbonyl group (C=O) attached directly to two other alkyl groups (carbon chain, R-CO-R). Aldehydes contain a carbonyl attached to at least one Hydrogen (R-CO-H)."
            ),
            QuizQuestion(
                id = 3,
                question = "If the radius of Earth is reduced by 1% with constant mass, the acceleration due to gravity on its surface would:",
                options = listOf(
                    "Increase by 1%",
                    "Decrease by 2%",
                    "Increase by 2%",
                    "Remain unchanged"
                ),
                correctIndex = 2,
                explanation = "g is equal to G*M/R^2. Taking logs or differentiating gives: dg/g = -2 * dR/R. If R decreases by 1%, dR/R = -0.01. So dg/g = -2 * (-0.01) = +0.02 (increase by 2%)."
            )
        )

        // Init Leaderboard matches PW atmosphere
        _leaderboard.value = listOf(
            LeaderboardUser(1, "Deepak Yadav (AIR 1)", 950, 98),
            LeaderboardUser(2, "Ananya S (AIR 3)", 920, 96),
            LeaderboardUser(3, "Rohan Sharma (AIR 14)", 895, 94),
            LeaderboardUser(4, "Pranav J (AIR 29)", 880, 92),
            LeaderboardUser(5, "You (Next Ranker)", 750, 84, isSelf = true),
            LeaderboardUser(6, "Divya K", 720, 80),
            LeaderboardUser(7, "Nikhil Patil", 700, 78)
        )

        // Pre-seed some initial quiz attempt logs for performance history charts
        _quizAttempts.value = listOf(
            QuizAttempt(
                questionTitle = "Find dimension formula of electrical permittivity epsilon_0",
                selectedOption = "[M^-1 L^-3 T^4 A^2]",
                correctOption = "[M^-1 L^-3 T^4 A^2]",
                isCorrect = true,
                pointsGained = 10,
                subject = "Physics"
            ),
            QuizAttempt(
                questionTitle = "Anatomy - Mitosis chromosomes aligning on equator occurs in:",
                selectedOption = "Metaphase",
                correctOption = "Metaphase",
                isCorrect = true,
                pointsGained = 10,
                subject = "Biology"
            ),
            QuizAttempt(
                questionTitle = "Isomerisms in Ketones functional groups",
                selectedOption = "Chain Isomerism",
                correctOption = "Metamerism",
                isCorrect = false,
                pointsGained = 0,
                subject = "Chemistry"
            )
        )

        // Pre-seed study materials written on WPS Note Writer
        _wpsNotes.value = listOf(
            WpsNote(
                title = "Polished JEE Physics Cheat Sheet: Projectiles & Kinematics",
                content = "### Core Projectile Formulary\n\n- Time of Flight: T = 2 * u * sin(theta) / g\n- Maximum Height: H = (u * sin(theta))^2 / (2 * g)\n- Horizontal Range: R = u^2 * sin(2 * theta) / g\n\n### Advanced Ranker Hacks:\n1. Maximum range is achieved at theta = 45 degrees.\n2. For complementary angles theta and 90 - theta, the horizontal range remains exactly identical.\n3. Write your free body diagrams instantly to analyze any accelerating trolley coordinates.",
                author = "Prof. Alok Pandey (HOD)",
                subject = "Physics"
            ),
            WpsNote(
                title = "Visual Chemistry Guide: Hydrocarbon Symmetries",
                content = "### Geometric Isomerism (Cis/Trans Symmetry)\n\nCis/Trans isomerism arises when there is restricted rotation in a molecule (e.g., carbon-carbon double bonds).\n\n- Cis isomer: Similar functional groups are on the same side of the double bond. Features higher net dipole moment and higher boiling point!\n- Trans isomer: Similar groups on opposite sides. Dipole moments cancel out, leading to enhanced crystal packing symmetry and higher melting point!\n\nUse this to predict melting and boiling point relations instantly.",
                author = "Dr. Shreya Roy",
                subject = "Chemistry"
            )
        )
    }
}
