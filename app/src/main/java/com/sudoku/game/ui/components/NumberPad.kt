package com.sudoku.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudoku.game.ui.theme.*

/**
 * 数字输入面板 (1-9 + 功能按钮)
 */
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onClearClick: () -> Unit,
    onNotesToggle: () -> Unit,
    onHintClick: () -> Unit,
    onUndoClick: () -> Unit,
    isNotesMode: Boolean,
    numberCounts: Map<Int, Int>,  // 每个数字已填入的个数
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 数字行 (1-9)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (num in 1..9) {
                val count = numberCounts[num] ?: 0
                val isComplete = count >= 9
                NumberButton(
                    number = num,
                    isComplete = isComplete,
                    onClick = { onNumberClick(num) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 功能按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 撤销
            FunctionButton(
                icon = null,
                label = "撤销",
                onClick = onUndoClick,
                modifier = Modifier.weight(1f)
            )
            // 笔记
            FunctionButton(
                icon = Icons.Outlined.Edit,
                label = "笔记",
                onClick = onNotesToggle,
                isActive = isNotesMode,
                modifier = Modifier.weight(1f)
            )
            // 清除
            FunctionButton(
                icon = null,
                label = "清除",
                onClick = onClearClick,
                modifier = Modifier.weight(1f)
            )
            // 提示
            FunctionButton(
                icon = Icons.Filled.Lightbulb,
                label = "提示",
                onClick = onHintClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    isComplete: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isComplete) {
        if (isDark) DarkSurfaceVariant else Color(0xFFF0F0F0)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val textColor = if (isComplete) {
        if (isDark) DarkOnSurfaceVariant else Color(0xFFBDBDBD)
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(enabled = !isComplete, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun FunctionButton(
    icon: ImageVector?,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDark) DarkSurfaceVariant else Color(0xFFF5F5F5)
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
