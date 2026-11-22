package com.exemplo.oficina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.exemplo.oficina.ui.navigation.AppNavigation
import com.exemplo.oficina.ui.theme.TrabalhoEdivaldoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as OficinaApplication).container

        setContent {
            val themeMode by appContainer.settingsRepository.themeModeFlow.collectAsState(initial = "SISTEMA")
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "CLARO" -> false
                "ESCURO" -> true
                else -> systemDark
            }

            TrabalhoEdivaldoTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appContainer = appContainer)
                }
            }
        }
    }
}
