package de.sprachgefuehl.app

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.sprachgefuehl.app.data.CheckupItem
import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.ContentRepository
import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.LessonSession
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.data.Settings
import de.sprachgefuehl.app.data.SituationVariant
import de.sprachgefuehl.app.data.account.CloudAccountRepository
import de.sprachgefuehl.app.domain.Checkup
import de.sprachgefuehl.app.domain.FocusBlock
import de.sprachgefuehl.app.domain.FocusRound
import de.sprachgefuehl.app.domain.LessonSuggestion
import de.sprachgefuehl.app.domain.Milestone
import de.sprachgefuehl.app.domain.Personalization
import de.sprachgefuehl.app.domain.SessionBuilder
import de.sprachgefuehl.app.domain.SessionComposer
import de.sprachgefuehl.app.domain.Skill
import de.sprachgefuehl.app.domain.SkillView
import de.sprachgefuehl.app.domain.Skills
import de.sprachgefuehl.app.ui.navigation.AppTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

data class AppUiState(
    val loading: Boolean = true,
    val content: ContentBundle? = null,
    val progress: Progress = Progress(),
    val error: String? = null
)

sealed interface Screen {
    data object Home : Screen
    data object Paths : Screen
    data class Situation(val id: String) : Screen
    data class Lesson(val id: String) : Screen
    data object Review : Screen
    data object Map : Screen
    data object Workshop : Screen
    data object Collection : Screen
    data object Practice : Screen
    data object Recap : Screen
    data object Profile : Screen
    data object Settings : Screen
    data object Search : Screen
    data object SkillProfile : Screen
    data object Checkup : Screen
    data object Preferences : Screen
    data object Guide : Screen
    data object Account : Screen
    data object Onboarding : Screen
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val contentRepository = ContentRepository(application)
    private val store = ProgressStore(File(application.filesDir, "progress.json"))
    private var learning: LearningRepository? = null

    // Accounts are optional. The repository is created even without Supabase values so the
    // account screen can explain that setup is still missing.
    private val cloudAccount = CloudAccountRepository(
        application,
        progressProvider = { learning?.progress?.value ?: store.load() },
        importProgress = { progress -> learning?.importProgress(progress) ?: false },
        // Abmelden räumt das Gerät ab: Danach steht der Erststart wieder am Anfang, und wer
        // weiterlernen will, meldet sich an und holt seinen Stand aus der Cloud.
        clearLocalProgress = {
            learning?.reset()
            _screen.value = Screen.Home
            _tab.value = AppTab.START
            backStack.clear()
        }
    )
    val account = cloudAccount.state

    // The saved settings are read once, synchronously: otherwise the first frame would flash in
    // the light theme before the dark choice from the profile is known.
    private val _state = MutableStateFlow(AppUiState(progress = store.load()))
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    private val _screen = MutableStateFlow<Screen>(Screen.Home)
    val screen: StateFlow<Screen> = _screen.asStateFlow()
    private val backStack = ArrayDeque<Screen>()

