package com.omidshojaee.repeatalarm.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.omidshojaee.repeatalarm.MainActivity
import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.receiver.AlarmReceiver

class AlarmSchedulerImpl(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm): Long? {
        if (!alarm.isEnabled) {
            cancel(alarm)
            return null
        }

        val nextTriggerTime = AlarmCalculator.calculateNextTriggerTime(alarm, System.currentTimeMillis())
        if (nextTriggerTime == null) {
            Log.d(TAG, "Alarm ${alarm.id} is past end date limit or invalid. Canceling schedule.")
            cancel(alarm)
            return null
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(nextTriggerTime, showPendingIntent),
                    pendingIntent
                )
                Log.d(TAG, "Scheduled alarm ${alarm.id} exact at $nextTriggerTime via setAlarmClock")
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled alarm ${alarm.id} non-exact at $nextTriggerTime (canScheduleExactAlarms = false)")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm ${alarm.id}, falling back to setAndAllowWhileIdle", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }

        return nextTriggerTime
    }

    override fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Canceled alarm ${alarm.id}")
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    companion object {
        private const val TAG = "AlarmSchedulerImpl"
    }
}
