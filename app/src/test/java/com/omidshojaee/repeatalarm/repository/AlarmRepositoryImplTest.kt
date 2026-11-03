package com.omidshojaee.repeatalarm.repository

import com.omidshojaee.repeatalarm.data.local.AlarmDao
import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.data.model.RepeatUnit
import com.omidshojaee.repeatalarm.data.repository.AlarmRepositoryImpl
import com.omidshojaee.repeatalarm.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class FakeAlarmDao : AlarmDao {
    private val alarmsMap = mutableMapOf<Long, Alarm>()
    private var currentId = 0L

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return flowOf(alarmsMap.values.toList())
    }

    override suspend fun getActiveAlarms(): List<Alarm> {
        return alarmsMap.values.filter { it.isEnabled }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmsMap[id]
    }

    override fun getAlarmByIdFlow(id: Long): Flow<Alarm?> {
        return flowOf(alarmsMap[id])
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        val id = if (alarm.id == 0L) ++currentId else alarm.id
        val savedAlarm = alarm.copy(id = id)
        alarmsMap[id] = savedAlarm
        return id
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmsMap[alarm.id] = alarm
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmsMap.remove(alarm.id)
    }

    override suspend fun deleteAlarmById(id: Long) {
        alarmsMap.remove(id)
    }
}

class FakeAlarmScheduler : AlarmScheduler {
    val scheduledAlarms = mutableListOf<Alarm>()
    val canceledAlarms = mutableListOf<Alarm>()

    override fun schedule(alarm: Alarm): Long? {
        scheduledAlarms.add(alarm)
        return alarm.nextTriggerTime
    }

    override fun cancel(alarm: Alarm) {
        canceledAlarms.add(alarm)
    }
}

class AlarmRepositoryImplTest {

    private val fakeDao = FakeAlarmDao()
    private val fakeScheduler = FakeAlarmScheduler()
    private val repository = AlarmRepositoryImpl(fakeDao, fakeScheduler)
    private val zoneId = ZoneId.systemDefault()

    @Test
    fun `insertAlarm calculates next trigger time and schedules alarm`() = runBlocking {
        val tomorrow = LocalDate.now().plusDays(1)
        val startDateMillis = tomorrow.atStartOfDay(zoneId).toInstant().toEpochMilli()

        val alarm = Alarm(
            title = "Test Alarm",
            startDate = startDateMillis,
            startTime = (7 * 60 + 30) * 60 * 1000L,
            repeatIntervalValue = 1,
            repeatIntervalUnit = RepeatUnit.DAYS,
            isEnabled = true
        )

        val id = repository.insertAlarm(alarm)
        val saved = repository.getAlarmById(id)

        assertNotNull(saved)
        assertNotNull(saved?.nextTriggerTime)
        assertTrue(saved!!.isEnabled)
        assertEquals(1, fakeScheduler.scheduledAlarms.size)
    }

    @Test
    fun `toggleAlarmEnabled disables alarm and cancels schedule`() = runBlocking {
        val tomorrow = LocalDate.now().plusDays(1)
        val startDateMillis = tomorrow.atStartOfDay(zoneId).toInstant().toEpochMilli()

        val alarm = Alarm(
            title = "Toggle Test",
            startDate = startDateMillis,
            startTime = (8 * 60) * 60 * 1000L,
            isEnabled = true
        )

        val id = repository.insertAlarm(alarm)
        repository.toggleAlarmEnabled(id, false)

        val updated = repository.getAlarmById(id)
        assertNotNull(updated)
        assertFalse(updated!!.isEnabled)
        assertTrue(fakeScheduler.canceledAlarms.any { it.id == id })
    }
}
