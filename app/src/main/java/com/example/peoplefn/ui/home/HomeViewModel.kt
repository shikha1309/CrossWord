package com.example.peoplefn.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.peoplefn.data.repository.GameRepository
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    val unlockedLevelMax: StateFlow<Int> = repository.unlockedLevelMax
    val coins: StateFlow<Int> = repository.coins

    fun getTotalLevelsCount(): Int {
        return repository.getAllLevels().size
    }
}
