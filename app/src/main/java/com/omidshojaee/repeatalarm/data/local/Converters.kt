package com.omidshojaee.repeatalarm.data.local

import androidx.room.TypeConverter
import com.omidshojaee.repeatalarm.data.model.RepeatUnit

class Converters {
    @TypeConverter
    fun fromRepeatUnit(unit: RepeatUnit?): String {
        return unit?.name ?: RepeatUnit.DAYS.name
    }

    @TypeConverter
    fun toRepeatUnit(value: String?): RepeatUnit {
        return try {
            if (value != null) RepeatUnit.valueOf(value) else RepeatUnit.DAYS
        } catch (e: Exception) {
            RepeatUnit.DAYS
        }
    }
}
