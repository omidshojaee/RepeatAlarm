# Repeat Alarm

A Farsi-first Android alarm app built with Jetpack Compose for creating alarms that repeat on a custom interval, rather than the fixed daily/weekly schedules most alarm clocks offer.

## Features

- **Custom repeat intervals** — repeat an alarm every N minutes, hours, days, weeks, or months (not just "daily" or specific weekdays).
- **Start date & time control** — an alarm's first trigger happens exactly at the configured start date/time, not immediately after creation.
- **Optional end date** — stop a repeating alarm automatically on a chosen date, or let it repeat indefinitely.
- **Alarm sound selection** — use the app default sound, pick a custom audio file, or open the system ringtone picker, with in-app preview playback before saving.
- **Vibration toggle** per alarm.
- **Full-screen ringing screen** — alarms display over the lock screen and turn the screen on when triggered.
- **Snooze and dismiss** controls on the ringing screen.
- **Reboot-safe scheduling** — alarms are rescheduled automatically after the device restarts.
- **Next-trigger preview** — the alarm list shows when each enabled alarm will next go off.
- **Persian (Jalali) calendar** — dates are picked and displayed using the Persian calendar, with Persian numerals.
- **RTL, Farsi-first UI** — interface language and layout direction are forced to Farsi/RTL; English string resources are also provided.
- **Adaptive layout** — a list-detail pane layout (via Material3 Adaptive) that can show the alarm list and editor side by side on tablets and unfolded foldables, collapsing to single-pane navigation on phones.
- **Material 3 design** built entirely with Jetpack Compose.

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** for local alarm storage
- **Navigation 3** with Material3 Adaptive list-detail scaffolding
- **AlarmManager** (exact alarms) for scheduling, with a `BroadcastReceiver` to handle triggers and reschedule on boot
- **Coroutines / Flow** for async data handling

## Requirements

- `minSdk` 24 (Android 7.0)
- `targetSdk` / `compileSdk` 35

## Permissions

| Permission | Purpose |
|---|---|
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Schedule alarms that fire at an exact time |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after a device restart |
| `POST_NOTIFICATIONS` | Show the alarm notification |
| `VIBRATE` | Vibrate when an alarm rings |
| `WAKE_LOCK` | Keep the device awake while an alarm is ringing |
| `USE_FULL_SCREEN_INTENT` | Show the ringing screen over the lock screen |
| `INTERNET` | Network access for bundled networking libraries |

## Building

Open the project in Android Studio and build the `app` module normally (`Run`, or **Build → Generate Signed Bundle / APK…** for a release build). Release signing is configured via a local `key.properties` file (gitignored) referencing a keystore; see `app/build.gradle.kts` for the `signingConfigs` setup.

## Project Structure

```
app/src/main/java/com/omidshojaee/repeatalarm/
├── data/        # Room entities, DAO, repository
├── scheduler/   # Alarm scheduling logic (AlarmManager)
├── receiver/    # BroadcastReceivers (alarm trigger, boot reschedule)
├── sound/       # Sound picker UI
├── ui/alarm/    # Compose screens (list, edit, ringing, detail placeholder)
├── ui/theme/    # Material 3 theme
└── util/        # Date/time formatting, Persian calendar conversion
```
