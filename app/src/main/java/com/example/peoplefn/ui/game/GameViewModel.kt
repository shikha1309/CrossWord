package com.example.peoplefn.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peoplefn.data.model.GridCell
import com.example.peoplefn.data.model.Level
import com.example.peoplefn.data.model.WordDirection
import com.example.peoplefn.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.peoplefn.utils.SoundManager

enum class FeedbackType {
    NONE,
    VALID_GRID,
    BONUS,
    ALREADY_FOUND,
    INVALID
}

data class GameUiState(
    val level: Level? = null,
    val wheelLetters: List<String> = emptyList(),
    val gridMatrix: List<List<GridCell?>> = emptyList(),
    val solvedWordIds: Set<String> = emptySet(),
    val foundBonusWords: Set<String> = emptySet(),
    val currentFormedWord: String = "",
    val feedbackMessage: String? = null,
    val feedbackType: FeedbackType = FeedbackType.NONE,
    val isLevelComplete: Boolean = false,
    val isPaused: Boolean = false,
    val coins: Int = 100,
    val isLockInteraction: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    private val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun loadLevel(levelId: Int) {
        val level = repository.getLevelById(levelId) ?: return
        val solvedWords = repository.getSolvedWordsForLevel(levelId)
        val gridMatrix = buildInitialGridMatrix(level, solvedWords)

        _uiState.update {
            GameUiState(
                level = level,
                wheelLetters = level.wheelLetters,
                gridMatrix = gridMatrix,
                solvedWordIds = solvedWords,
                coins = repository.coins.value
            )
        }
    }

    private fun buildInitialGridMatrix(level: Level, solvedWordIds: Set<String>): List<List<GridCell?>> {
        val matrix = MutableList(level.gridHeight) {
            MutableList<GridCell?>(level.gridWidth) { null }
        }

        level.words.forEach { word ->
            val charArray = word.text.toCharArray()
            val wordSolved = solvedWordIds.contains(word.id)
            charArray.forEachIndexed { i, c ->
                val r = if (word.direction == WordDirection.VERTICAL) word.startRow + i else word.startRow
                val col = if (word.direction == WordDirection.HORIZONTAL) word.startCol + i else word.startCol

                val existing = matrix[r][col]
                val belongsIds = (existing?.belongsToWordIds ?: emptyList()) + word.id
                val isRevealed = wordSolved || (existing?.isRevealed ?: false)

                matrix[r][col] = GridCell(
                    row = r,
                    col = col,
                    char = c,
                    isRevealed = isRevealed,
                    belongsToWordIds = belongsIds
                )
            }
        }
        return matrix
    }

    fun updateCurrentFormedWord(word: String) {
        _uiState.update { it.copy(currentFormedWord = word) }
    }

    fun submitWord(word: String) {
        val state = _uiState.value
        if (state.isLockInteraction) return
        val level = state.level ?: return

        val uppercaseWord = word.uppercase()
        if (uppercaseWord.isBlank()) return

        // 1. Check if matches target word in grid
        val targetWord = level.words.find { it.text.equals(uppercaseWord, ignoreCase = true) }

        if (targetWord != null) {
            if (state.solvedWordIds.contains(targetWord.id)) {
                soundManager.playError()
                triggerFeedback("ALREADY FOUND!", FeedbackType.ALREADY_FOUND)
                return
            }

            // Mark solved
            val newSolvedWordIds = state.solvedWordIds + targetWord.id
            val newMatrix = revealWordInMatrix(state.gridMatrix, targetWord)

            // Save to SharedPreferences
            repository.saveSolvedWordsForLevel(level.id, newSolvedWordIds)

            repository.addCoins(20)
            val isComplete = newSolvedWordIds.size == level.words.size

            if (isComplete) {
                repository.unlockNextLevel(level.id)
                soundManager.playVictory()
            } else {
                soundManager.playSuccess()
            }

            _uiState.update {
                it.copy(
                    solvedWordIds = newSolvedWordIds,
                    gridMatrix = newMatrix,
                    coins = repository.coins.value,
                    isLevelComplete = isComplete
                )
            }

            triggerFeedback("AWESOME! +20 COINS", FeedbackType.VALID_GRID)
            return
        }

        // 2. Check if bonus word
        val isBonusWord = level.bonusWords.any { it.equals(uppercaseWord, ignoreCase = true) }
        if (isBonusWord) {
            if (state.foundBonusWords.contains(uppercaseWord)) {
                soundManager.playError()
                triggerFeedback("BONUS ALREADY FOUND!", FeedbackType.ALREADY_FOUND)
            } else {
                repository.addCoins(10)
                _uiState.update {
                    it.copy(
                        foundBonusWords = it.foundBonusWords + uppercaseWord,
                        coins = repository.coins.value
                    )
                }
                soundManager.playSuccess()
                triggerFeedback("BONUS WORD! +10 COINS", FeedbackType.BONUS)
            }
            return
        }

        // 3. Invalid word
        soundManager.playError()
        triggerFeedback("WRONG WORD ", FeedbackType.INVALID)
    }

    private fun revealWordInMatrix(
        matrix: List<List<GridCell?>>,
        targetWord: com.example.peoplefn.data.model.Word
    ): List<List<GridCell?>> {
        val newMatrix = matrix.map { row -> row.toMutableList() }.toMutableList()

        for (i in targetWord.text.indices) {
            val r = if (targetWord.direction == WordDirection.VERTICAL) targetWord.startRow + i else targetWord.startRow
            val c = if (targetWord.direction == WordDirection.HORIZONTAL) targetWord.startCol + i else targetWord.startCol

            val cell = newMatrix[r][c]
            if (cell != null) {
                newMatrix[r][c] = cell.copy(isRevealed = true, isJustRevealed = true)
            }
        }
        return newMatrix
    }

    fun useHint() {
        val state = _uiState.value
        val level = state.level ?: return

        if (!repository.consumeCoins(20)) {
            triggerFeedback("NEED 20 COINS FOR HINT!", FeedbackType.INVALID)
            return
        }

        // Find unrevealed cell
        val unrevealedCells = mutableListOf<Pair<Int, Int>>()
        state.gridMatrix.forEachIndexed { r, row ->
            row.forEachIndexed { c, cell ->
                if (cell != null && !cell.isRevealed) {
                    unrevealedCells.add(r to c)
                }
            }
        }

        if (unrevealedCells.isEmpty()) return

        val (r, c) = unrevealedCells.random()
        val newMatrix = state.gridMatrix.map { row -> row.toMutableList() }.toMutableList()
        val targetCell = newMatrix[r][c]
        if (targetCell != null) {
            newMatrix[r][c] = targetCell.copy(isRevealed = true, isJustRevealed = true)
        }

        // Check if any word fully revealed as result
        val newSolvedWordIds = state.solvedWordIds.toMutableSet()
        level.words.forEach { word ->
            var allRevealed = true
            for (i in word.text.indices) {
                val wr = if (word.direction == WordDirection.VERTICAL) word.startRow + i else word.startRow
                val wc = if (word.direction == WordDirection.HORIZONTAL) word.startCol + i else word.startCol
                if (newMatrix[wr][wc]?.isRevealed != true) {
                    allRevealed = false
                    break
                }
            }
            if (allRevealed) newSolvedWordIds.add(word.id)
        }

        val isComplete = newSolvedWordIds.size == level.words.size
        if (isComplete) {
            repository.unlockNextLevel(level.id)
            soundManager.playVictory()
        } else {
            soundManager.playSuccess()
        }

        // Save progress
        repository.saveSolvedWordsForLevel(level.id, newSolvedWordIds)

        _uiState.update {
            it.copy(
                gridMatrix = newMatrix,
                solvedWordIds = newSolvedWordIds,
                coins = repository.coins.value,
                isLevelComplete = isComplete
            )
        }
    }

    fun shuffleWheel() {
        _uiState.update {
            it.copy(wheelLetters = it.wheelLetters.shuffled())
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }



    private fun triggerFeedback(msg: String, type: FeedbackType) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    feedbackMessage = msg,
                    feedbackType = type,
                    isLockInteraction = true
                )
            }
            delay(800) // Keep validation/color active for 800ms
            _uiState.update {
                it.copy(
                    feedbackType = FeedbackType.NONE,
                    currentFormedWord = "",
                    isLockInteraction = false
                )
            }
            delay(700) // Keep text feedback visible for 1500ms total
            if (_uiState.value.feedbackMessage == msg) {
                _uiState.update { it.copy(feedbackMessage = null) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
