package com.noobzsociety.smsgames.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game")
data class RoomGame(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)