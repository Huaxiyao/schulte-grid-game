package com.schulte.grid.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schulte.grid.model.GameMode
import com.schulte.grid.model.GridSize
import com.schulte.grid.ui.theme.THEMES

/**
 * 控制栏
 */
@Composable
fun ControlBar(
    currentSize: GridSize,
    isReverse: Boolean,
    currentMode: GameMode,
    currentTheme: Int,
    onSizeSelected: (GridSize) -> Unit,
    onToggleReverse: () -> Unit,
    onRestart: () -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onThemeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 尺寸
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            GridSize.entries.forEach { size ->
                val active = size == currentSize
                FilledTonalButton(
                    onClick = { onSizeSelected(size) },
                    modifier = Modifier.weight(1f),
                    shape = when (size) {
                        GridSize.SIZE_3 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        GridSize.SIZE_7 -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        else -> RoundedCornerShape(0.dp)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    Text(size.label, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }

        // 操作行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 反向
            OutlinedButton(
                onClick = onToggleReverse,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isReverse) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(1.dp, if (isReverse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(vertical = 6.dp),
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text("反向", fontSize = 11.sp)
            }

            // 模式（水平滚动，适应小屏）
            Row(
                modifier = Modifier.weight(3f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                GameMode.entries.forEach { mode ->
                    val active = mode == currentMode
                    SuggestionChip(
                        onClick = { onModeSelected(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    GameMode.NORMAL -> "标准"
                                    GameMode.LETTER -> "字母"
                                    GameMode.ZERO_TRACE -> "零痕"
                                    GameMode.TIME_CHALLENGE -> "⏱限时"
                                },
                                fontSize = 10.sp,
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (active) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }

            // 重开
            Button(
                onClick = onRestart,
                modifier = Modifier.weight(0.8f),
                contentPadding = PaddingValues(vertical = 6.dp),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text("重开", fontSize = 11.sp)
            }
        }

        // 主题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text("主题", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            THEMES.forEachIndexed { index, theme ->
                val active = index == currentTheme
                FilledTonalIconButton(
                    onClick = { onThemeSelected(index) },
                    modifier = Modifier.size(28.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (active) theme.lightAccent.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(theme.emoji, fontSize = 12.sp)
                }
            }
        }
    }
}
