package com.sudoku.game.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 存档管理器 —— 基于 SharedPreferences 的本地存储
 */
class SaveManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sudoku_saves", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SAVES = "save_slots"
        private const val KEY_CURRENT = "current_game"
        private const val MAX_SLOTS = 20
    }

    /** 获取所有存档列表（按时间倒序） */
    fun getAllSaves(): List<SaveSlot> {
        val json = prefs.getString(KEY_SAVES, null) ?: return emptyList()
        val type = object : TypeToken<List<SaveSlot>>() {}.type
        val saves: List<SaveSlot> = gson.fromJson(json, type)
        return saves.sortedByDescending { it.timestamp }
    }

    /** 保存游戏到指定槽位 */
    fun saveGame(slot: SaveSlot) {
        val saves = getAllSaves().toMutableList()
        val existingIdx = saves.indexOfFirst { it.id == slot.id }
        if (existingIdx >= 0) {
            saves[existingIdx] = slot
        } else {
            if (saves.size >= MAX_SLOTS) {
                // 移除最旧的存档
                saves.removeAt(saves.lastIndex)
            }
            saves.add(slot)
        }
        prefs.edit().putString(KEY_SAVES, gson.toJson(saves)).apply()
    }

    /** 删除存档 */
    fun deleteSave(id: String) {
        val saves = getAllSaves().toMutableList()
        saves.removeAll { it.id == id }
        prefs.edit().putString(KEY_SAVES, gson.toJson(saves)).apply()
    }

    /** 快速保存当前游戏进度（自动存档） */
    fun autoSave(slot: SaveSlot) {
        prefs.edit().putString(KEY_CURRENT, gson.toJson(slot)).apply()
    }

    /** 加载自动存档 */
    fun loadAutoSave(): SaveSlot? {
        val json = prefs.getString(KEY_CURRENT, null) ?: return null
        return try {
            gson.fromJson(json, SaveSlot::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /** 清除自动存档 */
    fun clearAutoSave() {
        prefs.edit().remove(KEY_CURRENT).apply()
    }

    /** 是否有自动存档 */
    fun hasAutoSave(): Boolean = prefs.contains(KEY_CURRENT)
}
