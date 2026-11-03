package com.omidshojaee.repeatalarm.sound

import android.net.Uri

class FakeRingtoneRepository(
    private val availableSoundsList: List<RingtoneItem> = listOf(
        RingtoneItem("Default Alarm Sound", uri = null, uriString = "content://settings/system/alarm_alert", isDefault = true),
        RingtoneItem("Digital Bell", uri = null, uriString = "content://media/internal/audio/media/1"),
        RingtoneItem("Chime", uri = null, uriString = "content://media/internal/audio/media/2"),
        RingtoneItem("Morning Breeze", uri = null, uriString = "content://media/internal/audio/media/3")
    )
) : RingtoneRepository {

    override fun getAvailableAlarmSounds(): List<RingtoneItem> = availableSoundsList

    override fun getDefaultAlarmSound(): RingtoneItem = availableSoundsList.first { it.isDefault }

    override fun getRingtoneTitle(uriString: String?): String {
        if (uriString.isNullOrBlank()) return "Default Alarm Sound"
        return availableSoundsList.find { it.uriString == uriString }?.title ?: "Custom Sound"
    }

    override fun getRingtoneUri(uriString: String?): Uri {
        return Uri.EMPTY
    }
}
