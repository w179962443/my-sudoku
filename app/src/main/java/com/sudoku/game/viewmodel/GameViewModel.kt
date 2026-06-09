package com.sudoku.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sudoku.game.data.Difficulty
import com.sudoku.game.data.SaveManager
import com.sudoku.game.data.SaveSlot
import com.sudoku.game.logic.Board
import com.sudoku.game.logic.SudokuEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// ======================== UI 状态 ========================

data class GameUiState(
    val puzzle: Board = SudokuEngine.emptyBoard(),
    val solution: Board = SudokuEngine.emptyBoard(),
    val current: Board = SudokuEngine.emptyBoard(),
    val notes: Array<Array<MutableSet<Int>>> = emptyNotes(),
    val selectedCell: Pair<Int, Int>? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val elapsedTimeMs: Long = 0L,
    val isNotesMode: Boolean = false,
    val isCompleted: Boolean = false,
    val isErrorHighlight: Boolean = true,
    val conflicts: Set<Pair<Int, Int>> = emptySet(),
    val isGenerating: Boolean = false,
    val undoStack: List<UndoAction> = emptyList()
) {
    companion object {
        fun emptyNotes(): Array<Array<MutableSet<Int>>> =
            Array(9) { Array(9) { mutableSetOf<Int>() } }
    }
}

data class UndoAction(
    val row: Int,
    val col: Int,
    val oldValue: Int,
    val oldNotes: Set<Int>
)

