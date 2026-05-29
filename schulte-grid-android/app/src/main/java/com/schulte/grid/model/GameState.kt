package com.schulte.grid.model

/**
 * 游戏完整状态
 */
data class GameState(
    /** 当前网格尺寸 */
    val gridSize: GridSize = GridSize.DEFAULT,
    /** 网格中内容的排列顺序（按行展开的一维数组） */
    val items: List<String> = emptyList(),
    /** 当前应点击的目标 */
    val currentTarget: String = "1",
    /** 已经正确点击的内容集合 */
    val clickedItems: Set<String> = emptySet(),
    /** 上次点击错误的内容（用于触发反馈后清空） */
    val wrongItem: String? = null,
    /** 已耗时（毫秒） */
    val elapsedMs: Long = 0L,
    /** 游戏是否可交互 */
    val isActive: Boolean = false,
    /** 游戏是否已完成 */
    val isFinished: Boolean = false,
    /** 计时器是否已启动 */
    val timerStarted: Boolean = false,
    /** 点击次数 */
    val clickCount: Int = 0,
    /** 游戏模式 */
    val gameMode: GameMode = GameMode.NORMAL,
    /** 是否暂停 */
    val isPaused: Boolean = false,
    /** 限时模式剩余秒数 */
    val timeRemainingSec: Int = 30,
    /** 限时模式得分（正确点击数） */
    val timeChallengeScore: Int = 0,
    /** 刚刚被正确点击的格子索引（用于完成动画） */
    val lastCorrectIndex: Int = -1,
) {
    /** 总项目数 */
    val totalItems: Int get() = gridSize.totalNumbers
    /** 进度文本 */
    val progressText: String
        get() {
            val done = clickedItems.size
            return "$done / $totalItems"
        }
    /** 下一个目标（如果游戏结束则为 null） */
    val nextTarget: String?
        get() = if (clickedItems.size >= totalItems) null else currentTarget
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
    val gameMode: GameMode = GameMode.NORMAL,
    val themeIndex: Int = 0,
    val vibrationEnabled: Boolean = true,
)

/**
 * 游戏记录条目
 */
data class GameRecord(
    val gridSize: GridSize,
    val elapsedMs: Long,
    val reverseMode: Boolean,
    val gameMode: GameMode = GameMode.NORMAL,
    val timeChallengeScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
)
