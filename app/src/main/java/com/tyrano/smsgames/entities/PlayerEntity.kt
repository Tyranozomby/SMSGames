package com.tyrano.smsgames.entities

import androidx.room.*

@Entity(
    tableName = "player",
    indices = [Index(
        name = "phone_number_index",
        unique = true,
        value = ["phone_number"]
    ), Index(name = "game_id_index", value = ["game_id"])],
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["game_id"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class PlayerEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,

    val tester: Boolean = false,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "game_id")
    val gameId: Long?
)