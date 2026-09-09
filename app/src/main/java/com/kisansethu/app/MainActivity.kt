package com.kisansethu.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.kisansethu.app.data.UserPreferencesRepository
import com.kisansethu.app.navigation.KisanSethuNavHost
import com.kisansethu.app.ui.theme.KisanSethuTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val userPreferencesRepository = remember { UserPreferencesRepository(context) }
            val isDarkThemePref by userPreferencesRepository.isDarkTheme.collectAsState(initial = null)
            val darkTheme = false
            val coroutineScope = rememberCoroutineScope()

            KisanSethuTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                KisanSethuNavHost(
                    isDarkTheme = darkTheme,
                    onToggleTheme = {
                        coroutineScope.launch {
                            userPreferencesRepository.saveDarkTheme(!darkTheme)
                        }
                    }
                )
            }
        }
    }
}
