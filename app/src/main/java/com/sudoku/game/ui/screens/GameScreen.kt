package com.sudoku.game.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudoku.game.ui.components.NumberPad
import com.sudoku.game.ui.components.SudokuBoard
import com.sudoku.game.viewmodel.GameUiState
import kotlinx.coroutines.delay

/**
 * 游戏主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: GameUiState,
    onCellClick: (Int, Int) -> Unit,
    onNumberClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    onNotesToggle: () -> Unit,
    onHintClick: () -> Unit,
    onUndoClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onNewGame: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // 计时器
    LaunchedEffect(state.isCompleted) {
        while (!state.isCompleted) {
            delay(1000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.difficulty.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "提示: ${state.difficulty.hints}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 计时器
                    Text(
                        text = formatTime(state.elapsedTimeMs),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    // 保存按钮
                    IconButton(onClick = onSaveClick) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "保存"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 棋盘
            SudokuBoard(
                puzzle = state.puzzle,
                current = state.current,
                notes = state.notes.map { row -> row.map { it.toSet() }.toTypedArray() }.toTypedArray(),
                selectedCell = state.selectedCell,
                conflicts = state.conflicts,
                isErrorHighlight = state.isErrorHighlight,
                onCellClick = onCellClick,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 数字面板
            NumberPad(
                onNumberClick = onNumberClick,
                onClearClick = onClearClick,
                onNotesToggle = onNotesToggle,
                onHintClick = onHintClick,
                onUndoClick = onUndoClick,
                isNotesMode = state.isNotesMode,
                numberCounts = getNumberCounts(state.current)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 完成弹窗
        if (state.isCompleted) {
            CompletionDialog(
                time = state.elapsedTimeMs,
                difficulty = state.difficulty.label,
                onNewGame = onNewGame,
                onBack = onBackClick
            )
        }

        // 生成中遮罩
        if (state.isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "正在生成谜题...",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionDialog(
    time: Long,
    difficulty: String,
    onNewGame: () -> Unit,
    onBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        icon = {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "🎉 恭喜完成！",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "难度：$difficulty",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "用时：${formatTime(time)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onNewGame) {
                Text("再来一局")
            }
        },
        dismissButton = {
            TextButton(onClick = onBack) {
                Text("返回首页")
            }
        }
    )
}

/**
 * 保存对话框
 */
@Composable
fun SaveDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var saveName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存游戏", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = saveName,
                onValueChange = { saveName = it },
                label = { Text("存档名称") },
                placeholder = { Text("输入存档名称...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = saveName.ifBlank { "存档 ${System.currentTimeMillis() % 10000}" }
                    onSave(name)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// ======================== 工具函数 ========================

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun getNumberCounts(current: Array<IntArray>): Map<Int, Int> {
    val counts = mutableMapOf<Int, Int>()
    for (num in 1..9) counts[num] = 0
    for (r in 0..8) for (c in 0..8) {
        val v = current[r][c]
        if (v in 1..9) counts[v] = (counts[v] ?: 0) + 1
    }
    return counts
}
