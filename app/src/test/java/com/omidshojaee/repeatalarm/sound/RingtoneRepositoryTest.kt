package com.omidshojaee.repeatalarm.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RingtoneRepositoryTest {

    private lateinit var ringtoneRepository: RingtoneRepository

    @Before
    fun setUp() {
        ringtoneRepository = FakeRingtoneRepository()
    }

    @Test
    fun getRingtoneTitle_returnsDefaultAlarmSound_whenUriStringIsNull() {
        val title = ringtoneRepository.getRingtoneTitle(null)
        assertEquals("Default Alarm Sound", title)
    }

    @Test
    fun getRingtoneTitle_returnsDefaultAlarmSound_whenUriStringIsEmpty() {
        val title = ringtoneRepository.getRingtoneTitle("")
        assertEquals("Default Alarm Sound", title)
    }

    @Test
    fun getRingtoneTitle_returnsCustomSound_whenUriStringIsUnknown() {
        val title = ringtoneRepository.getRingtoneTitle("content://unknown/uri")
        assertEquals("Custom Sound", title)
    }

    @Test
    fun getRingtoneTitle_returnsMatchingTitle_whenUriStringIsKnown() {
        val title = ringtoneRepository.getRingtoneTitle("content://media/internal/audio/media/1")
        assertEquals("Digital Bell", title)
    }

    @Test
    fun getDefaultAlarmSound_returnsDefaultSoundItem() {
        val defaultSound = ringtoneRepository.getDefaultAlarmSound()
        assertNotNull(defaultSound)
        assertTrue(defaultSound.isDefault)
        assertEquals("Default Alarm Sound", defaultSound.title)
    }

    @Test
    fun getAvailableAlarmSounds_returnsListOfRingtoneItems() {
        val sounds = ringtoneRepository.getAvailableAlarmSounds()
        assertTrue(sounds.isNotEmpty())
        assertTrue(sounds.any { it.isDefault })
    }
}
