package com.omidshojaee.repeatalarm.sound

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.omidshojaee.repeatalarm.ui.util.drawVerticalScrollbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omidshojaee.repeatalarm.R

/**
 * UI State contract for Sound / Ringtone selection.
 */
data class SoundPickerState(
    val selectedUriString: String = "",
    val selectedTitle: String = "Default Alarm Sound",
    val availableSounds: List<RingtoneItem> = emptyList(),
    val playingPreviewUri: String? = null
)

/**
 * Compose ModalBottomSheet component for selecting per-alarm ringtones/sounds.
 * Provides on-device ringtone listing, preview audio playback, and system picker integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickerSheet(
    currentUriString: String,
    onSoundSelected: (uriString: String, title: String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val context = LocalContext.current
    val ringtoneRepository = remember(context) { RingtoneRepositoryImpl(context) }
    val audioPlayer = remember(context) { AlarmSoundPlayer(context) }
    val systemPickerLauncher = rememberRingtonePickerLauncher { uriString, title ->
        audioPlayer.stop()
        onSoundSelected(uriString, title)
        onDismissRequest()
    }

    var availableSounds by remember { mutableStateOf<List<RingtoneItem>>(emptyList()) }
    var selectedUriString by remember { mutableStateOf(currentUriString) }
    var playingPreviewUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        availableSounds = ringtoneRepository.getAvailableAlarmSounds()
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stop()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            audioPlayer.stop()
            onDismissRequest()
        },
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.select_alarm_sound),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        val selectedSound = availableSounds.find { it.uriString == selectedUriString }
                        val title = selectedSound?.title
                            ?: ringtoneRepository.getRingtoneTitle(selectedUriString)
                        audioPlayer.stop()
                        onSoundSelected(selectedUriString, title)
                        onDismissRequest()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.confirm_selection),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    audioPlayer.stop()
                    playingPreviewUri = null
                    systemPickerLauncher.launch(selectedUriString)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.system_ringtone_picker))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .drawVerticalScrollbar(listState)
            ) {
                items(availableSounds) { sound ->
                    val isSelected = (sound.uriString == selectedUriString) ||
                            (sound.isDefault && selectedUriString.isBlank())
                    val isPreviewing = (playingPreviewUri == sound.uriString)

                    val soundDisplayTitle = when (sound.title) {
                        "Default Alarm Sound" -> stringResource(R.string.default_alarm_sound)
                        "Custom Sound" -> stringResource(R.string.custom_sound)
                        else -> sound.title
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedUriString = sound.uriString
                                if (isPreviewing) {
                                    audioPlayer.stop()
                                    playingPreviewUri = null
                                } else {
                                    playingPreviewUri = sound.uriString
                                    audioPlayer.play(sound.uriString, enableVolumeRamp = false)
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedUriString = sound.uriString
                                playingPreviewUri = sound.uriString
                                audioPlayer.play(sound.uriString, enableVolumeRamp = false)
                            }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = soundDisplayTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (isPreviewing) {
                                    audioPlayer.stop()
                                    playingPreviewUri = null
                                } else {
                                    playingPreviewUri = sound.uriString
                                    audioPlayer.play(sound.uriString, enableVolumeRamp = false)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPreviewing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPreviewing) stringResource(R.string.stop_preview) else stringResource(R.string.play_preview),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
