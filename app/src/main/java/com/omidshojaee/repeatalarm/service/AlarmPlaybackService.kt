package com.omidshojaee.repeatalarm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CombinedVibration
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.omidshojaee.repeatalarm.notification.NotificationHelper
import com.omidshojaee.repeatalarm.sound.AlarmSoundPlayer

class AlarmPlaybackService : Service() {

    private var alarmSoundPlayer: AlarmSoundPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) {
            Log.e(TAG, "Started without a valid alarm id")
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val title = intent.getStringExtra(EXTRA_ALARM_TITLE).orEmpty()
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)
        val shouldVibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true)
        val notification = NotificationHelper.buildAlarmNotification(
            this,
            alarmId,
            title,
            soundUri,
            shouldVibrate
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                alarmId.toInt(),
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(alarmId.toInt(), notification)
        }

        alarmSoundPlayer?.stop()
        alarmSoundPlayer = AlarmSoundPlayer(this).also {
            it.play(soundUri, enableVolumeRamp = true)
        }
        stopVibration()
        if (shouldVibrate) startVibration()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        alarmSoundPlayer?.stop()
        alarmSoundPlayer = null
        stopVibration()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val pattern = VibrationEffect.createWaveform(longArrayOf(0, 500, 500, 500), 0)
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.vibrate(CombinedVibration.createParallel(pattern))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500, 500), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 500, 500), 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alarm vibration", e)
        }
    }

    private fun stopVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.cancel()
            } else {
                @Suppress("DEPRECATION")
                vibrator?.cancel()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop alarm vibration", e)
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "service_alarm_id"
        const val EXTRA_ALARM_TITLE = "service_alarm_title"
        const val EXTRA_SOUND_URI = "service_sound_uri"
        const val EXTRA_VIBRATE = "service_vibrate"
        private const val TAG = "AlarmPlaybackService"
    }
}
