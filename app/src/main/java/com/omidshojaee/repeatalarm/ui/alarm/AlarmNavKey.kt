package com.omidshojaee.repeatalarm.ui.alarm

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AlarmNavKey : NavKey {

    @Serializable
    data object AlarmList : AlarmNavKey

    @Serializable
    data class AlarmEdit(val alarmId: Long? = null) : AlarmNavKey
}
