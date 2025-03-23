package com.noobzsociety.smsgames.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

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
    val id: Long = 0,

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