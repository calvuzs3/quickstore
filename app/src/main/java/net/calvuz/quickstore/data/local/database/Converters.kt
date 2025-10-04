package net.calvuz.quickstore.data.local.database

import androidx.room.TypeConverter
import net.calvuz.quickstore.data.local.entity.MovementType

/**
 * Type Converters per Room Database
 */
class Converters {

    @TypeConverter
    fun fromMovementType(type: MovementType): String {
        return type.name
    }

    @TypeConverter
    fun toMovementType(value: String): MovementType {
        return MovementType.valueOf(value)
    }
}