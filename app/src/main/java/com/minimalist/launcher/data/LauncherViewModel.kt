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
import org.json.JSONArray
import org.json.JSONObject

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
    
    private val _vinlandQuote = MutableStateFlow(getQuote())
    val vinlandQuote: StateFlow<String> = _vinlandQuote.asStateFlow()

    // Focus Timer
    private val _focusSession = MutableStateFlow(FocusSession())
    val focusSession: StateFlow<FocusSession> = _focusSession.asStateFlow()
    private var timerJob: Job? = null

    // Habits
    private val _habits = MutableStateFlow<List<HabitItem>>(emptyList())
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
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            prefs.habitsJson.collect { json ->
                if (json != null) {
                    val list = mutableListOf<HabitItem>()
                    val arr = JSONArray(json)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(HabitItem(
                            obj.getString("id"),
                            obj.getString("name"),
                            obj.getString("emoji"),
                            obj.getBoolean("isDoneToday"),
                            obj.getInt("streakDays")
                        ))
                    }
                    _habits.value = list
                } else {
                    // Default habits if none saved
                    val defaults = listOf(
                        HabitItem("1", "Morning walk", "🚶", false, 3),
                        HabitItem("2", "Read 20 pages", "📚", false, 7)
                    )
                    _habits.value = defaults
                    saveHabits(defaults)
                }
            }
        }
    }

    private fun saveHabits(list: List<HabitItem>) {
        viewModelScope.launch {
            val arr = JSONArray()
            list.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("emoji", it.emoji)
                obj.put("isDoneToday", it.isDoneToday)
                obj.put("streakDays", it.streakDays)
                arr.put(obj)
            }
            prefs.saveHabitsJson(arr.toString())
        }
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

    fun launchApp(context: Context, packageName: String, limitMinutes: Int = 0) {
        if (isBlocked(packageName)) return
        recordLaunch(packageName)
        
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
            
            if (limitMinutes > 0) {
                viewModelScope.launch {
                    delay(limitMinutes * 60 * 1000L)
                    // Close the app by returning home
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(homeIntent)
                }
            }
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
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        _currentTime.value = timeFmt.format(now.time)
        _currentDate.value = dateFmt.format(now.time)
        _greeting.value = when (now.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning."
            in 12..16 -> "Good afternoon."
            in 17..20 -> "Good evening."
            else -> "Good night."
        }
        
        if (now.get(Calendar.MINUTE) == 0 && now.get(Calendar.SECOND) == 0) {
            _vinlandQuote.value = getQuote()
        }
    }

    private fun startTimeClock() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateTime()
            }
        }
    }
    
    private fun getQuote(): String {
        val quotes = listOf(
            "I have no enemies. No one has any enemies.",
            "You're a kind person. You don't have to hurt anyone.",
            "A true warrior doesn't need a sword.",
            "Anger is a luxury you cannot afford.",
            "The heart is like a mirror, it reflects what's in front of it.",
            "Forgiveness is the highest form of strength.",
            "Control your anger, or it will control you."
        )
        return quotes.random()
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
        val newList = _habits.value.map { habit ->
            if (habit.id == id) {
                habit.copy(
                    isDoneToday = !habit.isDoneToday,
                    streakDays = if (!habit.isDoneToday) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
                )
            } else habit
        }
        _habits.value = newList
        saveHabits(newList)
    }

    fun addHabit(name: String, emoji: String) {
        if (name.isBlank()) return
        val newHabit = HabitItem(
            id = UUID.randomUUID().toString(),
            name = name,
            emoji = emoji
        )
        val newList = _habits.value + newHabit
        _habits.value = newList
        saveHabits(newList)
    }

    fun deleteHabit(id: String) {
        val newList = _habits.value.filter { it.id != id }
        _habits.value = newList
        saveHabits(newList)
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
