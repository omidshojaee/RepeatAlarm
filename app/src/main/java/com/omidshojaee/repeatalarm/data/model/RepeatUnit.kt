package com.omidshojaee.repeatalarm.data.model

import android.content.Context
import com.omidshojaee.repeatalarm.R

enum class RepeatUnit {
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS;

    fun getLocalizedName(context: Context): String {
        return when (this) {
            MINUTES -> context.getString(R.string.unit_minutes)
            HOURS -> context.getString(R.string.unit_hours)
            DAYS -> context.getString(R.string.unit_days)
            WEEKS -> context.getString(R.string.unit_weeks)
            MONTHS -> context.getString(R.string.unit_months)
        }
    }
}