    private val _tab = MutableStateFlow(AppTab.START)
    val tab: StateFlow<AppTab> = _tab.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val bundle = withContext(Dispatchers.IO) { contentRepository.load() }
                val repository = LearningRepository(bundle, store)
                learning = repository
                _state.value = AppUiState(loading = false, content = bundle, progress = repository.progress.value)
                launch {
                    repository.progress.collect { progress ->
                        _state.value = _state.value.copy(progress = progress)
                    }
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = error.message ?: "Die Inhalte konnten nicht geladen werden."
                )
            }
        }
    }

    fun navigate(screen: Screen) {
        // A tab root is never shown as a detail page: opening it switches the tab, so the bar
        // keeps working and the right tab stays highlighted.
        AppTab.entries.firstOrNull { it.root == screen }?.let { tab ->
            openTab(tab)
            return
        }
        if (_screen.value == screen) return
        backStack.addLast(_screen.value)
        _screen.value = screen
    }

    /** Switching a tab starts fresh: the old back stack belongs to the other tab. */
    fun openTab(tab: AppTab) {
        backStack.clear()
        _tab.value = tab
        _screen.value = tab.root
    }

    /**
     * One step back. On the root of a tab the step leads to Start instead of doing nothing, so the
     * back arrow in a header is never a dead click.
     */
    fun back(): Boolean {
        val previous = backStack.removeLastOrNull()
        if (previous != null) {
            _screen.value = previous
            return true
        }
        if (_tab.value != AppTab.START) {
            openTab(AppTab.START)
            return true
        }
        return false
    }

    fun updateSettings(transform: (Settings) -> Settings) = learning?.updateSettings(transform)

    fun updateProfile(transform: (Profile) -> Profile) = learning?.updateProfile(transform)

    fun markHomeIntroSeen() = learning?.markHomeIntroSeen()

    fun markProfileReviewed() = learning?.markProfileReviewed()

    fun updatePlacementLevel(level: Int) = learning?.updatePlacementLevel(level)

    fun celebrateMilestones(): List<Milestone> = learning?.celebrateMilestones().orEmpty()

    fun exportProgress(): String = learning?.exportProgress() ?: ""

    fun importProgress(text: String): Boolean = learning?.importProgress(text) ?: false

    fun suggestion(): LessonSuggestion? = learning?.let { Personalization.suggestion(it.content, it.progress.value) }

    fun setPlacement(level: Int, score: Int, selfRating: Int) {
        learning?.setPlacement(level, score, selfRating)
        // Leaving onboarding must also leave the onboarding route: after a reset the screen is
        // Screen.Onboarding, and the content area would keep rendering it forever.
        openTab(AppTab.START)
    }

    /**
     * Der Abschluss des Onboardings in einem Schritt: Platzierung speichern und danach die
     * Einstiegslektion öffnen, die zu diesem Stand passt. Zwei getrennte Aufrufe hätten die
     * Lektion noch nach dem alten Stand gewählt.
     */
    fun beginAfterOnboarding(level: Int, score: Int, selfRating: Int) {
        setPlacement(level, score, selfRating)
        val next = learning?.let {
            Personalization.suggestion(it.content, it.progress.value)?.lesson
        } ?: return
        navigate(Screen.Lesson(next.id))
    }

    fun completeOnboarding() {
        learning?.completeOnboarding()
        openTab(AppTab.START)
    }

    // Fokus-Runden leben im Speicher: Sie gehören zu einer Fehlerkarte, nicht zum Lernpfad.
    private val _focusLessons = MutableStateFlow<Map<String, Lesson>>(emptyMap())

    fun focusLesson(lessonId: String): Lesson? = _focusLessons.value[lessonId]

    fun startFocusRound(cardId: String, day: Long): Lesson? {
        val bundle = _state.value.content ?: return null
        val card = bundle.errorCards.firstOrNull { it.id == cardId } ?: return null
        val lesson = FocusRound.lesson(card, bundle, _state.value.progress, day)
        _focusLessons.value = _focusLessons.value + (lesson.id to lesson)
        return lesson
    }

    /** Starts or resumes the one active lesson round. */
    fun startLesson(lessonId: String): LessonSession? =
        learning?.startLesson(lessonId, focusLesson(lessonId))

    fun today(): Long = learning?.today() ?: LocalDate.now().toEpochDay()

    fun dayOf(millis: Long): Long = learning?.dayOf(millis)
        ?: java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault())
            .toLocalDate().toEpochDay()

    fun recordStep(lessonId: String, stepIndex: Int) = learning?.recordStep(lessonId, stepIndex)

    fun recordPracticeRound(chunkIds: List<String>) = learning?.recordPracticeRound(chunkIds)

    /** One graded attempt; [skills] names what the exercise really trained. */
    fun recordSpeaking(
        chunkId: String,
        grade: Int,
        usedHint: Boolean,
        skills: List<Skill> = listOf(Skill.BAUSTEINE, Skill.SPRECHEN)
    ) {
        if (chunkId.isNotBlank()) learning?.reviewChunk(chunkId, grade, usedHint, skills)
    }

    fun recordAnswer(
        chunkId: String,
        grade: Int,
        usedHint: Boolean,
        skills: List<Skill>,
        cardId: String,
        lessonId: String,
        lessonTitle: String,
        target: String,
        heard: String,
        mistakeSkill: String,
        trackMistake: Boolean,
        checkSkill: Skill?,
        checkCorrect: Boolean,
        level: Int,
        taskType: String? = null
    ) = learning?.recordAnswer(
        chunkId, grade, usedHint, skills, cardId, lessonId, lessonTitle, target, heard,
        mistakeSkill, trackMistake, checkSkill, checkCorrect, level, taskType
    )

    fun recordError(cardId: String) = learning?.recordError(cardId)

    /** Ein Fehler mit Satz und Gehörtem – die nächste Lektion nimmt ihn wieder auf. */
    fun recordMistake(
        chunkId: String,
        lessonId: String,
        lessonTitle: String,
        target: String,
        heard: String,
        skill: Skill?,
        cardId: String
    ) = learning?.recordMistake(
        chunkId = chunkId,
        lessonId = lessonId,
        lessonTitle = lessonTitle,
        target = target,
        heard = heard,
        skill = skill?.id.orEmpty(),
        cardId = cardId
    )

    /** Eine richtige Antwort schließt den Eintrag. */
    fun resolveMistake(chunkId: String) = learning?.resolveMistake(chunkId)

    /** One checked task from a lesson or a practice round. */
    fun recordCheck(skill: Skill, correct: Boolean, level: Int = 1) =
        learning?.observeAnswer(skill, correct, level)

    // ---- Standort check -------------------------------------------------------------------

    fun startCheckup(selfRating: Int) = learning?.startCheckup(selfRating)

    /** True while a started standort check is unfinished – the home screen offers to continue. */
    fun checkupInProgress(): Boolean = learning?.let {
        val progress = it.progress.value
        progress.onboarded && progress.checkup.startedAt > 0L && !Checkup.isFinished(progress.checkup)
    } ?: false

    fun checkupItem(): CheckupItem? = learning?.let {
        Checkup.nextItem(it.progress.value.checkup, it.content.checkup)
    }

    fun answerCheckup(item: CheckupItem, correct: Boolean) = learning?.answerCheckup(item, correct)

    fun finishCheckup(selfRating: Int) = learning?.finishCheckup(selfRating)

    fun skillViews(): List<SkillView> = learning?.let { Skills.views(it.progress.value.skills) }.orEmpty()

    fun checkupLevel(): Int = learning?.let { Checkup.overallLevel(it.progress.value.skills) } ?: 1

    fun checkupCorrectCount(): Int =
        learning?.progress?.value?.checkup?.results?.count { it.value } ?: 0

    fun focusBlock(): FocusBlock? = learning?.let {
        SessionComposer.focus(it.content, it.progress.value, it.now())
    }

    fun variantFor(situationId: String): SituationVariant? = learning?.variantFor(situationId)

    fun completeLesson(lessonId: String, speakingTasks: Int, seconds: Int = 0): Boolean {
        val repository = learning ?: return false
        val lesson = repository.content.lessons.firstOrNull { it.id == lessonId } ?: return false
        return repository.completeLesson(lesson, speakingTasks, seconds)
    }

    /** Gemessene Lernzeit in Sekunden – nicht geschätzt. */
    fun recordLearningTime(seconds: Int) = learning?.recordLearningTime(seconds)

    fun recordReviewSession(minutes: Int, speakingTasks: Int) =
        learning?.recordReviewSession(minutes, speakingTasks)

    fun removeRecording(id: String) = learning?.removeRecording(id)

    fun addRecording(label: String, path: String) = learning?.addRecording(label, path)

    fun resetProgress() {
        learning?.reset()
        openTab(AppTab.START)
    }

    fun nextLesson(): Lesson? = suggestion()?.lesson

    fun dueCount(): Int = learning?.dueReviewCount() ?: 0

    fun diagnosticTrace(lessonId: String): String? = learning?.diagnosticTrace(lessonId)

    fun lesson(id: String): Lesson? = learning?.content?.lessons?.firstOrNull { it.id == id }

    fun lessonProgress(id: String): Int = _state.value.progress.lessonProgress[id] ?: 0

    // ---- Optional cloud account ---------------------------------------------------------

    fun handleAuthIntent(intent: Intent?) = cloudAccount.handleAuthIntent(intent)

    fun registerAccount(email: String, password: String) = cloudAccount.register(email, password)

    fun loginAccount(email: String, password: String) = cloudAccount.login(email, password)

    fun logoutAccount() = cloudAccount.logout()

    fun resendConfirmation() = cloudAccount.resendConfirmation()

    fun sendPasswordReset(email: String? = null) = cloudAccount.sendPasswordReset(email)

    fun updatePassword(password: String) = cloudAccount.updatePassword(password)

    fun setCloudSyncEnabled(enabled: Boolean) = cloudAccount.setSyncEnabled(enabled)

    fun syncCloudNow() = cloudAccount.syncNow()

    fun resolveCloudConflict(useCloud: Boolean) = cloudAccount.resolveConflict(useCloud)

    fun deleteAccount() = cloudAccount.deleteAccount()

    // ---- Admin console -----------------------------------------------------------------

    fun adminRefreshUsers() = cloudAccount.adminRefreshUsers()

    fun adminCreateUser(email: String, password: String) = cloudAccount.adminCreateUser(email, password)

    fun adminDeleteUser(userId: String) = cloudAccount.adminDeleteUser(userId)

    fun adminSetPassword(userId: String, password: String) =
        cloudAccount.adminSetPassword(userId, password)

    fun adminSetEmail(userId: String, email: String) = cloudAccount.adminSetEmail(userId, email)

    fun adminSetBanned(userId: String, banned: Boolean) =
        cloudAccount.adminSetBanned(userId, banned)

    fun adminResetProgress(userId: String) = cloudAccount.adminResetProgress(userId)

    fun adminLoadDetail(userId: String) = cloudAccount.adminLoadDetail(userId)

    val repository: LearningRepository? get() = learning
}
