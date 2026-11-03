# Project Plan

The app is called Repeat Alarm in Farsi language with RTL support. It allows the user to create alarms with start date, start time, repeat interval and end date. The layout is Material 3 Expressive with per-alarm sound selection. Default language is Farsi, dates are displayed and picked using Persian Solar Hijri (Shamsi) calendar, and typography uses the Vazirmatn font family.

## Project Brief

# Project Brief: Repeat Alarm (زنگ تکرارشونده)

## Features
- **Alarm Creation & Management**: Create and manage alarms with start date, start time, custom repeat interval (e.g., minutes/hours/days), end date, and individual sound selection.
- **Persian Solar Hijri (Shamsi) Calendar**: Integrated Shamsi/Jalali calendar pickers and date displays throughout the application.
- **Farsi First & RTL Design**: Full Farsi (Persian) default language support with native Right-to-Left (RTL) layout alignment.
- **Material 3 Expressive UI & Vazirmatn Typography**: Modern Material 3 Expressive interface styled with the Vazirmatn font family for clean Persian text rendering.
- **Exact Alarm Triggering**: Precision alarm execution and notification delivery using system alarm services and custom sound ringers.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3 Expressive) with Vazirmatn typography
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven) and Compose Material Adaptive library
- **Concurrency & Architecture**: Kotlin Coroutines, Flow, and ViewModel with `StateFlow`
- **System Integration**: Android `AlarmManager`, `NotificationManager`, and `RingtoneManager`
- **Localization & Calendar**: Farsi locale setup, RTL layout enforcement, and Persian Solar Hijri calendar logic

## Implementation Steps
**Total Duration:** 10h 44m 46s

### Task_1_DataModel_And_Scheduler: Define Alarm data model, Room local storage/repository, and Android AlarmManager integration.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - Alarm model supports start date, start time, repeat interval, end date, sound URI, and active status
- **Duration:** 19m 3s

### Task_2_Sound_Selection_And_Ringtone_Manager: Implement ringtone picker integration and audio player manager.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - User can choose system ringtones or alarm sounds per alarm
- **Duration:** 10m 15s

### Task_3_Compose_UI_And_Navigation: Build Material 3 Expressive Jetpack Compose UI using Navigation 3 and Compose Material Adaptive.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - Jetpack Navigation 3 state-driven navigation implemented
- **Duration:** 6m 44s

### Task_6_Vazirmatn_Font_And_Farsi_Default_Locale: Integrate Vazirmatn font family into Compose Material 3 Typography and force Farsi (Persian) default locale across the app (in res/values/strings.xml and Activity locale context) so all UI text displays in Farsi regardless of system language.
- **Status:** COMPLETED
- **Updates:** Added Google Fonts Vazirmatn font family integration to all Material 3 Typography tokens. Set res/values/strings.xml to default Farsi strings and forced Activity context locale to Locale("fa") in MainActivity and AlarmActivity. Verified clean assembleDebug build and 26 unit tests passed.
- **Acceptance Criteria:**
  - Vazirmatn font family from Google Fonts applied to all Material3 Typography styles
  - App default strings in res/values/strings.xml set to Farsi
  - App activity locale forced to Farsi ('fa') and RTL layout direction enforced

### Task_7_Persian_Solar_Hijri_Calendar: Implement Persian Solar Hijri (Shamsi / Jalali) calendar date converter, Shamsi date picker dialog, and Shamsi date formatting for start date and end date throughout the app.
- **Status:** COMPLETED
- **Updates:** Implemented PersianCalendarHelper with bi-directional Gregorian <-> Shamsi conversion, PersianDatePickerDialog Compose component, and updated AlarmEditScreen and AlarmListScreen to display and pick dates in Persian Solar Hijri (Shamsi) calendar with Persian digits. Added unit tests in PersianCalendarHelperTest. All 34 unit tests passed and assembleDebug completed successfully.
- **Acceptance Criteria:**
  - Solar Hijri (Shamsi) date converter algorithm (Gregorian <-> Shamsi) implemented
  - Date picker allows selecting dates in Shamsi calendar (Year/Month/Day in Shamsi e.g. 1403/12/05)
  - All date displays in alarm list, alarm edit screen, and alarm activity formatted in Shamsi calendar with Persian digits
- **Duration:** 7m 5s

### Task_4_Alarm_Alert_Screen_And_Run_Verify: Verify build, test suite execution, and UI alignment with Vazirmatn font, Farsi locale, and Shamsi dates.
- **Status:** COMPLETED
- **Updates:** Ran all unit tests (34 tests passed out of 34) and assembleDebug build. Confirmed Vazirmatn font typography, primary Farsi locale, Shamsi date picker and calendar displays, RTL layout, and alarm alert screen verification.
- **Acceptance Criteria:**
  - All unit tests pass
  - Build passes cleanly
  - App displays Farsi text with Vazirmatn font and Shamsi dates
- **Duration:** 10h 1m 39s

