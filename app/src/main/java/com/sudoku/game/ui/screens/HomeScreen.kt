package com.sudoku.game.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudoku.game.data.Difficulty
import com.sudoku.game.ui.theme.*

/**
 * 首页 —— 新游戏 / 继续 / 存档管理
 */
@Composable
fun HomeScreen(
    hasAutoSave: Boolean,
    onNewGame: (Difficulty) -> Unit,
    onContinueGame: () -> Unit,
    onLoadSaves: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var showDifficultyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    ) else listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo & 标题
            Icon(
                imageVector = Icons.Filled.GridOn,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "数独",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "经典逻辑推理",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 继续游戏
            if (hasAutoSave) {
                MenuButton(
                    icon = Icons.Filled.PlayArrow,
                    label = "继续游戏",
                    sublabel = "从上次进度继续",
                    onClick = onContinueGame,
                    isPrimary = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 新游戏
            MenuButton(
                icon = Icons.Filled.Add,
                label = "新游戏",
                sublabel = "选择难度开始",
                onClick = { showDifficultyDialog = true },
                isPrimary = !hasAutoSave
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 存档管理
            MenuButton(
                icon = Icons.Filled.FolderOpen,
                label = "存档管理",
                sublabel = "查看和管理存档",
                onClick = onLoadSaves,
                isPrimary = false
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 底部信息
            Text(
                text = "小米 HyperOS 3 适配版",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    // 难度选择对话框
    if (showDifficultyDialog) {
        DifficultyDialog(
            onDismiss = { showDifficultyDialog = false },
            onSelect = { difficulty ->
                showDifficultyDialog = false
                onNewGame(difficulty)
            }
        )
    }
}

@Composable
private fun MenuButton(
    icon: ImageVector,
    label: String,
    sublabel: String,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isPrimary) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isPrimary) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sublabel,
                    fontSize = 13.sp,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DifficultyDialog(
    onDismiss: () -> Unit,
    onSelect: (Difficulty) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择难度",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Difficulty.entries.forEach { difficulty ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(difficulty) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = difficulty.label,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${difficulty.hints} 个提示",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // 难度指示条
                            DifficultyIndicator(
                                level = Difficulty.entries.indexOf(difficulty),
                                maxLevel = Difficulty.entries.size - 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DifficultyIndicator(level: Int, maxLevel: Int) {
    val filledCount = (level * 5 / maxLevel) + 1
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (i <= filledCount) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
