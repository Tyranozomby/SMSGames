package com.tyrano.smsgames.entities

import androidx.room.*
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
    )],
    indices = [
        Index(name = "gamemode_id_index", value = ["gamemode_id"])
    ]
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