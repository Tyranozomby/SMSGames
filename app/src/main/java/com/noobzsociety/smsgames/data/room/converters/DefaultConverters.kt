package com.noobzsociety.smsgames.data.room.converters

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class DefaultConverters {
    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}