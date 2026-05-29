package com.schulte.grid.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schulte.grid.ui.component.*
import com.schulte.grid.viewmodel.GameViewModel

/**
 * 主游戏屏幕
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val bestRecords by viewModel.bestRecords.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val countdownNumber by viewModel.countdownNumber.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── 标题行 ──
        HeaderRow(
            isReverse = settings.reverseMode,
            isDarkMode = settings.darkMode,
            soundEnabled = settings.soundEnabled,
            showTimer = settings.showTimer,
            showCountdown = settings.showCountdown,
            onToggleDark = { viewModel.toggleDarkMode() },
            onToggleSound = { viewModel.toggleSound() },
            onToggleTimer = { viewModel.toggleTimerVisibility() },
            onToggleCountdown = { viewModel.toggleCountdown() },
        )

        Spacer(Modifier.height(12.dp))

        // ── 控制栏 ──
        ControlBar(
            currentSize = gameState.gridSize,
            isReverse = settings.reverseMode,
            onSizeSelected = { viewModel.setGridSize(it) },
            onToggleReverse = { viewModel.toggleReverseMode() },
            onRestart = { viewModel.initGame() },
        )

        Spacer(Modifier.height(4.dp))

        // ── 信息栏 ──
        InfoBar(
            nextTarget = gameState.nextTarget,
            elapsedMs = gameState.elapsedMs,
            progressText = gameState.progressText,
            showTimer = settings.showTimer,
        )

        Spacer(Modifier.height(8.dp))

        // ── 网格区域（含倒计时覆盖层） ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            GameGrid(
                gridSize = gameState.gridSize.size,
                numbers = gameState.numbers,
                clickedNumbers = gameState.clickedNumbers,
                wrongNumber = gameState.wrongNumber,
                onCellClick = { number -> viewModel.onCellClick(number) },
                modifier = Modifier.fillMaxSize(),
            )

            // 倒计时覆盖层
            CountdownOverlay(
                countdownNumber = countdownNumber,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── 完成弹窗 ──
        if (gameState.isFinished) {
            CompletionDialog(
                elapsedMs = gameState.elapsedMs,
                isNewBest = false,  // 简化处理，实际可在 ViewModel 中计算
                onPlayAgain = { viewModel.playAgain() },
            )

            Spacer(Modifier.height(12.dp))
        }

        // ── 记录面板 ──
        RecordSection(
            bestRecords = bestRecords,
            history = history,
            onClear = { viewModel.clearRecords() },
        )

        // 底部留白
        Spacer(Modifier.height(24.dp))
    }
}

/**
 * 标题行
 */
@Composable
private fun HeaderRow(
    isReverse: Boolean,
    isDarkMode: Boolean,
    soundEnabled: Boolean,
    showTimer: Boolean,
    showCountdown: Boolean,
    onToggleDark: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleTimer: () -> Unit,
    onToggleCountdown: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "🧠 舒尔特方格",
                style = MaterialTheme.typography.headlineLarge,
            )
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = if (isReverse) "反向模式" else "专注力训练",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // 右侧图标按钮
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconToggleBtn(
                text = if (isDarkMode) "☀️" else "🌙",
                isActive = false,
                onClick = onToggleDark,
            )
            IconToggleBtn(
                text = if (soundEnabled) "🔊" else "🔇",
                isActive = soundEnabled,
                onClick = onToggleSound,
            )
            IconToggleBtn(
                text = if (showTimer) "👁" else "👁‍🗨",
                isActive = showTimer,
                onClick = onToggleTimer,
            )
            IconToggleBtn(
                text = "⏳",
                isActive = showCountdown,
                onClick = onToggleCountdown,
            )
        }
    }
}

/**
 * 小图标切换按钮
 */
@Composable
private fun IconToggleBtn(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.size(36.dp),
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}

/**
 * 网格布局
 */
@Composable
private fun GameGrid(
    gridSize: Int,
    numbers: List<Int>,
    clickedNumbers: Set<Int>,
    wrongNumber: Int?,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = if (gridSize >= 6) 4.dp else 8.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                for (col in 0 until gridSize) {
                    val index = row * gridSize + col
                    if (index < numbers.size) {
                        val number = numbers[index]
                        GridCell(
                            number = number,
                            isDone = clickedNumbers.contains(number),
                            isWrong = number == wrongNumber,
                            onClick = { onCellClick(number) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            gridSize = gridSize,
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
