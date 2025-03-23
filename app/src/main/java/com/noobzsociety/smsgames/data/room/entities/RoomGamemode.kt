package com.noobzsociety.smsgames.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(
    tableName = "gamemode",
)
data class RoomGamemode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val rules: String,
    val code: String,

    @ColumnInfo(name = "min_players")
    val minPlayers: Int,
    @ColumnInfo(name = "max_players")
    val maxPlayers: Int,

//    val settings: JSONObject,
    val enabled: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Clock.System.now()
)