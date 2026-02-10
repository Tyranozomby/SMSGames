package com.noobzsociety.smsgames.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "player_to_game",
    primaryKeys = ["player_id", "game_id"],
    foreignKeys = [
        ForeignKey(
            entity = RoomPlayer::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomGame::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["player_id"], unique = true),
        Index(value = ["game_id"]),
    ]
)
data class RoomPlayerToGame(
    @ColumnInfo(name = "player_id")
    val playerId: Long,

    @ColumnInfo(name = "game_id")
    val gameId: Long
)