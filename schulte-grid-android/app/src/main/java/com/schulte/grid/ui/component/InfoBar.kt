package com.schulte.grid.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 信息栏 —— 显示下一个目标、计时器、进度
 *
 * @param nextTarget    下一个应点击的数字
 * @param elapsedMs     已用时间（毫秒）
 * @param progressText  进度文本，如 "12 / 25"
 * @param showTimer     是否显示计时器
 */
@Composable
fun InfoBar(
    nextTarget: Int?,
    elapsedMs: Long,
    progressText: String,
    showTimer: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 下一个目标
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "下一个",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = nextTarget?.toString() ?: "✓",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // 计时器
        AnimatedVisibility(
            visible = showTimer,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                Text(
                    text = "⏱",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = formatTime(elapsedMs),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // 进度
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "🎯",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = progressText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val sec = ms / 1000.0
    return "%.2fs".format(sec)
}
