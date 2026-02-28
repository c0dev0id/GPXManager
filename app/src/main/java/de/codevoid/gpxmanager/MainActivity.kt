package de.codevoid.gpxmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import de.codevoid.gpxmanager.navigation.AppNavigation
import de.codevoid.gpxmanager.ui.theme.GpxManagerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GpxManagerTheme {
                AppNavigation()
            }
        }
    }
}
