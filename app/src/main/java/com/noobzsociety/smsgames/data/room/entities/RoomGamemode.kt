package com.noobzsociety.smsgames.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Entity(
    tableName = "gamemode",
)
data class RoomGamemode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val rules: String,
    val code: String,

    val parameters: Parameters = emptyMap(),
    val enabled: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Clock.System.now(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Clock.System.now()
)

typealias Parameters = Map<String, ParameterDefinition>

fun Parameters.toJson(): JSONObject = JSONObject(this.mapValues { (_, value) -> value.default })

@Serializable
sealed class ParameterDefinition {
    abstract val definition: kotlin.String
    abstract val default: Any

    @Serializable
    @SerialName("string")
    data class String(
        override val definition: kotlin.String,
        override val default: kotlin.String,
        val minLength: kotlin.Int = 0,
        val maxLength: kotlin.Int? = null,
    ) : ParameterDefinition()

    @Serializable
    @SerialName("int")
    data class Int(
        override val definition: kotlin.String,
        override val default: kotlin.Int,
        val min: kotlin.Int? = null,
        val max: kotlin.Int? = null
    ) : ParameterDefinition()

    @Serializable
    @SerialName("float")
    data class Float(
        override val definition: kotlin.String,
        override val default: kotlin.Float,
        val min: kotlin.Float? = null,
        val max: kotlin.Float? = null
    ) : ParameterDefinition()

    @Serializable
    @SerialName("boolean")
    data class Boolean(
        override val definition: kotlin.String,
        override val default: kotlin.Boolean
    ) : ParameterDefinition()
}
