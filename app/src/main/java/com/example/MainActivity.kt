package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.location.LocationTracker
import com.example.ui.screens.WeatherDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WeatherViewModel
import com.example.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        val locationTracker = LocationTracker(applicationContext)
        val viewModelFactory = WeatherViewModelFactory(applicationContext, locationTracker)
        
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherDashboardScreen(
                        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
