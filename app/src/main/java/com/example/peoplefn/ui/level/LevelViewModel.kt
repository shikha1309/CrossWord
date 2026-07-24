package com.example.peoplefn.ui.level

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.peoplefn.data.model.Level
import com.example.peoplefn.data.repository.GameRepository
import kotlinx.coroutines.flow.StateFlow

class LevelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    val unlockedLevelMax: StateFlow<Int> = repository.unlockedLevelMax

    fun getLevels(): List<Level> {
        return repository.getAllLevels()
    }
}
