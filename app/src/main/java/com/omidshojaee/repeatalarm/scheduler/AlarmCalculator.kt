package com.omidshojaee.repeatalarm.scheduler

import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.data.model.RepeatUnit
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmCalculator {

    /**
     * Calculates the next trigger time in epoch milliseconds for the given alarm.
     * Returns null if the alarm has an end date and the next calculated trigger time is past the end date.
     */
    fun calculateNextTriggerTime(alarm: Alarm, fromTimeMillis: Long = System.currentTimeMillis()): Long? {
        val zoneId = ZoneId.systemDefault()

        // Convert startDate (epoch millis) to LocalDate
        val startDateInstant = Instant.ofEpochMilli(alarm.startDate)
        val startDate = startDateInstant.atZone(zoneId).toLocalDate()

        val startTime = LocalTime.of(alarm.hour, alarm.minute)
        var startDateTime = LocalDateTime.of(startDate, startTime)

        var candidateMillis = startDateTime.atZone(zoneId).toInstant().toEpochMilli()

        // If candidate time is in the future relative to fromTimeMillis, it's the next trigger time
        if (candidateMillis <= fromTimeMillis) {
            val repeatValue = alarm.repeatIntervalValue.coerceAtLeast(1)
            startDateTime = advanceDateTimePast(startDateTime, alarm.repeatIntervalUnit, repeatValue, fromTimeMillis, zoneId)
            candidateMillis = startDateTime.atZone(zoneId).toInstant().toEpochMilli()
        }

        // Now check candidateMillis against endDate if specified
        if (alarm.endDate != null) {
            val endDateInstant = Instant.ofEpochMilli(alarm.endDate)
            val endDate = endDateInstant.atZone(zoneId).toLocalDate()
            val endOfLimitMillis = LocalDateTime.of(endDate, LocalTime.MAX)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()

            if (candidateMillis > endOfLimitMillis) {
                return null
            }
        }

        return candidateMillis
    }

    private fun advanceDateTimePast(
        startDateTime: LocalDateTime,
        unit: RepeatUnit,
        value: Int,
        fromTimeMillis: Long,
        zoneId: ZoneId
    ): LocalDateTime {
        var current = startDateTime
        val currentMillis = current.atZone(zoneId).toInstant().toEpochMilli()

        when (unit) {
            RepeatUnit.MINUTES -> {
                val intervalMillis = value * 60 * 1000L
                val elapsed = fromTimeMillis - currentMillis
                val steps = (elapsed / intervalMillis) + 1
                return current.plusMinutes(steps * value)
            }
            RepeatUnit.HOURS -> {
                val intervalMillis = value * 60 * 60 * 1000L
                val elapsed = fromTimeMillis - currentMillis
                val steps = (elapsed / intervalMillis) + 1
                return current.plusHours(steps * value)
            }
            RepeatUnit.DAYS -> {
                val intervalMillis = value * 24 * 60 * 60 * 1000L
                val elapsed = fromTimeMillis - currentMillis
                val steps = (elapsed / intervalMillis).coerceAtLeast(0)
                current = current.plusDays(steps * value)
                while (current.atZone(zoneId).toInstant().toEpochMilli() <= fromTimeMillis) {
                    current = current.plusDays(value.toLong())
                }
                return current
            }
            RepeatUnit.WEEKS -> {
                val intervalMillis = value * 7 * 24 * 60 * 60 * 1000L
                val elapsed = fromTimeMillis - currentMillis
                val steps = (elapsed / intervalMillis).coerceAtLeast(0)
                current = current.plusWeeks(steps * value)
                while (current.atZone(zoneId).toInstant().toEpochMilli() <= fromTimeMillis) {
                    current = current.plusWeeks(value.toLong())
                }
                return current
            }
            RepeatUnit.MONTHS -> {
                val approxMonthMillis = value * 30 * 24 * 60 * 60 * 1000L
                val elapsed = fromTimeMillis - currentMillis
                val steps = (elapsed / approxMonthMillis).coerceAtLeast(0)
                current = current.plusMonths(steps * value)
                while (current.atZone(zoneId).toInstant().toEpochMilli() <= fromTimeMillis) {
                    current = current.plusMonths(value.toLong())
                }
                return current
            }
        }
    }
}