// ======================== ViewModel ========================

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val saveManager = SaveManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerRunning = false

    // ==================== 新游戏 ====================

    fun newGame(difficulty: Difficulty) {
        _uiState.update { it.copy(isGenerating = true) }
        viewModelScope.launch(Dispatchers.Default) {
            val (puzzle, solution) = SudokuEngine.generatePuzzle(difficulty.hints)
            val notes = GameUiState.emptyNotes()
            _uiState.update {
                it.copy(
                    puzzle = SudokuEngine.cloneBoard(puzzle),
                    solution = solution,
                    current = SudokuEngine.cloneBoard(puzzle),
                    notes = notes,
                    selectedCell = null,
                    difficulty = difficulty,
                    elapsedTimeMs = 0L,
                    isNotesMode = false,
                    isCompleted = false,
                    conflicts = emptySet(),
                    isGenerating = false,
                    undoStack = emptyList()
                )
            }
            startTimer()
        }
    }

    // ==================== 继续游戏 ====================

    fun continueAutoSave() {
        val slot = saveManager.loadAutoSave() ?: return
        loadFromSlot(slot)
    }

    fun hasAutoSave(): Boolean = saveManager.hasAutoSave()

    // ==================== 选中格子 ====================

    fun selectCell(row: Int, col: Int) {
        _uiState.update { it.copy(selectedCell = row to col) }
    }

    // ==================== 输入数字 ====================

    fun inputNumber(num: Int) {
        val state = _uiState.value
        val (r, c) = state.selectedCell ?: return
        if (state.puzzle[r][c] != 0) return  // 初始数字不可修改
        if (state.isCompleted) return

        if (state.isNotesMode) {
            // 笔记模式
            val oldNotes = state.notes[r][c].toSet()
            val newNotes = state.notes.toMutableList2D()
            if (num in newNotes[r][c]) {
                newNotes[r][c].remove(num)
            } else {
                newNotes[r][c].add(num)
            }
            // 清除该格的数字（如果有）
            val newCurrent = SudokuEngine.cloneBoard(state.current)
            newCurrent[r][c] = 0
            _uiState.update {
                it.copy(
                    current = newCurrent,
                    notes = newNotes,
                    undoStack = it.undoStack + UndoAction(r, c, 0, oldNotes),
                    conflicts = emptySet()
                )
            }
        } else {
            // 正常输入
            val oldVal = state.current[r][c]
            val oldNotes = state.notes[r][c].toSet()
            val newCurrent = SudokuEngine.cloneBoard(state.current)
            newCurrent[r][c] = num
            // 清除该格笔记
            val newNotes = state.notes.toMutableList2D()
            newNotes[r][c].clear()
            // 清除同行/列/宫中其他格的相同笔记
            clearRelatedNotes(newNotes, r, c, num)
            // 检查冲突
            val conflicts = if (state.isErrorHighlight) {
                SudokuEngine.getConflicts(newCurrent, r, c) + setOf(r to c)
            } else emptySet()
            // 检查是否完成
            val completed = SudokuEngine.isCompleteAndCorrect(newCurrent, state.solution)
            _uiState.update {
                it.copy(
                    current = newCurrent,
                    notes = newNotes,
                    conflicts = conflicts,
                    isCompleted = completed,
                    undoStack = it.undoStack + UndoAction(r, c, oldVal, oldNotes)
                )
            }
            if (completed) stopTimer()
        }
        autoSave()
    }

    // ==================== 清除格子 ====================

    fun clearCell() {
        val state = _uiState.value
        val (r, c) = state.selectedCell ?: return
        if (state.puzzle[r][c] != 0) return
        if (state.isCompleted) return

        val oldVal = state.current[r][c]
        val oldNotes = state.notes[r][c].toSet()
        val newCurrent = SudokuEngine.cloneBoard(state.current)
        newCurrent[r][c] = 0
        val newNotes = state.notes.toMutableList2D()
        newNotes[r][c].clear()

        _uiState.update {
            it.copy(
                current = newCurrent,
                notes = newNotes,
                conflicts = emptySet(),
                undoStack = it.undoStack + UndoAction(r, c, oldVal, oldNotes)
            )
        }
        autoSave()
    }

    // ==================== 撤销 ====================

    fun undo() {
        val state = _uiState.value
        val action = state.undoStack.lastOrNull() ?: return
        val newCurrent = SudokuEngine.cloneBoard(state.current)
        newCurrent[action.row][action.col] = action.oldValue
        val newNotes = state.notes.toMutableList2D()
        newNotes[action.row][action.col].clear()
        newNotes[action.row][action.col].addAll(action.oldNotes)

        _uiState.update {
            it.copy(
                current = newCurrent,
                notes = newNotes,
                conflicts = emptySet(),
                undoStack = it.undoStack.dropLast(1)
            )
        }
    }

    // ==================== 笔记模式切换 ====================

    fun toggleNotesMode() {
        _uiState.update { it.copy(isNotesMode = !it.isNotesMode) }
    }

    // ==================== 提示 ====================

    fun giveHint() {
        val state = _uiState.value
        if (state.isCompleted) return
        // 找一个空格或错误的格子填上正确答案
        val candidates = mutableListOf<Pair<Int, Int>>()
        for (r in 0..8) for (c in 0..8) {
            if (state.current[r][c] != state.solution[r][c]) {
                candidates.add(r to c)
            }
        }
        if (candidates.isEmpty()) return
        val (r, c) = candidates.random()
        val newCurrent = SudokuEngine.cloneBoard(state.current)
        newCurrent[r][c] = state.solution[r][c]
        val newNotes = state.notes.toMutableList2D()
        newNotes[r][c].clear()

        val completed = SudokuEngine.isCompleteAndCorrect(newCurrent, state.solution)
        _uiState.update {
            it.copy(
                current = newCurrent,
                notes = newNotes,
                selectedCell = r to c,
                conflicts = emptySet(),
                isCompleted = completed,
                undoStack = it.undoStack + UndoAction(r, c, state.current[r][c], state.notes[r][c].toSet())
            )
        }
        if (completed) stopTimer()
        autoSave()
    }

    // ==================== 计时器 ====================

    fun startTimer() {
        timerRunning = true
    }

    fun stopTimer() {
        timerRunning = false
    }

    fun tickTimer() {
        if (timerRunning && !_uiState.value.isCompleted) {
            _uiState.update { it.copy(elapsedTimeMs = it.elapsedTimeMs + 1000L) }
        }
    }

    fun pauseTimer() {
        timerRunning = false
    }

    fun resumeTimer() {
        if (!_uiState.value.isCompleted) timerRunning = true
    }

    // ==================== 存档 ====================

    fun saveToSlot(name: String) {
        val state = _uiState.value
        val slot = state.toSaveSlot(
            id = UUID.randomUUID().toString(),
            name = name
        )
        saveManager.saveGame(slot)
    }

    fun autoSave() {
        val state = _uiState.value
        val slot = state.toSaveSlot(id = "auto", name = "自动存档")
        saveManager.autoSave(slot)
    }

    fun loadFromSlot(slot: SaveSlot) {
        val notes = GameUiState.emptyNotes()
        for (r in 0..8) for (c in 0..8) {
            if (r < slot.notes.size && c < slot.notes[r].size) {
                notes[r][c].addAll(slot.notes[r][c])
            }
        }
        _uiState.update {
            it.copy(
                puzzle = slot.puzzle.toBoard(),
                solution = slot.solution.toBoard(),
                current = slot.current.toBoard(),
                notes = notes,
                difficulty = Difficulty.fromHints(slot.difficulty),
                elapsedTimeMs = slot.elapsedTimeMs,
                isCompleted = slot.isCompleted,
                selectedCell = null,
                conflicts = emptySet(),
                undoStack = emptyList()
            )
        }
        startTimer()
    }

    fun getAllSaves(): List<SaveSlot> = saveManager.getAllSaves()

    fun deleteSave(id: String) {
        saveManager.deleteSave(id)
    }

    fun clearAutoSave() {
        saveManager.clearAutoSave()
    }

    // ==================== 辅助 ====================

    private fun clearRelatedNotes(
        notes: Array<Array<MutableSet<Int>>>,
        row: Int, col: Int, num: Int
    ) {
        // 同行
        for (c in 0..8) notes[row][c].remove(num)
        // 同列
        for (r in 0..8) notes[r][col].remove(num)
        // 同宫
        val br = row / 3 * 3
        val bc = col / 3 * 3
        for (r in br until br + 3) for (c in bc until bc + 3) notes[r][c].remove(num)
    }

    private fun Array<Array<MutableSet<Int>>>.toMutableList2D(): Array<Array<MutableSet<Int>>> {
        return Array(9) { r -> Array(9) { c -> this[r][c].toMutableSet() } }
    }

    private fun GameUiState.toSaveSlot(id: String, name: String) = SaveSlot(
        id = id,
        name = name,
        timestamp = System.currentTimeMillis(),
        difficulty = difficulty.hints,
        elapsedTimeMs = elapsedTimeMs,
        puzzle = puzzle.toList2D(),
        solution = solution.toList2D(),
        current = current.toList2D(),
        notes = notes.map { row -> row.map { it.toSet() } },
        isCompleted = isCompleted
    )

    private fun Board.toList2D(): List<List<Int>> =
        this.map { it.toList() }

    private fun List<List<Int>>.toBoard(): Board =
        Array(this.size) { r -> this[r].toIntArray() }
}
