package com.fqrproject.fqr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fqrproject.fqr.ui.fitness.StepViewModel
import com.fqrproject.fqr.ui.theme.FQRTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FQRTheme {
                val context = LocalContext.current
                val stepViewModel: StepViewModel = viewModel()

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        stepViewModel.startListening()
                    }
                }

                LaunchedEffect(Unit) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        stepViewModel.startListening()
                    } else {
                        launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        stepViewModel.stopListening()
                    }
                }
                AppNavHost()
            }
        }
    }
}
