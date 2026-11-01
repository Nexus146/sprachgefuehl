package de.sprachgefuehl.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.sprachgefuehl.app.data.CheckupItem
import de.sprachgefuehl.app.data.ContentBundle
import de.sprachgefuehl.app.data.ContentRepository
import de.sprachgefuehl.app.data.LearningRepository
import de.sprachgefuehl.app.data.Lesson
import de.sprachgefuehl.app.data.Progress
import de.sprachgefuehl.app.data.Profile
import de.sprachgefuehl.app.data.ProgressStore
import de.sprachgefuehl.app.data.Settings
import de.sprachgefuehl.app.data.SituationVariant
import de.sprachgefuehl.app.domain.Checkup
import de.sprachgefuehl.app.domain.FocusBlock
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
    data object Onboarding : Screen
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val contentRepository = ContentRepository(application)
    private val store = ProgressStore(File(application.filesDir, "progress.json"))
    private var learning: LearningRepository? = null

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

    fun completeOnboarding() {
        learning?.completeOnboarding()
        openTab(AppTab.START)
    }

    fun recordStep(lessonId: String, stepIndex: Int) = learning?.recordStep(lessonId, stepIndex)

    /** One graded attempt; [skills] names what the exercise really trained. */
    fun recordSpeaking(
        chunkId: String,
        grade: Int,
        usedHint: Boolean,
        skills: List<Skill> = listOf(Skill.BAUSTEINE, Skill.SPRECHEN)
    ) {
        if (chunkId.isNotBlank()) learning?.reviewChunk(chunkId, grade, usedHint, skills)
    }

    fun recordError(cardId: String) = learning?.recordError(cardId)

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

    fun completeLesson(lessonId: String, speakingTasks: Int): Boolean {
        val repository = learning ?: return false
        val lesson = repository.content.lessons.firstOrNull { it.id == lessonId } ?: return false
        return repository.completeLesson(lesson, speakingTasks)
    }

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

    fun lesson(id: String): Lesson? = learning?.content?.lessons?.firstOrNull { it.id == id }

    fun lessonProgress(id: String): Int = _state.value.progress.lessonProgress[id] ?: 0

    val repository: LearningRepository? get() = learning
}
