package com.omidshojaee.repeatalarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startDate: Long, // Epoch millis of start date (local date at 00:00:00)
    val startTime: Long, // Milliseconds from midnight (e.g. (hour * 60 + min) * 60 * 1000)
    val repeatIntervalValue: Int = 1,
    val repeatIntervalUnit: RepeatUnit = RepeatUnit.DAYS,
    val endDate: Long? = null, // Epoch millis limit (end date)
    val soundUri: String = "",
    val soundTitle: String = "Default Alarm",
    val isEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val nextTriggerTime: Long? = null
) {
    val repeatInterval: RepeatInterval
        get() = RepeatInterval(repeatIntervalValue, repeatIntervalUnit)

    val hour: Int
        get() = ((startTime / (1000 * 60 * 60)) % 24).toInt()

    val minute: Int
        get() = ((startTime / (1000 * 60)) % 60).toInt()
}
