package com.omidshojaee.repeatalarm.data.repository

import com.omidshojaee.repeatalarm.data.local.AlarmDao
import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.scheduler.AlarmCalculator
import com.omidshojaee.repeatalarm.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms()
    }

    override fun getAlarmByIdFlow(id: Long): Flow<Alarm?> {
        return alarmDao.getAlarmByIdFlow(id)
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)
    }

    override suspend fun getActiveAlarms(): List<Alarm> {
        return alarmDao.getActiveAlarms()
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        val calculatedNextTime = if (alarm.isEnabled) {
            AlarmCalculator.calculateNextTriggerTime(alarm, System.currentTimeMillis())
        } else {
            null
        }

        val alarmToInsert = alarm.copy(
            isEnabled = if (calculatedNextTime != null) alarm.isEnabled else (alarm.isEnabled && alarm.endDate == null),
            nextTriggerTime = calculatedNextTime
        )

        val newId = alarmDao.insertAlarm(alarmToInsert)
        val finalAlarm = alarmToInsert.copy(id = newId)

        if (finalAlarm.isEnabled && calculatedNextTime != null) {
            alarmScheduler.schedule(finalAlarm)
        }
        return newId
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        val calculatedNextTime = if (alarm.isEnabled) {
            AlarmCalculator.calculateNextTriggerTime(alarm, System.currentTimeMillis())
        } else {
            null
        }

        val alarmToUpdate = alarm.copy(
            isEnabled = if (calculatedNextTime == null && alarm.isEnabled && alarm.endDate != null) false else alarm.isEnabled,
            nextTriggerTime = calculatedNextTime
        )

        alarmDao.updateAlarm(alarmToUpdate)

        if (alarmToUpdate.isEnabled && calculatedNextTime != null) {
            alarmScheduler.schedule(alarmToUpdate)
        } else {
            alarmScheduler.cancel(alarmToUpdate)
        }
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmScheduler.cancel(alarm)
        alarmDao.deleteAlarm(alarm)
    }

    override suspend fun deleteAlarmById(id: Long) {
        val alarm = alarmDao.getAlarmById(id)
        if (alarm != null) {
            alarmScheduler.cancel(alarm)
            alarmDao.deleteAlarmById(id)
        }
    }

    override suspend fun toggleAlarmEnabled(id: Long, enabled: Boolean) {
        val alarm = alarmDao.getAlarmById(id) ?: return
        val updatedAlarm = alarm.copy(isEnabled = enabled)
        updateAlarm(updatedAlarm)
    }

    override suspend fun rescheduleAlarm(alarm: Alarm): Long? {
        val calculatedNextTime = AlarmCalculator.calculateNextTriggerTime(alarm, System.currentTimeMillis())
        if (calculatedNextTime != null) {
            val scheduledAlarm = alarm.copy(nextTriggerTime = calculatedNextTime, isEnabled = true)
            alarmDao.updateAlarm(scheduledAlarm)
            alarmScheduler.schedule(scheduledAlarm)
            return calculatedNextTime
        } else {
            val expiredAlarm = alarm.copy(nextTriggerTime = null, isEnabled = false)
            alarmDao.updateAlarm(expiredAlarm)
            alarmScheduler.cancel(expiredAlarm)
            return null
        }
    }

    override suspend fun rescheduleAllActiveAlarms() {
        val activeAlarms = alarmDao.getActiveAlarms()
        for (alarm in activeAlarms) {
            rescheduleAlarm(alarm)
        }
    }
}
