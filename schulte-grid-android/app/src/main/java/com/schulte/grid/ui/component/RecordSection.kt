package com.schulte.grid.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schulte.grid.model.GameRecord
import com.schulte.grid.model.GridSize

/**
 * 记录面板 —— 折线图 + 最佳记录 + 最近历史
 */
@Composable
fun RecordSection(
    bestRecords: Map<GridSize, Long>,
    history: List<GameRecord>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // ── 标题行 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("🏆 记录", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = onClear,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("清除", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            // ── 折线图（只显示标准模式 5×5 最近 20 条） ──
            val chartData = history
                .filter { it.gameMode.ordinal == 0 && it.gridSize == GridSize.SIZE_5 && it.elapsedMs > 0 }
                .take(20)
                .reversed()

            if (chartData.size >= 2) {
                Spacer(Modifier.height(8.dp))
                Text("5×5 标准模式趋势", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))

                val accent = MaterialTheme.colorScheme.primary
                val sub = MaterialTheme.colorScheme.onSurfaceVariant

                Canvas(
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                ) {
                    val minTime = chartData.minOf { it.elapsedMs }.toFloat()
                    val maxTime = chartData.maxOf { it.elapsedMs }.toFloat()
                    val range = (maxTime - minTime).coerceAtLeast(1f)

                    val stepX = size.width / (chartData.size - 1).coerceAtLeast(1)
                    val padding = 4f

                    // 画折线
                    val path = Path()
                    chartData.forEachIndexed { i, record ->
                        val x = i * stepX
                        val y = size.height - padding - ((record.elapsedMs - minTime) / range) * (size.height - padding * 2)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = accent,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )

                    // 起点/终点圆点
                    val first = chartData.first()
                    val last = chartData.last()
                    drawCircle(accent, 4f, Offset(0f, size.height - padding - ((first.elapsedMs - minTime) / range) * (size.height - padding * 2)))
                    drawCircle(accent, 4f, Offset((chartData.size - 1) * stepX, size.height - padding - ((last.elapsedMs - minTime) / range) * (size.height - padding * 2)))
                }

                // 标注最好/最新
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                ) {
                    Text("最好: ${formatTime(chartData.minOf { it.elapsedMs })}", fontSize = 10.sp, color = accent)
                    Text("最新: ${formatTime(chartData.last().elapsedMs)}", fontSize = 10.sp, color = sub)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }

            // ── 最佳 ──
            val sizes = GridSize.entries
            val hasAny = sizes.any { bestRecords.containsKey(it) }
            if (hasAny) {
                sizes.forEach { size ->
                    bestRecords[size]?.let { ms ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        ) {
                            Text(size.label, style = MaterialTheme.typography.bodySmall)
                            Text(formatTime(ms), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                Text("还没有记录，开始游戏吧!", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp))
            }

            // ── 历史 ──
            if (history.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Text("📜 最近记录", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp))

                history.take(5).forEach { record ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    ) {
                        val tag = when (record.gameMode) {
                            com.schulte.grid.model.GameMode.LETTER -> " 🔤"
                            com.schulte.grid.model.GameMode.ZERO_TRACE -> " 👻"
                            com.schulte.grid.model.GameMode.TIME_CHALLENGE -> " ⏱${record.timeChallengeScore}"
                            else -> if (record.reverseMode) " 🔄" else ""
                        }
                        Text("${record.gridSize.label}$tag", style = MaterialTheme.typography.bodySmall)
                        Text(formatTime(record.elapsedMs), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val sec = ms / 1000.0
    return "%.2fs".format(sec)
}
