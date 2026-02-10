package com.noobzsociety.smsgames.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.json.JSONObject

@Entity(
    tableName = "game",
    foreignKeys = [
        ForeignKey(
            entity = RoomGamemode::class,
            parentColumns = ["id"],
            childColumns = ["gamemode_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomPlayer::class,
            parentColumns = ["id"],
            childColumns = ["owner_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "gamemode_id_index", value = ["gamemode_id"]),
        Index(name = "owner_id_index", value = ["owner_id"])
    ]
)
data class RoomGame(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "gamemode_id")
    val gamemodeId: Long,

    @ColumnInfo(name = "owner_id")
    val ownerId: Long,

    val parameters: JSONObject,
    val data: JSONObject? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Clock.System.now()
)