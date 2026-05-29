package com.schulte.grid.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schulte.grid.model.GameMode

/**
 * 信息栏 —— 下一个目标 / 计时 / 限时 / 暂停
 */
@Composable
fun InfoBar(
    nextTarget: String?,
    elapsedMs: Long,
    progressText: String,
    showTimer: Boolean,
    isPaused: Boolean,
    gameMode: GameMode,
    timeRemainingSec: Int,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 下一个目标
        Column(modifier = Modifier.weight(1f)) {
            Text("下一个", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = nextTarget ?: "✓",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // 限时倒计时
        if (gameMode == GameMode.TIME_CHALLENGE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("⏱", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${timeRemainingSec}s",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (timeRemainingSec <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
        } else if (showTimer) {
            // 普通计时
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("⏱", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = formatTime(elapsedMs),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // 进度
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("🎯", style = MaterialTheme.typography.bodySmall)
            Text(progressText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        // 暂停按钮
        if (gameMode == GameMode.TIME_CHALLENGE) {
            IconButton(
                onClick = onTogglePause,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "继续" else "暂停",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val sec = ms / 1000.0
    return "%.2fs".format(sec)
}
