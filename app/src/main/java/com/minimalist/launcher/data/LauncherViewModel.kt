package com.minimalist.launcher.data

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val packageManager = application.packageManager

    // Apps
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val pinnedPackages: StateFlow<Set<String>> = prefs.pinnedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val pinnedApps: StateFlow<List<AppInfo>> = combine(_allApps, pinnedPackages) { apps, pinned ->
        apps.filter { pinned.contains(it.packageName) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedPackages: StateFlow<Set<String>> = prefs.blockedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val hiddenPackages: StateFlow<Set<String>> = prefs.hiddenApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Time
    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _greeting = MutableStateFlow("")
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    // Focus Timer
    private val _focusSession = MutableStateFlow(FocusSession())
    val focusSession: StateFlow<FocusSession> = _focusSession.asStateFlow()
    private var timerJob: Job? = null

    // Habits (Placeholder for persistence if needed)
    private val _habits = MutableStateFlow(
        listOf(
            HabitItem("1", "Morning walk", "🚶", false, 3),
            HabitItem("2", "Read 20 pages", "📚", false, 7),
            HabitItem("3", "Meditate", "🧘", false, 1),
            HabitItem("4", "No social media", "🚫", false, 0),
        )
    )
    val habits: StateFlow<List<HabitItem>> = _habits.asStateFlow()

    // Notes
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    val scratchNote: StateFlow<String> = prefs.scratchPad
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // UI state
    private val _showAllApps = MutableStateFlow(false)
    val showAllApps: StateFlow<Boolean> = _showAllApps.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    init {
        updateTime()
        startTimeClock()
        loadApps(application)
    }

    fun loadApps(context: Context) {
        viewModelScope.launch {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
                .sortedBy { it.loadLabel(packageManager).toString().lowercase() }

            val launcherPackage = context.packageName

            val apps = resolveInfoList
                .filter { it.activityInfo.packageName != launcherPackage }
                .map { info ->
                    AppInfo(
                        name = info.loadLabel(packageManager).toString(),
                        packageName = info.activityInfo.packageName,
                        icon = info.loadIcon(packageManager)
                    )
                }

            _allApps.value = apps
        }
    }

    fun launchApp(context: Context, packageName: String) {
        if (isBlocked(packageName)) return
        recordLaunch(packageName)
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredApps(): List<AppInfo> {
        val query = _searchQuery.value.lowercase().trim()
        val hidden = hiddenPackages.value
        return if (query.isEmpty()) {
            _allApps.value.filter { !hidden.contains(it.packageName) }
        } else {
            _allApps.value.filter {
                it.name.lowercase().contains(query) && !hidden.contains(it.packageName)
            }
        }
    }

    fun togglePin(app: AppInfo) {
        viewModelScope.launch {
            prefs.togglePin(app.packageName)
        }
    }

    fun isPinned(packageName: String): Boolean =
        pinnedPackages.value.contains(packageName)

    fun toggleHidden(packageName: String) {
        viewModelScope.launch {
            prefs.toggleHidden(packageName)
        }
    }

    fun setShowAllApps(show: Boolean) {
        _showAllApps.value = show
        if (!show) _searchQuery.value = ""
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    // Screen time (tracked within session)
    private val _appLaunchCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val appLaunchCounts: StateFlow<Map<String, Int>> = _appLaunchCounts.asStateFlow()

    fun recordLaunch(packageName: String) {
        val current = _appLaunchCounts.value.toMutableMap()
        current[packageName] = (current[packageName] ?: 0) + 1
        _appLaunchCounts.value = current
    }

    fun toggleBlock(packageName: String) {
        viewModelScope.launch {
            prefs.toggleBlock(packageName)
        }
    }

    fun isBlocked(packageName: String) = blockedPackages.value.contains(packageName)

    fun clearLaunchHistory() {
        _appLaunchCounts.value = emptyMap()
    }

    // ---- Time ----
    fun updateTime() {
        val now = Calendar.getInstance()
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        _currentTime.value = timeFmt.format(now.time)
        _currentDate.value = dateFmt.format(now.time)
        _greeting.value = when (now.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning."
            in 12..16 -> "Good afternoon."
            in 17..20 -> "Good evening."
            else -> "Good night."
        }
    }

    private fun startTimeClock() {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                updateTime()
            }
        }
    }

    // ---- Focus Timer ----
    fun startFocus() {
        val session = _focusSession.value
        val seconds = if (session.state == FocusState.PAUSED) {
            session.remainingSeconds
        } else {
            session.durationMinutes * 60
        }
        _focusSession.value = session.copy(state = FocusState.RUNNING, remainingSeconds = seconds)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0 && _focusSession.value.state == FocusState.RUNNING) {
                delay(1000)
                remaining--
                _focusSession.value = _focusSession.value.copy(remainingSeconds = remaining)
            }
            if (remaining == 0 && _focusSession.value.state == FocusState.RUNNING) {
                val completed = _focusSession.value.sessionsCompleted + 1
                _focusSession.value = _focusSession.value.copy(
                    state = FocusState.BREAK,
                    remainingSeconds = _focusSession.value.breakMinutes * 60,
                    sessionsCompleted = completed
                )
                startBreak()
            }
        }
    }

    private fun startBreak() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = _focusSession.value.breakMinutes * 60
            while (remaining > 0 && _focusSession.value.state == FocusState.BREAK) {
                delay(1000)
                remaining--
                _focusSession.value = _focusSession.value.copy(remainingSeconds = remaining)
            }
            if (remaining == 0) {
                resetFocus()
            }
        }
    }

    fun pauseFocus() {
        timerJob?.cancel()
        _focusSession.value = _focusSession.value.copy(state = FocusState.PAUSED)
    }

    fun resetFocus() {
        timerJob?.cancel()
        _focusSession.value = FocusSession()
    }

    fun setFocusDuration(minutes: Int) {
        _focusSession.value = FocusSession(durationMinutes = minutes)
    }

    // ---- Habits ----
    fun toggleHabit(id: String) {
        _habits.value = _habits.value.map { habit ->
            if (habit.id == id) {
                habit.copy(
                    isDoneToday = !habit.isDoneToday,
                    streakDays = if (!habit.isDoneToday) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
                )
            } else habit
        }
    }

    fun addHabit(name: String, emoji: String) {
        if (name.isBlank()) return
        val newHabit = HabitItem(
            id = UUID.randomUUID().toString(),
            name = name,
            emoji = emoji
        )
        _habits.value = _habits.value + newHabit
    }

    fun deleteHabit(id: String) {
        _habits.value = _habits.value.filter { it.id != id }
    }

    // ---- Notes ----
    fun updateScratchNote(text: String) {
        viewModelScope.launch {
            prefs.saveScratchPad(text)
        }
    }

    fun saveNote() {
        val content = scratchNote.value.trim()
        if (content.isEmpty()) return
        val note = Note(id = UUID.randomUUID().toString(), content = content)
        _notes.value = listOf(note) + _notes.value
        updateScratchNote("")
    }

    fun deleteNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }
    }
}
