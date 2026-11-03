package com.omidshojaee.repeatalarm.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

interface AudioPlayer {
    fun play(
        soundUriString: String?,
        enableVolumeRamp: Boolean = false,
        rampDurationMs: Long = 10000L
    )
    fun stop()
    val isPlaying: Boolean
}

/**
 * Handles audio playback for triggered alarms using MediaPlayer with USAGE_ALARM audio attributes.
 * Supports looping playback, volume ramp/gradual increase, fallback to default sounds, and safe cleanup.
 */
class AlarmSoundPlayer(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var volumeRampJob: Job? = null
    private var _isPlaying: Boolean = false

    override val isPlaying: Boolean
        get() = _isPlaying

    @Synchronized
    override fun play(
        soundUriString: String?,
        enableVolumeRamp: Boolean,
        rampDurationMs: Long
    ) {
        stop()

        val soundUri = resolveSoundUri(soundUriString)

        val player = MediaPlayer()
        try {
            setMediaPlayerSourceWithFallback(player, soundUri)

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            player.isLooping = true

            if (enableVolumeRamp) {
                player.setVolume(INITIAL_RAMP_VOLUME, INITIAL_RAMP_VOLUME)
            } else {
                player.setVolume(MAX_VOLUME, MAX_VOLUME)
            }

            player.prepare()
            player.start()
            mediaPlayer = player
            _isPlaying = true

            if (enableVolumeRamp) {
                startVolumeRamp(player, rampDurationMs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio playback with soundUri: $soundUri", e)
            try {
                player.release()
            } catch (_: Exception) {}
            _isPlaying = false
            tryFallbackPlayback(enableVolumeRamp, rampDurationMs)
        }
    }

    private fun resolveSoundUri(soundUriString: String?): Uri {
        return if (!soundUriString.isNullOrBlank()) {
            try {
                Uri.parse(soundUriString)
            } catch (e: Exception) {
                getDefaultAlarmUri()
            }
        } else {
            getDefaultAlarmUri()
        }
    }

    private fun getDefaultAlarmUri(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI
            ?: Uri.EMPTY
    }

    private fun setMediaPlayerSourceWithFallback(player: MediaPlayer, targetUri: Uri) {
        try {
            player.setDataSource(context, targetUri)
        } catch (e: Exception) {
            Log.w(TAG, "Failed setting data source for $targetUri, attempting default alarm URI", e)
            val defaultUri = getDefaultAlarmUri()
            if (targetUri != defaultUri && defaultUri != Uri.EMPTY) {
                player.setDataSource(context, defaultUri)
            } else {
                throw e
            }
        }
    }

    private fun tryFallbackPlayback(enableVolumeRamp: Boolean, rampDurationMs: Long) {
        val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI
            ?: return

        val fallbackPlayer = MediaPlayer()
        try {
            fallbackPlayer.setDataSource(context, fallbackUri)
            fallbackPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            fallbackPlayer.isLooping = true
            if (enableVolumeRamp) {
                fallbackPlayer.setVolume(INITIAL_RAMP_VOLUME, INITIAL_RAMP_VOLUME)
            } else {
                fallbackPlayer.setVolume(MAX_VOLUME, MAX_VOLUME)
            }
            fallbackPlayer.prepare()
            fallbackPlayer.start()
            mediaPlayer = fallbackPlayer
            _isPlaying = true

            if (enableVolumeRamp) {
                startVolumeRamp(fallbackPlayer, rampDurationMs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback audio playback failed completely", e)
            try {
                fallbackPlayer.release()
            } catch (_: Exception) {}
            _isPlaying = false
        }
    }

    private fun startVolumeRamp(player: MediaPlayer, durationMs: Long) {
        volumeRampJob?.cancel()
        volumeRampJob = coroutineScope.launch {
            val steps = 20
            val intervalMs = (durationMs / steps).coerceAtLeast(100L)

            for (step in 1..steps) {
                delay(intervalMs)
                if (!isActive || mediaPlayer != player || !_isPlaying) break
                val currentVolume = INITIAL_RAMP_VOLUME + (MAX_VOLUME - INITIAL_RAMP_VOLUME) * (step.toFloat() / steps)
                try {
                    player.setVolume(currentVolume, currentVolume)
                } catch (e: Exception) {
                    Log.w(TAG, "Error adjusting ramp volume", e)
                    break
                }
            }
        }
    }

    @Synchronized
    override fun stop() {
        volumeRampJob?.cancel()
        volumeRampJob = null

        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping MediaPlayer", e)
            } finally {
                try {
                    player.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing MediaPlayer", e)
                }
            }
        }
        mediaPlayer = null
        _isPlaying = false
    }

    companion object {
        private const val TAG = "AlarmSoundPlayer"
        private const val INITIAL_RAMP_VOLUME = 0.05f
        private const val MAX_VOLUME = 1.0f
    }
}
