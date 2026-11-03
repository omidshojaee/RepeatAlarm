package com.omidshojaee.repeatalarm.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAudioPlayer : AudioPlayer {
    private var _isPlaying = false
    var lastPlayedUri: String? = null
    var lastEnableVolumeRamp: Boolean = false
    var lastRampDurationMs: Long = 0L

    override val isPlaying: Boolean
        get() = _isPlaying

    override fun play(soundUriString: String?, enableVolumeRamp: Boolean, rampDurationMs: Long) {
        stop()
        lastPlayedUri = soundUriString
        lastEnableVolumeRamp = enableVolumeRamp
        lastRampDurationMs = rampDurationMs
        _isPlaying = true
    }

    override fun stop() {
        _isPlaying = false
    }
}

class AlarmSoundPlayerTest {

    private lateinit var audioPlayer: FakeAudioPlayer

    @Before
    fun setUp() {
        audioPlayer = FakeAudioPlayer()
    }

    @Test
    fun isPlaying_isFalse_initially() {
        assertFalse(audioPlayer.isPlaying)
    }

    @Test
    fun play_startsPlayback_andSetsPlayingState() {
        audioPlayer.play("content://media/internal/audio/media/1", enableVolumeRamp = true, rampDurationMs = 5000L)

        assertTrue(audioPlayer.isPlaying)
        assertEquals("content://media/internal/audio/media/1", audioPlayer.lastPlayedUri)
        assertTrue(audioPlayer.lastEnableVolumeRamp)
        assertEquals(5000L, audioPlayer.lastRampDurationMs)
    }

    @Test
    fun stop_resetsPlayingState() {
        audioPlayer.play("content://media/internal/audio/media/1")
        assertTrue(audioPlayer.isPlaying)

        audioPlayer.stop()
        assertFalse(audioPlayer.isPlaying)
    }

    @Test
    fun play_calledSuccessively_replacesPreviousState() {
        audioPlayer.play("content://media/internal/audio/media/1")
        audioPlayer.play("content://media/internal/audio/media/2", enableVolumeRamp = false)

        assertTrue(audioPlayer.isPlaying)
        assertEquals("content://media/internal/audio/media/2", audioPlayer.lastPlayedUri)
        assertFalse(audioPlayer.lastEnableVolumeRamp)
    }
}
