package com.tyrano.smsgames.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

@Entity(
    tableName = "game",
    foreignKeys = [ForeignKey(
        entity = GamemodeEntity::class,
        parentColumns = ["id"],
        childColumns = ["gamemode_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GameEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "gamemode_id")
    val gamemodeId: Long,

    @ColumnInfo(name = "owner_id")
    val ownerId: Long,

    val start: Instant? = null,

    val data: JSONObject? = null,

    val settings: JSONObject = JSONObject(),

    @ColumnInfo(name = "players_order")
    val playersOrder: JSONArray = JSONArray()
)