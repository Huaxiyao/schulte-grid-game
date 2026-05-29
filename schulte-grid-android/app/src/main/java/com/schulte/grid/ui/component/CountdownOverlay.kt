package com.schulte.grid.ui.component

import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 倒计时覆盖层 — 显示 3-2-1 倒数
 *
 * @param countdownNumber 当前倒计时数字（1 / 2 / 3），null 时隐藏
 * @param onDismiss       倒计时结束后回调
 */
@Composable
fun CountdownOverlay(
    countdownNumber: Int?,
    modifier: Modifier = Modifier,
) {
    if (countdownNumber == null) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* 点击不执行任何操作，防止穿透 */ }
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 带动画的倒计时数字
        var animationTriggered by remember { mutableStateOf(false) }
        LaunchedEffect(countdownNumber) {
            animationTriggered = false
            delay(10)
            animationTriggered = true
        }

        val scale by animateFloatAsState(
            targetValue = if (animationTriggered) 1f else 0.5f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "cdScale",
        )

        val alpha by animateFloatAsState(
            targetValue = if (animationTriggered) 1f else 0f,
            animationSpec = tween(300),
            label = "cdAlpha",
        )

        Text(
            text = countdownNumber.toString(),
            fontSize = 80.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
        )
    }
}
