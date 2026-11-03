package com.omidshojaee.repeatalarm.scheduler

import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.data.model.RepeatUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmCalculatorTest {

    private val zoneId = ZoneId.systemDefault()

    @Test
    fun `calculateNextTriggerTime returns startDateTime when startDateTime is in future`() {
        val today = LocalDate.of(2025, 5, 10)
        val startDateMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val alarm = Alarm(
            id = 1,
            title = "Test Alarm",
            startDate = startDateMillis,
            startTime = (8 * 60 + 30) * 60 * 1000L,
            repeatIntervalValue = 1,
            repeatIntervalUnit = RepeatUnit.DAYS
        )

        val fromTime = LocalDateTime.of(today, LocalTime.of(7, 0))
            .atZone(zoneId).toInstant().toEpochMilli()

        val nextTrigger = AlarmCalculator.calculateNextTriggerTime(alarm, fromTime)

        val expectedNextTrigger = LocalDateTime.of(today, LocalTime.of(8, 30))
            .atZone(zoneId).toInstant().toEpochMilli()

        assertNotNull(nextTrigger)
        assertEquals(expectedNextTrigger, nextTrigger)
    }

    @Test
    fun `calculateNextTriggerTime advances by repeat interval in minutes`() {
        val today = LocalDate.of(2025, 5, 10)
        val startDateMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val alarm = Alarm(
            id = 1,
            title = "Interval Alarm",
            startDate = startDateMillis,
            startTime = (8 * 60) * 60 * 1000L,
            repeatIntervalValue = 15,
            repeatIntervalUnit = RepeatUnit.MINUTES
        )

        val fromTime = LocalDateTime.of(today, LocalTime.of(8, 5))
            .atZone(zoneId).toInstant().toEpochMilli()

        val nextTrigger = AlarmCalculator.calculateNextTriggerTime(alarm, fromTime)

        val expectedNextTrigger = LocalDateTime.of(today, LocalTime.of(8, 15))
            .atZone(zoneId).toInstant().toEpochMilli()

        assertNotNull(nextTrigger)
        assertEquals(expectedNextTrigger, nextTrigger)
    }

    @Test
    fun `calculateNextTriggerTime advances by repeat interval in days`() {
        val startDate = LocalDate.of(2025, 5, 1)
        val startDateMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val alarm = Alarm(
            id = 1,
            title = "Daily Alarm",
            startDate = startDateMillis,
            startTime = (9 * 60) * 60 * 1000L,
            repeatIntervalValue = 2,
            repeatIntervalUnit = RepeatUnit.DAYS
        )

        val fromTime = LocalDateTime.of(2025, 5, 2, 10, 0)
            .atZone(zoneId).toInstant().toEpochMilli()

        val nextTrigger = AlarmCalculator.calculateNextTriggerTime(alarm, fromTime)

        val expectedNextTrigger = LocalDateTime.of(2025, 5, 3, 9, 0)
            .atZone(zoneId).toInstant().toEpochMilli()

        assertNotNull(nextTrigger)
        assertEquals(expectedNextTrigger, nextTrigger)
    }

    @Test
    fun `calculateNextTriggerTime returns null when next trigger exceeds endDate`() {
        val startDate = LocalDate.of(2025, 5, 1)
        val endDate = LocalDate.of(2025, 5, 5)
        val startDateMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endDateMillis = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

        val alarm = Alarm(
            id = 1,
            title = "Expiring Alarm",
            startDate = startDateMillis,
            startTime = (10 * 60) * 60 * 1000L,
            repeatIntervalValue = 1,
            repeatIntervalUnit = RepeatUnit.DAYS,
            endDate = endDateMillis
        )

        val fromTime = LocalDateTime.of(2025, 5, 5, 11, 0)
            .atZone(zoneId).toInstant().toEpochMilli()

        val nextTrigger = AlarmCalculator.calculateNextTriggerTime(alarm, fromTime)

        assertNull(nextTrigger)
    }

    @Test
    fun `calculateNextTriggerTime returns trigger on endDate if trigger is within end of day`() {
        val startDate = LocalDate.of(2025, 5, 1)
        val endDate = LocalDate.of(2025, 5, 5)
        val startDateMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endDateMillis = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

        val alarm = Alarm(
            id = 1,
            title = "Expiring Alarm",
            startDate = startDateMillis,
            startTime = (10 * 60) * 60 * 1000L,
            repeatIntervalValue = 1,
            repeatIntervalUnit = RepeatUnit.DAYS,
            endDate = endDateMillis
        )

        val fromTime = LocalDateTime.of(2025, 5, 4, 11, 0)
            .atZone(zoneId).toInstant().toEpochMilli()

        val nextTrigger = AlarmCalculator.calculateNextTriggerTime(alarm, fromTime)

        val expectedNextTrigger = LocalDateTime.of(2025, 5, 5, 10, 0)
            .atZone(zoneId).toInstant().toEpochMilli()

        assertNotNull(nextTrigger)
        assertEquals(expectedNextTrigger, nextTrigger)
    }
}
