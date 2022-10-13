package com.tyrano.smsgames.converters

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class JsonConverter {

    @TypeConverter
    fun fromJsonObj(json: JSONObject?): String? {
        return json?.toString()
    }

    @TypeConverter
    fun toJsonObj(value: String?): JSONObject? {
        if (value == null)
            return null
        return JSONObject(value)
    }

    @TypeConverter
    fun fromJsonArr(jsonArray: JSONArray): String {
        return jsonArray.toString()
    }

    @TypeConverter
    fun toJsonArr(value: String): JSONArray {
        return JSONArray(value)
    }
}