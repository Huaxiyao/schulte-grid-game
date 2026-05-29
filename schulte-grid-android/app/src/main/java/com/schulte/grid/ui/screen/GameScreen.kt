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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schulte.grid.model.GameMode
import com.schulte.grid.ui.component.*
import com.schulte.grid.ui.theme.THEMES
import com.schulte.grid.viewmodel.GameViewModel
import kotlin.math.min

/**
 * 主游戏屏幕
 */
@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val bestRecords by viewModel.bestRecords.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val countdownNumber by viewModel.countdownNumber.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── 标题 ──
        HeaderRow(settings, viewModel)

        Spacer(Modifier.height(8.dp))

        // ── 控制栏 ──
        ControlBar(
            currentSize = gameState.gridSize,
            isReverse = settings.reverseMode,
            currentMode = settings.gameMode,
            currentTheme = settings.themeIndex,
            onSizeSelected = { viewModel.setGridSize(it) },
            onToggleReverse = { viewModel.toggleReverseMode() },
            onRestart = { viewModel.initGame() },
            onModeSelected = { viewModel.setGameMode(it) },
            onThemeSelected = { viewModel.setTheme(it) },
        )

        Spacer(Modifier.height(4.dp))

        // ── 信息栏 ──
        InfoBar(
            nextTarget = gameState.nextTarget,
            elapsedMs = gameState.elapsedMs,
            progressText = gameState.progressText,
            showTimer = settings.showTimer,
            isPaused = gameState.isPaused,
            gameMode = settings.gameMode,
            timeRemainingSec = gameState.timeRemainingSec,
            onTogglePause = { viewModel.togglePause() },
        )

        Spacer(Modifier.height(6.dp))

        // ── 网格 ──
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val mode = settings.gameMode
            val zeroTrace = mode == GameMode.ZERO_TRACE
            GameGrid(
                gridSize = gameState.gridSize.size,
                items = gameState.items,
                clickedItems = gameState.clickedItems,
                wrongItem = gameState.wrongItem,
                currentTarget = gameState.currentTarget,
                gameMode = mode,
                onCellClick = { item, index -> viewModel.onCellClick(item, index) },
                modifier = Modifier.fillMaxSize(),
            )

            // 倒计时
            CountdownOverlay(countdownNumber = countdownNumber, modifier = Modifier.fillMaxSize())

            // 暂停覆盖层
            if (gameState.isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏸", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("已暂停", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.togglePause() }) {
                            Text("继续")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── 完成 ──
        if (gameState.isFinished) {
            CompletionDialog(
                elapsedMs = gameState.elapsedMs,
                isNewBest = false,
                score = if (settings.gameMode == GameMode.TIME_CHALLENGE) gameState.timeChallengeScore else null,
                gameMode = settings.gameMode,
                onPlayAgain = { viewModel.playAgain() },
            )
            Spacer(Modifier.height(10.dp))
        }

        // ── 记录 ──
        RecordSection(bestRecords = bestRecords, history = history, onClear = { viewModel.clearRecords() })
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HeaderRow(settings: com.schulte.grid.model.AppSettings, viewModel: GameViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🧠 舒尔特方格", style = MaterialTheme.typography.headlineLarge)
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = when (settings.gameMode) {
                        GameMode.NORMAL -> if (settings.reverseMode) "反向" else "标准"
                        GameMode.LETTER -> "字母"
                        GameMode.ZERO_TRACE -> "零痕迹"
                        GameMode.TIME_CHALLENGE -> "限时"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            IconToggleBtn(if (settings.darkMode) "☀️" else "🌙", false) { viewModel.toggleDarkMode() }
            IconToggleBtn(if (settings.soundEnabled) "🔊" else "🔇", settings.soundEnabled) { viewModel.toggleSound() }
            IconToggleBtn(if (settings.showTimer) "👁" else "👁‍🗨", settings.showTimer) { viewModel.toggleTimerVisibility() }
            IconToggleBtn("⏳", settings.showCountdown) { viewModel.toggleCountdown() }
            IconToggleBtn(if (settings.vibrationEnabled) "📳" else "📴", settings.vibrationEnabled) { viewModel.toggleVibration() }
        }
    }
}

@Composable
private fun IconToggleBtn(text: String, isActive: Boolean, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.size(32.dp),
    ) {
        Text(text, fontSize = 12.sp)
    }
}

/**
 * 网格布局（自定义 Layout）
 */
@Composable
private fun GameGrid(
    gridSize: Int,
    items: List<String>,
    clickedItems: Set<String>,
    wrongItem: String?,
    currentTarget: String,
    gameMode: GameMode,
    onCellClick: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = if (gridSize >= 6) 4.dp else 6.dp
    val density = LocalDensity.current
    val gapPx = with(density) { gap.toPx() }

    Layout(
        modifier = modifier.aspectRatio(1f).clipToBounds(),
        content = {
            items.forEachIndexed { index, item ->
                GridCell(
                    item = item,
                    isDone = clickedItems.contains(item),
                    isWrong = item == wrongItem,
                    isTarget = item == currentTarget && gameMode == GameMode.ZERO_TRACE,
                    gameMode = gameMode,
                    onClick = { onCellClick(item, index) },
                    modifier = Modifier.aspectRatio(1f),
                    gridSize = gridSize,
                )
            }
        },
    ) { measurables, constraints ->
        val totalSize = min(constraints.maxWidth, constraints.maxHeight)
        val cellSizeInt = ((totalSize - gapPx * (gridSize - 1)) / gridSize.toFloat()).toInt()
        val cellConstraints = Constraints(cellSizeInt, cellSizeInt, cellSizeInt, cellSizeInt)
        val placeables = measurables.map { it.measure(cellConstraints) }

        layout(totalSize, totalSize) {
            placeables.forEachIndexed { index, placeable ->
                val row = index / gridSize
                val col = index % gridSize
                placeable.placeRelative(
                    x = (col * (cellSizeInt + gapPx)).toInt(),
                    y = (row * (cellSizeInt + gapPx)).toInt(),
                )
            }
        }
    }
}
