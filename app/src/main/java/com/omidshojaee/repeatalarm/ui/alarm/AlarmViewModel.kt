package com.omidshojaee.repeatalarm.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.data.model.RepeatUnit
import com.omidshojaee.repeatalarm.data.repository.AlarmRepository
import com.omidshojaee.repeatalarm.util.AlarmDateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlarmListUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
    val selectedAlarmId: Long? = null
)

data class AlarmEditUiState(
    val alarmId: Long? = null,
    val title: String = "",
    val startDate: Long = AlarmDateUtils.getStartOfTodayMillis(),
    val startTime: Long = AlarmDateUtils.timeToMillis(8, 0),
    val repeatIntervalValue: Int = 1,
    val repeatIntervalUnit: RepeatUnit = RepeatUnit.DAYS,
    val hasEndDate: Boolean = false,
    val endDate: Long? = null,
    val soundUri: String = "",
    val soundTitle: String = "Default Alarm Sound",
    val vibrate: Boolean = true,
    val isEditing: Boolean = false,
    val titleError: String? = null,
    val intervalError: String? = null,
    val dateError: String? = null,
    val isSaved: Boolean = false
)

class AlarmViewModel(
    private val repository: AlarmRepository
) : ViewModel() {

    private val _selectedAlarmId = MutableStateFlow<Long?>(null)

    val listUiState: StateFlow<AlarmListUiState> = combine(
        repository.getAllAlarms(),
        _selectedAlarmId
    ) { alarms, selectedId ->
        AlarmListUiState(
            alarms = alarms,
            isLoading = false,
            selectedAlarmId = selectedId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlarmListUiState(isLoading = true)
    )

    private val _editUiState = MutableStateFlow(AlarmEditUiState())
    val editUiState: StateFlow<AlarmEditUiState> = _editUiState.asStateFlow()

    fun selectAlarmForView(alarmId: Long?) {
        _selectedAlarmId.value = alarmId
    }

    fun initEditState(alarmId: Long?) {
        if (alarmId == null || alarmId <= 0L) {
            _editUiState.value = AlarmEditUiState(
                startDate = AlarmDateUtils.getStartOfTodayMillis(),
                startTime = AlarmDateUtils.timeToMillis(8, 0)
            )
        } else {
            viewModelScope.launch {
                val alarm = repository.getAlarmById(alarmId)
                if (alarm != null) {
                    _editUiState.value = AlarmEditUiState(
                        alarmId = alarm.id,
                        title = alarm.title,
                        startDate = alarm.startDate,
                        startTime = alarm.startTime,
                        repeatIntervalValue = alarm.repeatIntervalValue,
                        repeatIntervalUnit = alarm.repeatIntervalUnit,
                        hasEndDate = alarm.endDate != null,
                        endDate = alarm.endDate,
                        soundUri = alarm.soundUri,
                        soundTitle = alarm.soundTitle,
                        vibrate = alarm.vibrate,
                        isEditing = true
                    )
                } else {
                    _editUiState.value = AlarmEditUiState()
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _editUiState.update { it.copy(title = title, titleError = null) }
    }

    fun updateStartDate(millis: Long) {
        _editUiState.update { it.copy(startDate = millis, dateError = null) }
    }

    fun updateStartTime(hour: Int, minute: Int) {
        val millis = AlarmDateUtils.timeToMillis(hour, minute)
        _editUiState.update { it.copy(startTime = millis) }
    }

    fun updateRepeatIntervalValue(value: Int) {
        val safeValue = if (value < 1) 1 else value
        _editUiState.update { it.copy(repeatIntervalValue = safeValue, intervalError = null) }
    }

    fun updateRepeatIntervalUnit(unit: RepeatUnit) {
        _editUiState.update { it.copy(repeatIntervalUnit = unit) }
    }

    fun updateHasEndDate(hasEndDate: Boolean) {
        _editUiState.update { state ->
            state.copy(
                hasEndDate = hasEndDate,
                endDate = if (hasEndDate) (state.endDate ?: state.startDate) else null,
                dateError = null
            )
        }
    }

    fun updateEndDate(millis: Long?) {
        _editUiState.update { it.copy(endDate = millis, dateError = null) }
    }

    fun updateSound(uri: String, title: String) {
        _editUiState.update { it.copy(soundUri = uri, soundTitle = title) }
    }

    fun updateVibrate(vibrate: Boolean) {
        _editUiState.update { it.copy(vibrate = vibrate) }
    }

    fun toggleAlarmEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarmEnabled(id, enabled)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            if (_selectedAlarmId.value == alarm.id) {
                _selectedAlarmId.value = null
            }
        }
    }

    fun deleteAlarmById(id: Long) {
        viewModelScope.launch {
            repository.deleteAlarmById(id)
            if (_selectedAlarmId.value == id) {
                _selectedAlarmId.value = null
            }
        }
    }

    fun saveAlarm(): Boolean {
        val state = _editUiState.value

        var isValid = true
        var titleErr: String? = null
        var intervalErr: String? = null
        var dateErr: String? = null

        if (state.title.trim().isEmpty()) {
            titleErr = "Title cannot be empty"
            isValid = false
        }

        if (state.repeatIntervalValue <= 0) {
            intervalErr = "Interval value must be at least 1"
            isValid = false
        }

        if (state.hasEndDate && state.endDate != null) {
            if (state.endDate < state.startDate) {
                dateErr = "End date cannot be earlier than start date"
                isValid = false
            }
        }

        if (!isValid) {
            _editUiState.update {
                it.copy(
                    titleError = titleErr,
                    intervalError = intervalErr,
                    dateError = dateErr
                )
            }
            return false
        }

        val alarmToSave = Alarm(
            id = state.alarmId ?: 0L,
            title = state.title.trim(),
            startDate = state.startDate,
            startTime = state.startTime,
            repeatIntervalValue = state.repeatIntervalValue,
            repeatIntervalUnit = state.repeatIntervalUnit,
            endDate = if (state.hasEndDate) state.endDate else null,
            soundUri = state.soundUri,
            soundTitle = state.soundTitle.ifBlank { "Default Alarm Sound" },
            isEnabled = true,
            vibrate = state.vibrate
        )

        viewModelScope.launch {
            if (state.isEditing && state.alarmId != null) {
                repository.updateAlarm(alarmToSave)
            } else {
                val newId = repository.insertAlarm(alarmToSave)
                _selectedAlarmId.value = newId
            }
            _editUiState.update { it.copy(isSaved = true) }
        }
        return true
    }

    fun resetSaveState() {
        _editUiState.update { it.copy(isSaved = false) }
    }
}
