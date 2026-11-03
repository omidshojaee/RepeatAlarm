package com.omidshojaee.repeatalarm.scheduler

import com.omidshojaee.repeatalarm.data.model.Alarm

interface AlarmScheduler {
    /**
     * Schedules the given alarm for its next calculated trigger time.
     * Returns the next trigger time in epoch millis, or null if the alarm could not be scheduled or has expired.
     */
    fun schedule(alarm: Alarm): Long?

    /**
     * Cancels any pending schedule for the given alarm.
     */
    fun cancel(alarm: Alarm)
}
