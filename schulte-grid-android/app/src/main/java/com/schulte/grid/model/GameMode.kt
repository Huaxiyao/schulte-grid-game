package com.schulte.grid.model

/**
 * 游戏模式
 */
enum class GameMode(val label: String, val description: String) {
    NORMAL("标准", "按 1→2→3… 顺序点击"),
    LETTER("字母", "按 A→B→C… 顺序点击"),
    ZERO_TRACE("零痕迹", "点击后无标记，考验记忆"),
    TIME_CHALLENGE("限时", "30 秒内尽可能多点"),
}
