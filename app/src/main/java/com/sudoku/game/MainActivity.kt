package com.sudoku.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudoku.game.data.Difficulty
import com.sudoku.game.data.SaveSlot
import com.sudoku.game.ui.screens.*
import com.sudoku.game.ui.theme.SudokuGameTheme
import com.sudoku.game.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SudokuGameTheme {
                SudokuApp(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseTimer()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeTimer()
    }
}

// ======================== 导航路由 ========================

private sealed class Screen {
    data object Home : Screen()
    data object Game : Screen()
    data object Saves : Screen()
}

// ======================== 应用主入口 ========================

@Composable
private fun SudokuApp(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val gameState by viewModel.uiState.collectAsStateWithLifecycle()

    // 定时器 tick
    LaunchedEffect(gameState.isCompleted) {
        while (!gameState.isCompleted) {
            kotlinx.coroutines.delay(1000L)
            viewModel.tickTimer()
        }
    }

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                hasAutoSave = viewModel.hasAutoSave(),
                onNewGame = { difficulty ->
                    viewModel.newGame(difficulty)
                    currentScreen = Screen.Game
                },
                onContinueGame = {
                    viewModel.continueAutoSave()
                    currentScreen = Screen.Game
                },
                onLoadSaves = {
                    currentScreen = Screen.Saves
                }
            )
        }

        is Screen.Game -> {
            GameScreen(
                state = gameState,
                onCellClick = { r, c -> viewModel.selectCell(r, c) },
                onNumberClick = { num -> viewModel.inputNumber(num) },
                onClearClick = { viewModel.clearCell() },
                onNotesToggle = { viewModel.toggleNotesMode() },
                onHintClick = { viewModel.giveHint() },
                onUndoClick = { viewModel.undo() },
                onSaveClick = { showSaveDialog = true },
                onBackClick = {
                    viewModel.autoSave()
                    currentScreen = Screen.Home
                },
                onNewGame = {
                    viewModel.newGame(gameState.difficulty)
                }
            )

            if (showSaveDialog) {
                SaveDialog(
                    onSave = { name ->
                        viewModel.saveToSlot(name)
                        showSaveDialog = false
                    },
                    onDismiss = { showSaveDialog = false }
                )
            }
        }

        is Screen.Saves -> {
            val saves = remember { mutableStateOf(viewModel.getAllSaves()) }
            SaveScreen(
                saves = saves.value,
                onLoadSave = { slot ->
                    viewModel.loadFromSlot(slot)
                    currentScreen = Screen.Game
                },
                onDeleteSave = { id ->
                    viewModel.deleteSave(id)
                    saves.value = viewModel.getAllSaves()
                },
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )
        }
    }
}
