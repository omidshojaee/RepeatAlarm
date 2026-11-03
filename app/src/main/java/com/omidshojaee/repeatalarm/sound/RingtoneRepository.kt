package com.omidshojaee.repeatalarm.sound

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.omidshojaee.repeatalarm.R

interface RingtoneRepository {
    fun getAvailableAlarmSounds(): List<RingtoneItem>
    fun getDefaultAlarmSound(): RingtoneItem
    fun getRingtoneTitle(uriString: String?): String
    fun getRingtoneUri(uriString: String?): Uri
}

class RingtoneRepositoryImpl(
    private val context: Context
) : RingtoneRepository {

    override fun getAvailableAlarmSounds(): List<RingtoneItem> {
        val sounds = mutableListOf<RingtoneItem>()

        val defaultSound = getDefaultAlarmSound()
        sounds.add(defaultSound)

        try {
            val ringtoneManager = RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_ALARM)
            }
            val cursor = ringtoneManager.cursor
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val position = cursor.position
                    val uri = ringtoneManager.getRingtoneUri(position)
                    val ringtone = ringtoneManager.getRingtone(position)
                    val title = try {
                        ringtone?.getTitle(context) ?: "${context.getString(R.string.alarm_sound)} #${position + 1}"
                    } catch (e: Exception) {
                        "${context.getString(R.string.alarm_sound)} #${position + 1}"
                    }

                    if (uri != null && uri != defaultSound.uri) {
                        sounds.add(
                            RingtoneItem(
                                title = title,
                                uri = uri,
                                isDefault = false
                            )
                        )
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying available alarm sounds", e)
        }

        return sounds.distinctBy { it.uriString }
    }

    override fun getDefaultAlarmSound(): RingtoneItem {
        val defaultUri = getSystemDefaultAlarmUri()
        val resolvedTitle = getRingtoneTitle(defaultUri?.toString())
        val defaultTitle = context.getString(R.string.default_alarm_sound)
        val customTitle = context.getString(R.string.custom_sound)
        val title = if (resolvedTitle.isBlank() || resolvedTitle == "Custom Sound" || resolvedTitle == customTitle) defaultTitle else resolvedTitle
        return RingtoneItem(
            title = title,
            uri = defaultUri,
            isDefault = true
        )
    }

    override fun getRingtoneTitle(uriString: String?): String {
        val defaultTitle = context.getString(R.string.default_alarm_sound)
        val customTitle = context.getString(R.string.custom_sound)
        if (uriString.isNullOrBlank()) {
            return defaultTitle
        }
        return try {
            val uri = Uri.parse(uriString)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val title = ringtone?.getTitle(context)
            if (title.isNullOrBlank()) customTitle else title
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve ringtone title for $uriString", e)
            customTitle
        }
    }

    override fun getRingtoneUri(uriString: String?): Uri {
        if (uriString.isNullOrBlank()) {
            return getSystemDefaultAlarmUri() ?: Uri.EMPTY
        }
        return try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            getSystemDefaultAlarmUri() ?: Uri.EMPTY
        }
    }

    private fun getSystemDefaultAlarmUri(): Uri? {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI
    }

    companion object {
        private const val TAG = "RingtoneRepository"
    }
}
