package com.omidshojaee.repeatalarm.sound

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

import androidx.core.content.IntentCompat

class RingtonePickerLauncher(
    private val onLaunchSystemPicker: (currentUriString: String) -> Unit,
) {
    fun launch(currentUriString: String = "") {
        onLaunchSystemPicker(currentUriString)
    }
}

/**
 * Remembers a launcher for launching the Android system Ringtone Picker intent.
 * Calls [onSoundSelected] with the selected URI string and resolved title upon completion.
 */
@Composable
fun rememberRingtonePickerLauncher(
    onSoundSelected: (uriString: String, title: String) -> Unit,
): RingtonePickerLauncher {
    val context = LocalContext.current
    val ringtoneRepository = remember(context) { RingtoneRepositoryImpl(context) }

    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val pickedUri = intent?.let {
                IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            }
            val uriString = pickedUri?.toString() ?: ""
            val title = ringtoneRepository.getRingtoneTitle(uriString)
            onSoundSelected(uriString, title)
        }
    }

    return remember(activityLauncher) {
        RingtonePickerLauncher { currentUriString ->
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)

                if (currentUriString.isNotBlank()) {
                    try {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentUriString))
                    } catch (_: Exception) {}
                } else {
                    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultUri)
                }
            }
            activityLauncher.launch(intent)
        }
    }
}
