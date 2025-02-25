package com.noobzsociety.smsgames.room.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class JsonConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun stringListToJSON(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> = json.decodeFromString(value)
}