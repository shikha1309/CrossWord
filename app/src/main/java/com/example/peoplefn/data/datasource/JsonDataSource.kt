package com.example.peoplefn.data.datasource

import android.content.Context
import com.example.peoplefn.data.model.Level
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonDataSource(private val context: Context) {
    private val gson = Gson()

    fun loadLevels(): List<Level> {
        return try {
            val jsonString = context.assets.open("levels.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<Level>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
