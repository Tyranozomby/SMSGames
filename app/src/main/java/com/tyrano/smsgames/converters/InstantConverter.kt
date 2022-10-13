package com.tyrano.smsgames.converters

import androidx.room.TypeConverter
import java.time.Instant

class InstantConverter {

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(long: Long?): Instant? {
        if (long == null)
            return null
        return Instant.ofEpochMilli(long)
    }
}