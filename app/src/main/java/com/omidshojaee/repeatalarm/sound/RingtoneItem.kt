package com.omidshojaee.repeatalarm.sound

import android.net.Uri

/**
 * Represents a ringtone or alarm sound item available on the device.
 */
data class RingtoneItem(
    val title: String,
    val uri: Uri?,
    val uriString: String = uri?.toString() ?: "",
    val isDefault: Boolean = false
)
