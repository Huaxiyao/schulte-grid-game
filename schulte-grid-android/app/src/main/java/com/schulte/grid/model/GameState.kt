package com.schulte.grid.model

/**
 * 游戏完整状态
 */
data class GameState(
    /** 当前网格尺寸 */
    val gridSize: GridSize = GridSize.DEFAULT,
    /** 网格中数字的排列顺序（按行展开的一维数组） */
    val numbers: List<Int> = emptyList(),
    /** 当前应点击的目标数字 */
    val currentTarget: Int = 1,
    /** 已经正确点击的数字集合 */
    val clickedNumbers: Set<Int> = emptySet(),
    /** 上次点击错误的数字（用于动画触发后清空） */
    val wrongNumber: Int? = null,
    /** 已耗时（毫秒） */
    val elapsedMs: Long = 0L,
    /** 游戏是否可交互（倒计时时不可用） */
    val isActive: Boolean = false,
    /** 游戏是否已完成 */
    val isFinished: Boolean = false,
    /** 计时器是否已启动（首次点击后） */
    val timerStarted: Boolean = false,
    /** 点击次数 */
    val clickCount: Int = 0,
) {
    /** 总数字数 */
    val totalNumbers: Int get() = gridSize.totalNumbers
    /** 进度文本 */
    val progressText: String
        get() {
            val done = clickedNumbers.size
            return "$done / $totalNumbers"
        }
    /** 下一个目标（如果游戏结束则为 null） */
    val nextTarget: Int?
        get() = if (clickedNumbers.size >= totalNumbers) null else currentTarget
}

/**
 * 用户设置
 */
data class AppSettings(
    val darkMode: Boolean = false,
    val soundEnabled: Boolean = true,
    val showTimer: Boolean = true,
    val showCountdown: Boolean = false,
    val reverseMode: Boolean = false,
)

/**
 * 游戏记录条目
 */
data class GameRecord(
    val gridSize: GridSize,
    val elapsedMs: Long,
    val reverseMode: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)
