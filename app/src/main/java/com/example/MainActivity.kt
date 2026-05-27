package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainBaccaratScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishPrimaryBlue
import com.example.ui.viewmodel.BaccaratViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        appOpenAdManager = AppOpenAdManager(this)

        setContent {
            MyApplicationTheme {
                var canShowAppContent by remember { mutableStateOf(false) }

                if (!canShowAppContent) {
                    // Show a simple loading screen while the ad loads
                    Box(
                        modifier = Modifier.fillMaxSize().background(PolishBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PolishPrimaryBlue)
                    }

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        // Attempt to load and show the ad immediately
                        appOpenAdManager.loadAd(
                            onLoaded = {
                                appOpenAdManager.showAdIfAvailable(this@MainActivity) {
                                    canShowAppContent = true
                                }
                            },
                            onError = {
                                canShowAppContent = true
                            }
                        )
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        val viewModel: BaccaratViewModel = viewModel()
                        MainBaccaratScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

