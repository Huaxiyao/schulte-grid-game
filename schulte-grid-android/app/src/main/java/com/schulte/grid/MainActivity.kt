package com.schulte.grid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.schulte.grid.ui.screen.GameScreen
import com.schulte.grid.ui.theme.SchulteGridTheme
import com.schulte.grid.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: GameViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            SchulteGridTheme(darkMode = settings.darkMode, themeIndex = settings.themeIndex) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GameScreen(viewModel = viewModel)
                }
            }
        }
    }
}
