package com.sudoku.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudoku.game.logic.Board
import com.sudoku.game.logic.SudokuEngine
import com.sudoku.game.ui.theme.*

/**
 * 9×9 数独棋盘
 */
@Composable
fun SudokuBoard(
    puzzle: Board,
    current: Board,
    notes: Array<Array<Set<Int>>>,
    selectedCell: Pair<Int, Int>?,
    conflicts: Set<Pair<Int, Int>>,
    isErrorHighlight: Boolean,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val selectedRow = selectedCell?.first
    val selectedCol = selectedCell?.second
    val selectedNum = if (selectedCell != null) current[selectedCell.first][selectedCell.second] else 0

    // 棋盘背景
    val boardBg = if (isDark) BoardBackgroundDark else BoardBackground
    val thinLine = if (isDark) GridLineThinDark else GridLineThin
    val thickLine = if (isDark) GridLineThickDark else GridLineThick

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(boardBg)
            .border(2.dp, thickLine, RoundedCornerShape(12.dp))
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0..8) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0..8) {
                        val isSelected = r == selectedRow && c == selectedCol
                        val isSameRow = r == selectedRow
                        val isSameCol = c == selectedCol
                        val isSameBox = selectedRow != null && selectedCol != null &&
                                r / 3 == selectedRow / 3 && c / 3 == selectedCol / 3
                        val isSameNum = selectedNum != 0 && current[r][c] == selectedNum
                        val isConflict = conflicts.contains(r to c)
                        val isInitial = puzzle[r][c] != 0
                        val value = current[r][c]

                        // 背景色
                        val cellBg = when {
                            isSelected -> if (isDark) CellSelectedDark else CellSelected
                            isConflict && isErrorHighlight -> if (isDark) CellErrorDark else CellError
                            isSameNum && value != 0 -> if (isDark) CellSameNumberDark else CellSameNumber
                            isSameRow || isSameCol || isSameBox -> if (isDark) CellHighlightDark else CellHighlight
                            else -> Color.Transparent
                        }

                        // 文字色
                        val textColor = when {
                            isConflict && isErrorHighlight && !isInitial -> if (isDark) CellTextErrorDark else CellTextError
                            isInitial -> if (isDark) CellTextInitialDark else CellTextInitial
                            else -> if (isDark) CellTextUserDark else CellTextUser
                        }

                        // 边框
                        val borderEnd = if (c == 2 || c == 5) 1.5.dp else 0.5.dp
                        val borderColorEnd = if (c == 2 || c == 5) thickLine else thinLine

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(cellBg)
                                .clickable { onCellClick(r, c) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (value != 0) {
                                Text(
                                    text = value.toString(),
                                    color = textColor,
                                    fontSize = if (isInitial) 20.sp else 20.sp,
                                    fontWeight = if (isInitial) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            } else if (notes[r][c].isNotEmpty()) {
                                // 笔记显示
                                NotesGrid(notes = notes[r][c], isDark = isDark)
                            }
                        }

                        // 右边框
                        if (c < 8) {
                            Box(
                                modifier = Modifier
                                    .width(borderEnd)
                                    .fillMaxHeight()
                                    .background(borderColorEnd)
                            )
                        }
                    }
                }
                // 下边框
                if (r < 8) {
                    val lineH = if (r == 2 || r == 5) 1.5.dp else 0.5.dp
                    val lineColor = if (r == 2 || r == 5) thickLine else thinLine
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineH)
                            .background(lineColor)
                    )
                }
            }
        }
    }
}

/**
 * 笔记网格（3×3 小数字）
 */
@Composable
private fun NotesGrid(notes: Set<Int>, isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(1.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..2) {
                    val num = row * 3 + col + 1
                    Text(
                        text = if (num in notes) num.toString() else "",
                        fontSize = 8.sp,
                        color = if (isDark) NoteTextDark else NoteText,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
