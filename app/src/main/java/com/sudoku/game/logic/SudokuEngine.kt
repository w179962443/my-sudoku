package com.sudoku.game.logic

import kotlin.random.Random

/**
 * Sudoku 核心引擎：生成、求解、校验
 */
object SudokuEngine {

    // ======================== 数据结构 ========================

    /** 9×9 棋盘，0 表示空 */
    typealias Board = Array<IntArray>

    fun emptyBoard(): Board = Array(9) { IntArray(9) }

    fun cloneBoard(board: Board): Board = Array(9) { board[it].copyOf() }

    // ======================== 求解器 ========================

    /**
     * 回溯求解，返回是否成功（就地修改 board）
     */
    fun solve(board: Board): Boolean {
        val empty = findEmpty(board) ?: return true
        val (r, c) = empty
        for (num in 1..9) {
            if (isValid(board, r, c, num)) {
                board[r][c] = num
                if (solve(board)) return true
                board[r][c] = 0
            }
        }
        return false
    }

    /**
     * 计算解的个数（最多 countTo 个就停止）
     */
    fun countSolutions(board: Board, countTo: Int = 2): Int {
        var count = 0
        fun dfs(b: Board): Boolean {
            val empty = findEmpty(b) ?: run { count++; return count >= countTo }
            val (r, c) = empty
            for (num in 1..9) {
                if (isValid(b, r, c, num)) {
                    b[r][c] = num
                    if (dfs(b)) return true
                    b[r][c] = 0
                }
            }
            return false
        }
        dfs(cloneBoard(board))
        return count
    }

    /**
     * 验证唯一解
     */
    fun hasUniqueSolution(board: Board): Boolean = countSolutions(board, 2) == 1

    private fun findEmpty(board: Board): Pair<Int, Int>? {
        for (r in 0..8) for (c in 0..8) if (board[r][c] == 0) return r to c
        return null
    }

    fun isValid(board: Board, row: Int, col: Int, num: Int): Boolean {
        // 行
        for (c in 0..8) if (board[row][c] == num) return false
        // 列
        for (r in 0..8) if (board[r][col] == num) return false
        // 宫
        val br = row / 3 * 3
        val bc = col / 3 * 3
        for (r in br until br + 3) for (c in bc until bc + 3) if (board[r][c] == num) return false
        return true
    }

    // ======================== 生成器 ========================

    /**
     * 生成完整终盘
     */
    fun generateSolvedBoard(seed: Long = System.nanoTime()): Board {
        val board = emptyBoard()
        val rng = Random(seed)
        fillBoard(board, rng)
        return board
    }

    private fun fillBoard(board: Board, rng: Random): Boolean {
        val empty = findEmpty(board) ?: return true
        val (r, c) = empty
        val nums = (1..9).shuffled(rng)
        for (num in nums) {
            if (isValid(board, r, c, num)) {
                board[r][c] = num
                if (fillBoard(board, rng)) return true
                board[r][c] = 0
            }
        }
        return false
    }

    /**
     * 生成谜题
     * @param hints 保留的提示数量 (17–81)
     * @return Pair(谜题Board, 终盘Board)
     */
    fun generatePuzzle(hints: Int, seed: Long = System.nanoTime()): Pair<Board, Board> {
        require(hints in 17..81) { "hints must be between 17 and 81" }
        val rng = Random(seed)
        val solution = generateSolvedBoard(seed)
        val puzzle = cloneBoard(solution)
        val cells = (0 until 81).toMutableList().shuffled(rng).toMutableList()
        var removed = 0
        val toRemove = 81 - hints

        for (idx in cells) {
            if (removed >= toRemove) break
            val r = idx / 9
            val c = idx % 9
            val backup = puzzle[r][c]
            puzzle[r][c] = 0

            if (hasUniqueSolution(puzzle)) {
                removed++
            } else {
                puzzle[r][c] = backup
            }
        }

        return puzzle to solution
    }

    // ======================== 校验 ========================

    /**
     * 检查当前玩家的填写是否与终盘一致
     */
    fun isCompleteAndCorrect(current: Board, solution: Board): Boolean {
        for (r in 0..8) for (c in 0..8) {
            if (current[r][c] != solution[r][c]) return false
        }
        return true
    }

    /**
     * 检查某个格子填的数是否正确（相对于终盘）
     */
    fun isCellCorrect(current: Board, solution: Board, row: Int, col: Int): Boolean {
        return current[row][col] == 0 || current[row][col] == solution[row][col]
    }

    /**
     * 获取冲突格子集合（同一行/列/宫中有相同数字的）
     */
    fun getConflicts(board: Board, row: Int, col: Int): Set<Pair<Int, Int>> {
        if (board[row][col] == 0) return emptySet()
        val num = board[row][col]
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        // 行
        for (c in 0..8) if (c != col && board[row][c] == num) conflicts.add(row to c)
        // 列
        for (r in 0..8) if (r != row && board[r][col] == num) conflicts.add(r to col)
        // 宫
        val br = row / 3 * 3
        val bc = col / 3 * 3
        for (r in br until br + 3) for (c in bc until bc + 3) {
            if (r != row || c != col) {
                if (board[r][c] == num) conflicts.add(r to c)
            }
        }
        return conflicts
    }
}
