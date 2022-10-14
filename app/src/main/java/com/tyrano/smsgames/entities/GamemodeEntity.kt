package com.tyrano.smsgames.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "gamemode")
data class GamemodeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,

    val enabled: Boolean,

    val code: String,

    val rules: String,

    val settings: JSONObject
)