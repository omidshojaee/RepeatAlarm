package com.omidshojaee.repeatalarm.ui.alarm

import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.data.model.RepeatUnit
import com.omidshojaee.repeatalarm.data.repository.AlarmRepositoryImpl
import com.omidshojaee.repeatalarm.repository.FakeAlarmDao
import com.omidshojaee.repeatalarm.repository.FakeAlarmScheduler
import com.omidshojaee.repeatalarm.util.AlarmDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDao: FakeAlarmDao
    private lateinit var fakeScheduler: FakeAlarmScheduler
    private lateinit var repository: AlarmRepositoryImpl
    private lateinit var viewModel: AlarmViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeAlarmDao()
        fakeScheduler = FakeAlarmScheduler()
        repository = AlarmRepositoryImpl(fakeDao, fakeScheduler)
        viewModel = AlarmViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initEditState with null id initializes new alarm state`() = runTest {
        viewModel.initEditState(null)

        val state = viewModel.editUiState.value
        assertNull(state.alarmId)
        assertEquals("", state.title)
        assertFalse(state.isEditing)
        assertFalse(state.hasEndDate)
        assertTrue(state.vibrate)
    }

    @Test
    fun `initEditState with existing id loads alarm into edit state`() = runTest {
        val alarm = Alarm(
            title = "Existing Alarm",
            startDate = AlarmDateUtils.getStartOfTodayMillis(),
            startTime = AlarmDateUtils.timeToMillis(9, 30),
            repeatIntervalValue = 2,
            repeatIntervalUnit = RepeatUnit.WEEKS,
            soundTitle = "Radar",
            vibrate = true
        )
        val id = repository.insertAlarm(alarm)

        viewModel.initEditState(id)

        val state = viewModel.editUiState.value
        assertEquals(id, state.alarmId)
        assertEquals("Existing Alarm", state.title)
        assertEquals(2, state.repeatIntervalValue)
        assertEquals(RepeatUnit.WEEKS, state.repeatIntervalUnit)
        assertEquals("Radar", state.soundTitle)
        assertTrue(state.isEditing)
    }

    @Test
    fun `updateTitle updates title in state and clears title error`() = runTest {
        viewModel.saveAlarm() // Trigger validation error on empty title
        assertNotNull(viewModel.editUiState.value.titleError)

        viewModel.updateTitle("New Title")
        assertEquals("New Title", viewModel.editUiState.value.title)
        assertNull(viewModel.editUiState.value.titleError)
    }

    @Test
    fun `saveAlarm with empty title fails validation`() = runTest {
        viewModel.initEditState(null)
        viewModel.updateTitle("   ")

        val result = viewModel.saveAlarm()
        assertFalse(result)
        assertNotNull(viewModel.editUiState.value.titleError)
        assertFalse(viewModel.editUiState.value.isSaved)
    }

    @Test
    fun `saveAlarm with end date earlier than start date fails validation`() = runTest {
        val today = AlarmDateUtils.getStartOfTodayMillis()
        val yesterday = today - (24 * 60 * 60 * 1000L)

        viewModel.initEditState(null)
        viewModel.updateTitle("Valid Title")
        viewModel.updateStartDate(today)
        viewModel.updateHasEndDate(true)
        viewModel.updateEndDate(yesterday)

        val result = viewModel.saveAlarm()
        assertFalse(result)
        assertNotNull(viewModel.editUiState.value.dateError)
        assertFalse(viewModel.editUiState.value.isSaved)
    }

    @Test
    fun `saveAlarm with valid new alarm inserts into repository`() = runTest {
        viewModel.initEditState(null)
        viewModel.updateTitle("Morning Alarm")
        viewModel.updateStartTime(7, 45)
        viewModel.updateRepeatIntervalValue(1)
        viewModel.updateRepeatIntervalUnit(RepeatUnit.DAYS)

        val result = viewModel.saveAlarm()
        assertTrue(result)
        assertTrue(viewModel.editUiState.value.isSaved)

        val alarms = fakeDao.getActiveAlarms()
        assertEquals(1, alarms.size)
        assertEquals("Morning Alarm", alarms[0].title)
    }

    @Test
    fun `toggleAlarmEnabled updates state in repository`() = runTest {
        val alarm = Alarm(
            title = "Toggle Test",
            startDate = AlarmDateUtils.getStartOfTodayMillis(),
            startTime = AlarmDateUtils.timeToMillis(10, 0),
            isEnabled = true
        )
        val id = repository.insertAlarm(alarm)

        viewModel.toggleAlarmEnabled(id, false)

        val updated = repository.getAlarmById(id)
        assertNotNull(updated)
        assertFalse(updated!!.isEnabled)
    }

    @Test
    fun `deleteAlarm removes alarm from repository`() = runTest {
        val alarm = Alarm(
            title = "Delete Test",
            startDate = AlarmDateUtils.getStartOfTodayMillis(),
            startTime = AlarmDateUtils.timeToMillis(12, 0)
        )
        val id = repository.insertAlarm(alarm)
        val savedAlarm = repository.getAlarmById(id)!!

        viewModel.deleteAlarm(savedAlarm)

        val deleted = repository.getAlarmById(id)
        assertNull(deleted)
    }
}
