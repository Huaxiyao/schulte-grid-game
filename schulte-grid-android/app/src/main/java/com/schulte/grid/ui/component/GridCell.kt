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
import com.schulte.grid.model.GameMode

/**
 * 单个方格（扁平设计）
 */
@Composable
fun GridCell(
    item: String,
    isDone: Boolean,
    isWrong: Boolean,
    isTarget: Boolean,
    gameMode: GameMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gridSize: Int = 5,
) {
    val radius = if (gridSize >= 6) 6.dp else 8.dp
    val shape = remember(radius) { RoundedCornerShape(radius) }

    val visualDone = isDone && gameMode != GameMode.ZERO_TRACE

    val bgColor: Color = when {
        visualDone -> MaterialTheme.colorScheme.secondary
        isWrong -> MaterialTheme.colorScheme.error
        isTarget && gameMode == GameMode.ZERO_TRACE -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor: Color = when {
        visualDone -> MaterialTheme.colorScheme.onSecondary
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
            text = item,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
