package com.omidshojaee.repeatalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.omidshojaee.repeatalarm.data.local.AlarmDatabase
import com.omidshojaee.repeatalarm.data.repository.AlarmRepositoryImpl
import com.omidshojaee.repeatalarm.notification.NotificationHelper
import com.omidshojaee.repeatalarm.scheduler.AlarmSchedulerImpl
import com.omidshojaee.repeatalarm.ui.alarm.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ALARM_TRIGGER) return

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) {
            Log.e(TAG, "Received alarm intent without EXTRA_ALARM_ID")
            return
        }

        Log.d(TAG, "Alarm triggered for alarmId: $alarmId")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "RepeatAlarm:AlarmReceiverWakeLock"
        )
        wakeLock.acquire(10 * 1000L)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AlarmDatabase.getDatabase(context)
                val scheduler = AlarmSchedulerImpl(context)
                val repository = AlarmRepositoryImpl(database.alarmDao(), scheduler)

                val alarm = repository.getAlarmById(alarmId)
                if (alarm != null && alarm.isEnabled) {
                    // 1. Reschedule the next alarm if within end date limit
                    val nextTriggerTime = repository.rescheduleAlarm(alarm)
                    Log.d(TAG, "Rescheduled alarm $alarmId, next trigger time: $nextTriggerTime")

                    // 2. Post high-priority notification with full-screen intent
                    NotificationHelper.showAlarmNotification(context, alarm)

                    // 3. Directly launch full screen AlarmActivity for immediate user response
                    val alarmActivityIntent = Intent(context, AlarmActivity::class.java).apply {
                        putExtra(AlarmActivity.EXTRA_ALARM_ID, alarm.id)
                        putExtra(AlarmActivity.EXTRA_ALARM_TITLE, alarm.title)
                        putExtra(AlarmActivity.EXTRA_SOUND_URI, alarm.soundUri)
                        putExtra(AlarmActivity.EXTRA_VIBRATE, alarm.vibrate)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(alarmActivityIntent)
                } else {
                    Log.w(TAG, "Alarm $alarmId is null or disabled. Skipping ring.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm trigger for id $alarmId", e)
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.omidshojaee.repeatalarm.ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        private const val TAG = "AlarmReceiver"
    }
}
