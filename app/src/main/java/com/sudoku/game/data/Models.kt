package com.sudoku.game.data

/**
 * 难度等级定义
 * 从 45 个提示（最简单）到 17 个提示（最难），步长为 2
 */
enum class Difficulty(val hints: Int, val label: String) {
    VERY_EASY(45, "入门"),
    EASY(43, "简单"),
    EASY_2(41, "简单+"),
    MEDIUM_EASY(39, "中简"),
    MEDIUM_EASY_2(37, "中简+"),
    MEDIUM(35, "中等"),
    MEDIUM_2(33, "中等+"),
    MEDIUM_HARD(31, "中难"),
    MEDIUM_HARD_2(29, "中难+"),
    HARD(27, "困难"),
    HARD_2(25, "困难+"),
    VERY_HARD(23, "很难"),
    VERY_HARD_2(21, "很难+"),
    EXPERT(19, "专家"),
    MASTER(17, "大师");

    companion object {
        fun fromHints(hints: Int): Difficulty =
            entries.firstOrNull { it.hints == hints } ?: MEDIUM
    }
}

/**
 * 游戏存档数据
 */
data class SaveSlot(
    val id: String,
    val name: String,
    val timestamp: Long,
    val difficulty: Int,        // hints 数量
    val elapsedTimeMs: Long,    // 已用时间
    val puzzle: List<List<Int>>,
    val solution: List<List<Int>>,
    val current: List<List<Int>>,
    val notes: List<List<Set<Int>>>,  // 笔记 (9x9, 每格一组候选数)
    val isCompleted: Boolean
)
