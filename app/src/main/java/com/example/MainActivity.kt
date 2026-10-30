package com.example

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.vr.ui.DeviceTestingDialog
import com.example.vr.ui.MainMenuScreen
import com.example.vr.ui.SettingsDialog
import com.example.vr.ui.VrExperienceScreen
import com.example.vr.viewmodel.AppScreen
import com.example.vr.viewmodel.VrViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on during VR session
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable immersive full-screen sticky mode
        hideSystemBars()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VrSkyViewApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        if (viewModel.uiState.value.currentScreen == AppScreen.VR_EXPERIENCE) {
            viewModel.headTracker.start()
            viewModel.audioEngine.start()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.headTracker.stop()
        viewModel.audioEngine.stop()
    }
}

@Composable
fun VrSkyViewApp(viewModel: VrViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.currentScreen) {
        AppScreen.MAIN_MENU -> {
            MainMenuScreen(uiState = uiState, viewModel = viewModel)
        }
        AppScreen.VR_EXPERIENCE -> {
            VrExperienceScreen(uiState = uiState, viewModel = viewModel)
        }
    }

    // Modal dialogs
    if (uiState.showSettingsDialog) {
        SettingsDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { viewModel.closeSettingsDialog() }
        )
    }

    if (uiState.showTestingDialog) {
        DeviceTestingDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { viewModel.closeTestingDialog() }
        )
    }
}
