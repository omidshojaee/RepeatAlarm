package com.omidshojaee.repeatalarm.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.omidshojaee.repeatalarm.ui.util.drawVerticalScrollbar
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omidshojaee.repeatalarm.R
import com.omidshojaee.repeatalarm.data.model.Alarm
import com.omidshojaee.repeatalarm.data.model.RepeatUnit
import com.omidshojaee.repeatalarm.ui.theme.RepeatAlarmTheme
import com.omidshojaee.repeatalarm.util.AlarmDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    uiState: AlarmListUiState,
    onToggleEnabled: (id: Long, enabled: Boolean) -> Unit,
    onDeleteAlarm: (alarm: Alarm) -> Unit,
    onSelectAlarm: (alarmId: Long) -> Unit,
    onCreateAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateAlarm,
                icon = { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_alarm)) },
                text = { Text(stringResource(R.string.add_alarm)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.alarms.isEmpty()) {
                EmptyAlarmsCard(
                    onCreateAlarm = onCreateAlarm,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawVerticalScrollbar(listState),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.alarms,
                        key = { it.id }
                    ) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            isSelected = (alarm.id == uiState.selectedAlarmId),
                            onToggleEnabled = { enabled -> onToggleEnabled(alarm.id, enabled) },
                            onCardClick = { onSelectAlarm(alarm.id) },
                            onDeleteClick = { alarmToDelete = alarm }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Delete Confirmation Dialog
            alarmToDelete?.let { alarm ->
                val alarmName = alarm.title.ifBlank { stringResource(R.string.delete_alarm_confirm_default) }
                AlertDialog(
                    onDismissRequest = { alarmToDelete = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = { Text(text = stringResource(R.string.delete_alarm_title)) },
                    text = {
                        Text(
                            text = stringResource(R.string.delete_alarm_confirm, alarmName)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteAlarm(alarm)
                                alarmToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { alarmToDelete = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmItemCard(
    alarm: Alarm,
    isSelected: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardContainerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val soundDisplayTitle = when (alarm.soundTitle) {
        "Default Alarm Sound", "" -> stringResource(R.string.default_alarm_sound)
        "Custom Sound" -> stringResource(R.string.custom_sound)
        else -> alarm.soundTitle
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardContainerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = AlarmDateUtils.formatTime(alarm.startTime),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Text(
                        text = alarm.title.ifBlank { stringResource(R.string.alarm) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = onToggleEnabled
                    )

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Next trigger chip
                if (alarm.isEnabled && alarm.nextTriggerTime != null) {
                    AlarmChip(
                        icon = Icons.Rounded.Alarm,
                        text = stringResource(R.string.next_trigger_prefix, AlarmDateUtils.formatDateTime(alarm.nextTriggerTime)),
                        isEnabled = alarm.isEnabled
                    )
                }

                // Repeat interval chip
                AlarmChip(
                    icon = Icons.Rounded.Repeat,
                    text = AlarmDateUtils.formatRepeatInterval(context, alarm.repeatIntervalValue, alarm.repeatIntervalUnit),
                    isEnabled = alarm.isEnabled
                )

                // Start date chip
                AlarmChip(
                    icon = Icons.Rounded.CalendarToday,
                    text = stringResource(R.string.start_prefix, AlarmDateUtils.formatDate(alarm.startDate)),
                    isEnabled = alarm.isEnabled
                )

                // End date chip (if set)
                alarm.endDate?.let { endDate ->
                    AlarmChip(
                        icon = Icons.Rounded.CalendarToday,
                        text = stringResource(R.string.end_prefix, AlarmDateUtils.formatDate(endDate)),
                        isEnabled = alarm.isEnabled
                    )
                }

                // Sound title chip
                AlarmChip(
                    icon = Icons.Rounded.MusicNote,
                    text = soundDisplayTitle,
                    isEnabled = alarm.isEnabled
                )
            }
        }
    }
}

@Composable
fun AlarmChip(
    icon: ImageVector,
    text: String,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        enabled = isEnabled,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = CircleShape,
        modifier = modifier
    )
}

@Composable
fun EmptyAlarmsCard(
    onCreateAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AlarmOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.no_alarms_set),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.no_alarms_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateAlarm,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = stringResource(R.string.create_alarm))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmListScreenPreview() {
    RepeatAlarmTheme {
        AlarmListScreen(
            uiState = AlarmListUiState(
                alarms = listOf(
                    Alarm(
                        id = 1,
                        title = "Morning Medicine",
                        startDate = System.currentTimeMillis(),
                        startTime = AlarmDateUtils.timeToMillis(8, 30),
                        repeatIntervalValue = 1,
                        repeatIntervalUnit = RepeatUnit.DAYS,
                        soundTitle = "Default Alarm Sound",
                        isEnabled = true
                    ),
                    Alarm(
                        id = 2,
                        title = "Bi-weekly Workout",
                        startDate = System.currentTimeMillis(),
                        startTime = AlarmDateUtils.timeToMillis(18, 0),
                        repeatIntervalValue = 2,
                        repeatIntervalUnit = RepeatUnit.WEEKS,
                        soundTitle = "Chimes",
                        isEnabled = false
                    )
                )
            ),
            onToggleEnabled = { _, _ -> },
            onDeleteAlarm = {},
            onSelectAlarm = {},
            onCreateAlarm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmListScreenEmptyPreview() {
    RepeatAlarmTheme {
        AlarmListScreen(
            uiState = AlarmListUiState(alarms = emptyList()),
            onToggleEnabled = { _, _ -> },
            onDeleteAlarm = {},
            onSelectAlarm = {},
            onCreateAlarm = {}
        )
    }
}
