package com.schulte.grid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.schulte.grid.audio.SoundManager
import com.schulte.grid.data.RecordRepository
import com.schulte.grid.data.SettingsRepository
import com.schulte.grid.model.AppSettings
import com.schulte.grid.model.GameRecord
import com.schulte.grid.model.GameState
import com.schulte.grid.model.GridSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 游戏 ViewModel —— 管理所有游戏逻辑和状态
 *
 * 设计原则：
 * - 设置变更即时反映到内存状态，异步持久化到 DataStore
 * - 计时器通过协程循环模拟 requestAnimationFrame
 * - 音效在协程中通过 AudioTrack 实时合成
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    // ============ 仓库 ============
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

    // ============ 内部状态 ============
    private var timerJob: Job? = null
    private var startTimeMs: Long = 0L
    private var countdownJob: Job? = null

    init {
        // 一次性加载持久化数据
        viewModelScope.launch {
            _settings.value = settingsRepo.getSettings()
            _bestRecords.value = recordRepo.getAllBests()
            _history.value = recordRepo.getHistory()
            // 初始化游戏（不显示倒计时）
            initGame(showCountdown = false)
        }
    }

    // ========================================================================
    //  游戏生命周期
    // ========================================================================

    /**
     * 初始化（或重新开始）一局游戏
     * @param showCountdown 是否显示预备倒计时
     */
    fun initGame(showCountdown: Boolean = _settings.value.showCountdown) {
        stopTimer()
        countdownJob?.cancel()

        val size = _gameState.value.gridSize
        val reverse = _settings.value.reverseMode
        val total = size.totalNumbers

        // Fisher-Yates 洗牌
        val numbers = (1..total).toMutableList()
        for (i in numbers.indices.reversed()) {
            val j = Random.nextInt(i + 1)
            val tmp = numbers[i]; numbers[i] = numbers[j]; numbers[j] = tmp
        }

        _gameState.value = GameState(
            gridSize = size,
            numbers = numbers,
            currentTarget = if (reverse) total else 1,
            clickedNumbers = emptySet(),
            wrongNumber = null,
            elapsedMs = 0L,
            isActive = false,
            isFinished = false,
            timerStarted = false,
            clickCount = 0,
        )

        if (showCountdown) {
            startCountdown()
        } else {
            _gameState.value = _gameState.value.copy(isActive = true)
        }
    }

    /** 切换网格尺寸 */
    fun setGridSize(size: GridSize) {
        _gameState.value = _gameState.value.copy(gridSize = size)
        initGame()
    }

    /** 再来一局 */
    fun playAgain() {
        initGame()
    }

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
        }
    }

    // ========================================================================
    //  点击处理
    // ========================================================================

    fun onCellClick(number: Int) {
        val state = _gameState.value
        if (!state.isActive || state.isFinished) return

        if (number == state.currentTarget) {
            // 正确
            if (!state.timerStarted) startTimer()
            soundManager.playCorrect()

            val newClicked = state.clickedNumbers + number
            val isReverse = _settings.value.reverseMode
            val newTarget = if (isReverse) {
                if (number > 1) number - 1 else null
            } else {
                if (number < state.totalNumbers) number + 1 else null
            }
            val isFinished = newTarget == null

            _gameState.value = state.copy(
                clickedNumbers = newClicked,
                currentTarget = newTarget ?: state.currentTarget,
                clickCount = state.clickCount + 1,
                wrongNumber = null,
                isFinished = isFinished,
            )

            if (isFinished) onGameComplete()
        } else {
            // 错误
            soundManager.playWrong()
            _gameState.value = state.copy(wrongNumber = number)
            viewModelScope.launch {
                delay(400)
                _gameState.value = _gameState.value.copy(wrongNumber = null)
            }
        }
    }

    // ========================================================================
    //  计时器
    // ========================================================================

    private fun startTimer() {
        if (timerJob != null) return
        startTimeMs = System.currentTimeMillis()
        _gameState.value = _gameState.value.copy(timerStarted = true)

        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = System.currentTimeMillis() - startTimeMs
                _gameState.value = _gameState.value.copy(elapsedMs = elapsed)
                delay(33L) // ~30fps（省电省 CPU，计时器不需要 60fps）
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
        val elapsed = _gameState.value.elapsedMs
        soundManager.playComplete()

        val size = _gameState.value.gridSize
        val isReverse = _settings.value.reverseMode

        viewModelScope.launch {
            recordRepo.tryUpdateBest(size, elapsed)
            recordRepo.addHistory(
                GameRecord(
                    gridSize = size,
                    elapsedMs = elapsed,
                    reverseMode = isReverse,
                )
            )
            // 刷新记录显示
            _bestRecords.value = recordRepo.getAllBests()
            _history.value = recordRepo.getHistory()
        }
    }

    // ========================================================================
    //  设置 —— 即时更新内存，异步持久化
    // ========================================================================

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        viewModelScope.launch {
            settingsRepo.updateSettings(newSettings)
        }
    }

    fun toggleDarkMode() {
        updateSettings(_settings.value.copy(darkMode = !_settings.value.darkMode))
    }

    fun toggleSound() {
        updateSettings(_settings.value.copy(soundEnabled = !_settings.value.soundEnabled))
    }

    fun toggleTimerVisibility() {
        updateSettings(_settings.value.copy(showTimer = !_settings.value.showTimer))
    }

    fun toggleCountdown() {
        updateSettings(_settings.value.copy(showCountdown = !_settings.value.showCountdown))
    }

    fun toggleReverseMode() {
        val newReverse = !_settings.value.reverseMode
        _settings.value = _settings.value.copy(reverseMode = newReverse)
        viewModelScope.launch {
            settingsRepo.updateSettings(_settings.value)
        }
        initGame() // 模式切换后重新开始
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
        soundManager.release()
    }
}
