package com.noobzsociety.smsgames.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.json.JSONObject

@Entity(
    tableName = "gamemode",
)
data class RoomGamemode(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

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

@Entity(
    tableName = "game",
    foreignKeys = [
        ForeignKey(
            entity = RoomGamemode::class,
            parentColumns = ["id"],
            childColumns = ["gamemode_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "gamemode_id_index", value = ["gamemode_id"])
    ]
)
data class RoomGame(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "gamemode_id")
    val gamemodeId: Int,

    @ColumnInfo(name = "owner_id")
    val ownerId: Int,

    val start: Instant? = null,
    val data: JSONObject? = null,
    val settings: JSONObject = JSONObject(),

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Clock.System.now()
)

@Entity(
    tableName = "player",
    indices = [
        Index(
            name = "phone_number_index",
            unique = true,
            value = ["phone_number"]
        ),
    ]
)
data class RoomPlayer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    @ColumnInfo(name = "is_tester")
    val isTester: Boolean = false,

    @ColumnInfo(name = "is_master")
    val isMaster: Boolean = false,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),
)

@Entity(
    tableName = "player_game",
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
    ]
)
data class RoomPlayerGame(
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "game_id")
    val gameId: Int
)