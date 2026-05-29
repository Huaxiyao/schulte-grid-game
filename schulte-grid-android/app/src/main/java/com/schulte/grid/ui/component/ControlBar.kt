package com.schulte.grid.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schulte.grid.model.GridSize

/**
 * 控制栏 —— 网格尺寸选择、反向模式、重新开始
 */
@Composable
fun ControlBar(
    currentSize: GridSize,
    isReverse: Boolean,
    onSizeSelected: (GridSize) -> Unit,
    onToggleReverse: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 尺寸选择行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            GridSize.entries.forEach { size ->
                val isActive = size == currentSize
                FilledTonalButton(
                    onClick = { onSizeSelected(size) },
                    modifier = Modifier.weight(1f),
                    shape = when (size) {
                        GridSize.SIZE_3 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        GridSize.SIZE_7 -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        else -> RoundedCornerShape(0.dp)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isActive)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    Text(
                        text = size.label,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        // 操作按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 反向模式
            OutlinedButton(
                onClick = onToggleReverse,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isReverse)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(
                    1.dp,
                    if (isReverse) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("反向", fontSize = 13.sp)
            }

            // 重新开始
            Button(
                onClick = onRestart,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("重新开始", fontSize = 13.sp)
            }
        }
    }
}
