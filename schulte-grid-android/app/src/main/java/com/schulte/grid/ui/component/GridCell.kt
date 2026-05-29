package com.schulte.grid.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schulte.grid.ui.theme.DarkCorrect
import com.schulte.grid.ui.theme.DarkWrong
import com.schulte.grid.ui.theme.LightCorrect
import com.schulte.grid.ui.theme.LightWrong

/**
 * 单个舒尔特方格单元格
 *
 * @param number  格子显示的数字
 * @param isDone  是否已被正确点击
 * @param isWrong 是否刚被错误点击（触发抖动动画）
 * @param onClick 点击回调
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
    val shape = RoundedCornerShape(10.dp)

    // 背景颜色动画
    val bgColor by animateColorAsState(
        targetValue = when {
            isDone -> MaterialTheme.colorScheme.secondary
            isWrong -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(120),
        label = "cellBg",
    )

    // 错误抖动
    val shakeOffset by animateFloatAsState(
        targetValue = if (isWrong) 1f else 0f,
        animationSpec = tween(400),
        label = "shake",
    )

    // 点击缩放
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "pressScale",
    )

    // 根据网格尺寸调整字号
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
            .scale(scale)
            .then(
                if (!isDone) {
                    Modifier.clickable(
                        enabled = !isDone,
                        onClick = {
                            pressed = true
                            onClick()
                        }
                    )
                } else Modifier
            )
            .then(
                if (!isDone) {
                    Modifier.shadow(
                        elevation = if (pressed) 0.dp else 2.dp,
                        shape = shape,
                    )
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

    // 重置 pressed 状态
    if (pressed) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100)
            pressed = false
        }
    }
}
