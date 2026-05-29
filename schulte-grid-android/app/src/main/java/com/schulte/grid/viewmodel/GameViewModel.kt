package com.schulte.grid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.schulte.grid.audio.SoundManager
import com.schulte.grid.data.RecordRepository
import com.schulte.grid.data.SettingsRepository
import com.schulte.grid.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 游戏 ViewModel
 *
 * 支持模式：NORMAL / LETTER / ZERO_TRACE / TIME_CHALLENGE
 * 功能：暂停、振动、限时、主题切换
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val recordRepo = RecordRepository(application)
    private val settingsRepo = SettingsRepository(application)
    private val soundManager = SoundManager(application)

    // ============ 公开状态 ============
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _bestRecords = MutableStateFlow<Map<GridSize, Long>>(emptyMap())
    val bestRecords: StateFlow<Map<GridSize, Long>> = _bestRecords.asStateFlow()

    private val _history = MutableStateFlow<List<GameRecord>>(emptyList())
    val history: StateFlow<List<GameRecord>> = _history.asStateFlow()

    private val _countdownNumber = MutableStateFlow<Int?>(null)
    val countdownNumber: StateFlow<Int?> = _countdownNumber.asStateFlow()

    // ============ 内部 ============
    private var timerJob: Job? = null
    private var startTimeMs: Long = 0L
    private var pausedElapsedMs: Long = 0L
    private var countdownJob: Job? = null
    private var timeChallengeJob: Job? = null

    init {
        viewModelScope.launch {
            _settings.value = settingsRepo.getSettings()
            _bestRecords.value = recordRepo.getAllBests()
            _history.value = recordRepo.getHistory()
            initGame(showCountdown = false)
        }
    }

    // ========================================================================
    //  游戏初始化
    // ========================================================================

    fun initGame(showCountdown: Boolean = _settings.value.showCountdown) {
        stopTimer()
        countdownJob?.cancel()
        timeChallengeJob?.cancel()

        val size = _gameState.value.gridSize
        val mode = _settings.value.gameMode

        val items = generateItems(size, mode)
        val shuffled = items.toMutableList().also { list ->
            for (i in list.indices.reversed()) {
                val j = Random.nextInt(i + 1)
                val tmp = list[i]; list[i] = list[j]; list[j] = tmp
            }
        }

        val firstTarget = if (_settings.value.reverseMode) shuffled.size.toString() else "1"
        // 字母模式的首个目标
        val firstTargetStr = when (mode) {
            GameMode.LETTER -> if (_settings.value.reverseMode) intToLetter(shuffled.size) else "A"
            else -> firstTarget
        }

        _gameState.value = GameState(
            gridSize = size,
            items = shuffled,
            currentTarget = if (mode == GameMode.LETTER) firstTargetStr else firstTarget,
            clickedItems = emptySet(),
            wrongItem = null,
            elapsedMs = 0L,
            isActive = false,
            isFinished = false,
            timerStarted = false,
            clickCount = 0,
            gameMode = mode,
            isPaused = false,
            timeRemainingSec = if (mode == GameMode.TIME_CHALLENGE) 30 else 30,
            timeChallengeScore = 0,
            lastCorrectIndex = -1,
        )

        if (showCountdown) startCountdown()
        else _gameState.value = _gameState.value.copy(isActive = true)
    }

    private fun generateItems(size: GridSize, mode: GameMode): List<String> {
        val count = size.totalNumbers
        return when (mode) {
            GameMode.LETTER -> (1..count).map { intToLetter(it) }
            else -> (1..count).map { it.toString() }
        }
    }

    private fun intToLetter(n: Int): String {
        if (n <= 26) return ('A' + (n - 1)).toString()
        return "${('A' + ((n - 1) / 26) - 1)}${('A' + ((n - 1) % 26))}"
    }

    fun setGridSize(size: GridSize) {
        _gameState.value = _gameState.value.copy(gridSize = size)
        initGame()
    }

    fun playAgain() = initGame()

    // ========================================================================
    //  倒计时
    // ========================================================================

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _countdownNumber.value = 3
            soundManager.playCountdown()
            delay(600)
            _countdownNumber.value = 2
            soundManager.playCountdown()
            delay(600)
            _countdownNumber.value = 1
            soundManager.playCountdown()
            delay(600)
            _countdownNumber.value = null
            _gameState.value = _gameState.value.copy(isActive = true)
            // 限时模式激活后启动倒计时
            if (_gameState.value.gameMode == GameMode.TIME_CHALLENGE) startTimeChallenge()
        }
    }

    // ========================================================================
    //  暂停
    // ========================================================================

    fun togglePause() {
        val state = _gameState.value
        if (!state.isActive || state.isFinished) return

        if (!state.isPaused) {
            // 暂停
            stopTimer()
            pausedElapsedMs = state.elapsedMs
            timeChallengeJob?.cancel()
            _gameState.value = state.copy(isPaused = true)
        } else {
            // 恢复
            _gameState.value = state.copy(isPaused = false)
            if (state.timerStarted) resumeTimer()
            if (state.gameMode == GameMode.TIME_CHALLENGE) startTimeChallenge()
        }
    }

    // ========================================================================
    //  点击处理
    // ========================================================================

    fun onCellClick(item: String, index: Int) {
        val state = _gameState.value
        if (!state.isActive || state.isFinished || state.isPaused) return

        if (item == state.currentTarget) {
            // 正确
            if (!state.timerStarted) {
                startTimer()
                if (state.gameMode == GameMode.TIME_CHALLENGE) startTimeChallenge()
            }
            soundManager.playCorrect()
            if (_settings.value.vibrationEnabled) soundManager.vibrateCorrect()

            val newClicked = state.clickedItems + item
            val isReverse = _settings.value.reverseMode
            val mode = state.gameMode

            val newTarget = getNextTarget(item, isReverse, state.totalItems, mode)
            val isFinished = if (mode == GameMode.TIME_CHALLENGE) false else newTarget == null

            _gameState.value = state.copy(
                clickedItems = newClicked,
                currentTarget = newTarget ?: state.currentTarget,
                clickCount = state.clickCount + 1,
                wrongItem = null,
                isFinished = isFinished,
                lastCorrectIndex = index,
                timeChallengeScore = state.timeChallengeScore + 1,
            )

            if (isFinished) onGameComplete()
        } else {
            // 错误
            soundManager.playWrong()
            if (_settings.value.vibrationEnabled) soundManager.vibrateWrong()
            _gameState.value = state.copy(wrongItem = item)
            viewModelScope.launch {
                delay(400)
                _gameState.value = _gameState.value.copy(wrongItem = null)
            }
        }
    }

    private fun getNextTarget(current: String, reverse: Boolean, total: Int, mode: GameMode): String? {
        val currentNum = when (mode) {
            GameMode.LETTER -> letterToInt(current)
            else -> current.toIntOrNull() ?: return null
        } ?: return null

        val nextNum = if (reverse) {
            if (currentNum > 1) currentNum - 1 else null
        } else {
            if (currentNum < total) currentNum + 1 else null
        }

        return when (mode) {
            GameMode.LETTER -> nextNum?.let { intToLetter(it) }
            else -> nextNum?.toString()
        }
    }

    private fun letterToInt(s: String): Int? {
        if (s.length == 1) return s[0] - 'A' + 1
        return ((s[0] - 'A' + 1) * 26) + (s[1] - 'A' + 1)
    }

    // ========================================================================
    //  限时模式
    // ========================================================================

    private fun startTimeChallenge() {
        timeChallengeJob?.cancel()
        timeChallengeJob = viewModelScope.launch {
            while (_gameState.value.timeRemainingSec > 0 && !_gameState.value.isPaused) {
                delay(1000L)
                val remaining = _gameState.value.timeRemainingSec - 1
                _gameState.value = _gameState.value.copy(timeRemainingSec = remaining)
                if (remaining <= 0) {
                    // 时间到
                    stopTimer()
                    _gameState.value = _gameState.value.copy(isFinished = true)
                    onGameComplete()
                }
            }
        }
    }

    // ========================================================================
    //  计时器
    // ========================================================================

    private fun startTimer() {
        if (timerJob != null) return
        startTimeMs = System.currentTimeMillis()
        pausedElapsedMs = 0L
        _gameState.value = _gameState.value.copy(timerStarted = true)

        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = pausedElapsedMs + (System.currentTimeMillis() - startTimeMs)
                _gameState.value = _gameState.value.copy(elapsedMs = elapsed)
                delay(0L) // 无上限 — 由屏幕刷新率决定
            }
        }
    }

    private fun resumeTimer() {
        startTimeMs = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = pausedElapsedMs + (System.currentTimeMillis() - startTimeMs)
                _gameState.value = _gameState.value.copy(elapsedMs = elapsed)
                delay(0L)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // ========================================================================
    //  完成
    // ========================================================================

    private fun onGameComplete() {
        stopTimer()
        timeChallengeJob?.cancel()
        val elapsed = _gameState.value.elapsedMs
        soundManager.playComplete()

        val size = _gameState.value.gridSize
        val isReverse = _settings.value.reverseMode
        val mode = _gameState.value.gameMode

        viewModelScope.launch {
            // 限时模式不计入最佳记录（只记录历史）
            if (mode != GameMode.TIME_CHALLENGE) {
                recordRepo.tryUpdateBest(size, elapsed)
            }
            recordRepo.addHistory(
                GameRecord(
                    gridSize = size,
                    elapsedMs = elapsed,
                    reverseMode = isReverse,
                    gameMode = mode,
                    timeChallengeScore = _gameState.value.timeChallengeScore,
                )
            )
            _bestRecords.value = recordRepo.getAllBests()
            _history.value = recordRepo.getHistory()
        }
    }

    // ========================================================================
    //  设置
    // ========================================================================

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        viewModelScope.launch { settingsRepo.updateSettings(newSettings) }
    }

    fun setGameMode(mode: GameMode) {
        _settings.value = _settings.value.copy(gameMode = mode)
        viewModelScope.launch { settingsRepo.updateSettings(_settings.value) }
        initGame()
    }

    fun setTheme(index: Int) {
        _settings.value = _settings.value.copy(themeIndex = index)
        viewModelScope.launch { settingsRepo.updateSettings(_settings.value) }
    }

    fun toggleDarkMode() {
        updateSettings(_settings.value.copy(darkMode = !_settings.value.darkMode))
    }

    fun toggleSound() {
        updateSettings(_settings.value.copy(soundEnabled = !_settings.value.soundEnabled))
    }

    fun toggleVibration() {
        updateSettings(_settings.value.copy(vibrationEnabled = !_settings.value.vibrationEnabled))
    }

    fun toggleTimerVisibility() {
        updateSettings(_settings.value.copy(showTimer = !_settings.value.showTimer))
    }

    fun toggleCountdown() {
        updateSettings(_settings.value.copy(showCountdown = !_settings.value.showCountdown))
    }

    fun toggleReverseMode() {
        _settings.value = _settings.value.copy(reverseMode = !_settings.value.reverseMode)
        viewModelScope.launch { settingsRepo.updateSettings(_settings.value) }
        initGame()
    }

    fun clearRecords() {
        viewModelScope.launch {
            recordRepo.clearAll()
            _bestRecords.value = emptyMap()
            _history.value = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        countdownJob?.cancel()
        timeChallengeJob?.cancel()
        soundManager.release()
    }
}
