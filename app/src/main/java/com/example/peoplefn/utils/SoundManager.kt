package com.example.peoplefn.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var successSoundId = 0
    private var errorSoundId = 0
    private var victorySoundId = 0
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        val pool = soundPool ?: return
        // Dynamically fetch resource IDs so it compiles even if assets are missing
        val successId = context.resources.getIdentifier("sfx_success", "raw", context.packageName)
        val errorId = context.resources.getIdentifier("sfx_error", "raw", context.packageName)
        val victoryId = context.resources.getIdentifier("sfx_victory", "raw", context.packageName)

        if (successId != 0) {
            successSoundId = pool.load(context, successId, 1)
        } else {
            Log.w("SoundManager", "sfx_success sound resource not found")
        }

        if (errorId != 0) {
            errorSoundId = pool.load(context, errorId, 1)
        } else {
            Log.w("SoundManager", "sfx_error sound resource not found")
        }

        if (victoryId != 0) {
            victorySoundId = pool.load(context, victoryId, 1)
        } else {
            Log.w("SoundManager", "sfx_victory sound resource not found")
        }
    }

    fun playSuccess() {
        if (successSoundId != 0) {
            soundPool?.play(successSoundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Fallback system sound
            audioManager?.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP)
        }
    }

    fun playError() {
        if (errorSoundId != 0) {
            soundPool?.play(errorSoundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Fallback system sound
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        }
    }

    fun playVictory() {
        if (victorySoundId != 0) {
            soundPool?.play(victorySoundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Fallback system sound
            audioManager?.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}


