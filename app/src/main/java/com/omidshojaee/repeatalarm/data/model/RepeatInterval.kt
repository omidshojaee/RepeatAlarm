package com.omidshojaee.repeatalarm.data.model

import android.content.Context
import com.omidshojaee.repeatalarm.util.AlarmDateUtils

data class RepeatInterval(
    val value: Int = 1,
    val unit: RepeatUnit = RepeatUnit.DAYS
) {
    fun toFormattedString(context: Context? = null): String {
        return if (context != null) {
            AlarmDateUtils.formatRepeatInterval(context, value, unit)
        } else {
            AlarmDateUtils.formatRepeatInterval(value, unit)
        }
    }
}
