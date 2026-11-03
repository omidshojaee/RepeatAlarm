package com.omidshojaee.repeatalarm.data.repository

import com.omidshojaee.repeatalarm.data.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    fun getAlarmByIdFlow(id: Long): Flow<Alarm?>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun getActiveAlarms(): List<Alarm>
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun deleteAlarmById(id: Long)
    suspend fun toggleAlarmEnabled(id: Long, enabled: Boolean)
    suspend fun rescheduleAlarm(alarm: Alarm): Long?
    suspend fun rescheduleAllActiveAlarms()
}
