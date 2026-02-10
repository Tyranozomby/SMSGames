package com.noobzsociety.smsgames.data.room.converters

import androidx.room.TypeConverter
import com.noobzsociety.smsgames.data.room.entities.ParameterDefinition
import kotlinx.serialization.json.Json
import org.json.JSONObject

class JsonConverters {
    @TypeConverter
    fun objectToString(json: JSONObject?): String? {
        return json?.toString()
    }

    @TypeConverter
    fun stringToJsonObject(value: String?): JSONObject? {
        if (value == null)
            return null
        return JSONObject(value)
    }

    @TypeConverter
    fun settingsToString(settings: Map<String, ParameterDefinition>): String {
        return Json.encodeToString(settings)
    }

    @TypeConverter
    fun stringToSettings(value: String): Map<String, ParameterDefinition> {
        return Json.decodeFromString(value)
    }
}