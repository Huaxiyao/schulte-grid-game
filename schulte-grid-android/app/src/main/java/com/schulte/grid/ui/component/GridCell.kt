package com.schulte.grid.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 单个舒尔特方格单元格（极致性能版）
 *
 * ⚡ 优化：
 * - 无任何动画 — 不创建 Animatable 协程（49 格 = 49 个协程开销很大）
 * - 无阴影 shadow()
 * - 颜色直接计算，不经过 animateColorAsState
 */
@Composable
fun GridCell(
    number: Int,
    isDone: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gridSize: Int = 5,
) {
    val shape = remember { RoundedCornerShape(10.dp) }

    // 直接计算颜色，不经过 animateColorAsState（省掉 49 个动画协程）
    val bgColor: Color = when {
        isDone -> MaterialTheme.colorScheme.secondary
        isWrong -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor: Color = when {
        isDone -> MaterialTheme.colorScheme.onSecondary
        isWrong -> MaterialTheme.colorScheme.onError
        else -> MaterialTheme.colorScheme.onSurface
    }

    val fontSize = when (gridSize) {
        3 -> 32.sp; 4 -> 24.sp; 5 -> 20.sp; 6 -> 17.sp; 7 -> 14.sp
        else -> 20.sp
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bgColor, shape)
            .then(
                if (!isDone) Modifier.clickable(enabled = true, onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
