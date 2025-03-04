package com.noobzsociety.smsgames.room.converters

import androidx.room.TypeConverter
import org.json.JSONArray
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
    fun fromArrayToString(jsonArray: JSONArray): String {
        return jsonArray.toString()
    }

    @TypeConverter
    fun stringToArray(value: String): JSONArray {
        return JSONArray(value)
    }
}