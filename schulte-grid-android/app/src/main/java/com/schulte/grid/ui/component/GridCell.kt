package com.schulte.grid.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 单个舒尔特方格单元格（性能优化版）
 *
 * ⚡ 优化要点：
 * - 移除 shadow() — 阴影渲染 GPU 开销大，49 格尤其明显
 * - 移除按压缩放动画 — 减少重排
 * - 移除抖动动画 — 纯视觉噪音
 * - 保留背景色过渡动画（仅完成时有变化，触发极少）
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

    // 仅正确/错误时变化，正常游戏中几乎不触发
    val bgColor by animateColorAsState(
        targetValue = when {
            isDone -> MaterialTheme.colorScheme.secondary
            isWrong -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(120),
        label = "cellBg",
    )

    val fontSize = when (gridSize) {
        3 -> 32.sp
        4 -> 24.sp
        5 -> 20.sp
        6 -> 17.sp
        7 -> 14.sp
        else -> 20.sp
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bgColor, shape)
            .then(
                if (!isDone) {
                    Modifier.clickable(enabled = true, onClick = onClick)
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = when {
                isDone -> MaterialTheme.colorScheme.onSecondary
                isWrong -> MaterialTheme.colorScheme.onError
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}


