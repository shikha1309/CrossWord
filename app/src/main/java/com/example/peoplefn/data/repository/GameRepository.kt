package com.example.peoplefn.data.repository

import android.content.Context
import com.example.peoplefn.data.datasource.JsonDataSource
import com.example.peoplefn.data.model.Level
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(context: Context) {
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private val jsonDataSource = JsonDataSource(context)
    private var cachedLevels: List<Level> = emptyList()

    private val _unlockedLevelMax = MutableStateFlow(prefs.getInt("unlocked_level_max", 1))
    val unlockedLevelMax: StateFlow<Int> = _unlockedLevelMax.asStateFlow()

    private val _coins = MutableStateFlow(prefs.getInt("coins", 100))
    val coins: StateFlow<Int> = _coins.asStateFlow()

    init {
        cachedLevels = jsonDataSource.loadLevels()
    }

    fun getAllLevels(): List<Level> {
        if (cachedLevels.isEmpty()) {
            cachedLevels = jsonDataSource.loadLevels()
        }
        return cachedLevels
    }

    fun getLevelById(id: Int): Level? {
        return getAllLevels().find { it.id == id }
    }

    fun getSolvedWordsForLevel(levelId: Int): Set<String> {
        return prefs.getStringSet("solved_words_level_$levelId", emptySet()) ?: emptySet()
    }

    fun saveSolvedWordsForLevel(levelId: Int, solvedWords: Set<String>) {
        prefs.edit().putStringSet("solved_words_level_$levelId", solvedWords).apply()
    }

    fun unlockNextLevel(completedLevelId: Int) {
        if (completedLevelId >= _unlockedLevelMax.value && completedLevelId < cachedLevels.size) {
            val nextLevel = completedLevelId + 1
            _unlockedLevelMax.value = nextLevel
            prefs.edit().putInt("unlocked_level_max", nextLevel).apply()
        }
    }

    fun addCoins(amount: Int) {
        val newCoins = _coins.value + amount
        _coins.value = newCoins
        prefs.edit().putInt("coins", newCoins).apply()
    }

    fun consumeCoins(amount: Int): Boolean {
        if (_coins.value >= amount) {
            val newCoins = _coins.value - amount
            _coins.value = newCoins
            prefs.edit().putInt("coins", newCoins).apply()
            return true
        }
        return false
    }
}

