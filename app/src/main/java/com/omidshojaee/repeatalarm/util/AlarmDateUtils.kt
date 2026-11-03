package com.omidshojaee.repeatalarm.util

import android.content.Context
import com.omidshojaee.repeatalarm.R
import com.omidshojaee.repeatalarm.data.model.RepeatUnit
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.toPersianDigits(): String {
    return this.map { ch ->
        when (ch) {
            in '0'..'9' -> (ch - '0' + '۰'.code).toChar()
            else -> ch
        }
    }.joinToString("")
}

fun String.toEnglishDigits(): String {
    return this.map { ch ->
        when (ch) {
            in '۰'..'۹' -> (ch - '۰' + '0'.code).toChar()
            else -> ch
        }
    }.joinToString("")
}

fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
fun Long.toPersianDigits(): String = this.toString().toPersianDigits()

object AlarmDateUtils {

    fun formatTime(startTimeMillis: Long): String {
        val totalMinutes = (startTimeMillis / (1000 * 60)).toInt()
        val hour = ((totalMinutes / 60) % 24 + 24) % 24
        val minute = ((totalMinutes % 60) + 60) % 60
        val time = LocalTime.of(hour, minute)
        return time.format(DateTimeFormatter.ofPattern("HH:mm")).toPersianDigits()
    }

    fun formatDate(epochMillis: Long): String {
        return PersianCalendarHelper.formatDate(epochMillis)
    }

    fun formatDateTime(epochMillis: Long): String {
        val time = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
            .toPersianDigits()
        return "${PersianCalendarHelper.formatDate(epochMillis)}، $time"
    }

    fun formatRepeatInterval(context: Context, value: Int, unit: RepeatUnit): String {
        val unitStr = when (unit) {
            RepeatUnit.MINUTES -> context.getString(R.string.unit_minutes)
            RepeatUnit.HOURS -> context.getString(R.string.unit_hours)
            RepeatUnit.DAYS -> context.getString(R.string.unit_days)
            RepeatUnit.WEEKS -> context.getString(R.string.unit_weeks)
            RepeatUnit.MONTHS -> context.getString(R.string.unit_months)
        }
        return context.getString(R.string.every_x_y, value.toPersianDigits(), unitStr)
    }

    fun formatRepeatInterval(value: Int, unit: RepeatUnit): String {
        val unitName = when (unit) {
            RepeatUnit.MINUTES -> if (value == 1) "minute" else "minutes"
            RepeatUnit.HOURS -> if (value == 1) "hour" else "hours"
            RepeatUnit.DAYS -> if (value == 1) "day" else "days"
            RepeatUnit.WEEKS -> if (value == 1) "week" else "weeks"
            RepeatUnit.MONTHS -> if (value == 1) "month" else "months"
        }
        return "Every ${value.toPersianDigits()} $unitName"
    }

    fun getStartOfTodayMillis(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun timeToMillis(hour: Int, minute: Int): Long {
        return ((hour * 60 + minute) * 60 * 1000L)
    }

    fun millisToHourMinute(startTimeMillis: Long): Pair<Int, Int> {
        val totalMinutes = (startTimeMillis / (1000 * 60)).toInt()
        val hour = ((totalMinutes / 60) % 24 + 24) % 24
        val minute = ((totalMinutes % 60) + 60) % 60
        return Pair(hour, minute)
    }
}
