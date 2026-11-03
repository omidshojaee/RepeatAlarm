package com.omidshojaee.repeatalarm.ui.alarm

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.omidshojaee.repeatalarm.data.repository.AlarmRepository

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun RepeatAlarmApp(
    repository: AlarmRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: AlarmViewModel = viewModel(
        factory = AlarmViewModelFactory(repository)
    )

    val backStack = rememberNavBackStack(AlarmNavKey.AlarmList)

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        modifier = modifier,
        entryProvider = entryProvider {
            entry<AlarmNavKey.AlarmList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        AlarmDetailPlaceholder(
                            onCreateAlarm = {
                                viewModel.selectAlarmForView(null)
                                viewModel.initEditState(null)
                                backStack.add(AlarmNavKey.AlarmEdit(null))
                            }
                        )
                    }
                )
            ) {
                val listUiState by viewModel.listUiState.collectAsState()

                AlarmListScreen(
                    uiState = listUiState,
                    onToggleEnabled = { id, enabled ->
                        viewModel.toggleAlarmEnabled(id, enabled)
                    },
                    onDeleteAlarm = { alarm ->
                        viewModel.deleteAlarm(alarm)
                    },
                    onSelectAlarm = { alarmId ->
                        viewModel.selectAlarmForView(alarmId)
                        viewModel.initEditState(alarmId)
                        backStack.add(AlarmNavKey.AlarmEdit(alarmId))
                    },
                    onCreateAlarm = {
                        viewModel.selectAlarmForView(null)
                        viewModel.initEditState(null)
                        backStack.add(AlarmNavKey.AlarmEdit(null))
                    }
                )
            }

            entry<AlarmNavKey.AlarmEdit>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { navKey ->
                val editUiState by viewModel.editUiState.collectAsState()

                LaunchedEffect(navKey.alarmId) {
                    viewModel.initEditState(navKey.alarmId)
                }

                LaunchedEffect(editUiState.isSaved) {
                    if (editUiState.isSaved) {
                        viewModel.resetSaveState()
                        backStack.removeLastOrNull()
                    }
                }

                AlarmEditScreen(
                    uiState = editUiState,
                    onTitleChange = viewModel::updateTitle,
                    onStartDateChange = viewModel::updateStartDate,
                    onStartTimeChange = viewModel::updateStartTime,
                    onRepeatIntervalValueChange = viewModel::updateRepeatIntervalValue,
                    onRepeatIntervalUnitChange = viewModel::updateRepeatIntervalUnit,
                    onHasEndDateChange = viewModel::updateHasEndDate,
                    onEndDateChange = viewModel::updateEndDate,
                    onSoundSelected = viewModel::updateSound,
                    onVibrateChange = viewModel::updateVibrate,
                    onSave = {
                        viewModel.saveAlarm()
                    },
                    onCancel = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
