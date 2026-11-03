package com.omidshojaee.repeatalarm

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.omidshojaee.repeatalarm.data.local.AlarmDatabase
import com.omidshojaee.repeatalarm.data.repository.AlarmRepositoryImpl
import com.omidshojaee.repeatalarm.scheduler.AlarmSchedulerImpl
import com.omidshojaee.repeatalarm.ui.alarm.RepeatAlarmApp
import com.omidshojaee.repeatalarm.ui.theme.RepeatAlarmTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("fa")
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locale = Locale("fa")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        enableEdgeToEdge()

        val database = AlarmDatabase.getDatabase(applicationContext)
        val scheduler = AlarmSchedulerImpl(applicationContext)
        val repository = AlarmRepositoryImpl(database.alarmDao(), scheduler)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                RepeatAlarmTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        RepeatAlarmApp(repository = repository)
                    }
                }
            }
        }
    }
}
