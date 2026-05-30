package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import com.example.location.LocationTracker
import com.example.ui.components.getWeatherDescription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val data: OpenMeteoResponse,
        val locationLabel: String = "GPS Local Target"
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val context: Context,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _currentLatitude = MutableStateFlow(37.7749) // SF Default
    val currentLatitude: StateFlow<Double> = _currentLatitude.asStateFlow()

    private val _currentLongitude = MutableStateFlow(-122.4194) // SF Default
    val currentLongitude: StateFlow<Double> = _currentLongitude.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private val _aiReport = MutableStateFlow<String>("Awaiting telemetry analysis...")
    val aiReport: StateFlow<String> = _aiReport.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        fetchWeatherForCurrentGps()
    }

    fun fetchWeatherForCurrentGps() {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val location = locationTracker.getCurrentLocation()
                if (location != null) {
                    _currentLatitude.value = location.latitude
                    _currentLongitude.value = location.longitude
                    _isGpsActive.value = true
                    loadWeatherData(location.latitude, location.longitude, "GPS Local Coordinate Target")
                } else {
                    _isGpsActive.value = false
                    loadWeatherData(_currentLatitude.value, _currentLongitude.value, "Default Sector Coordinates")
                }
            } catch (e: Exception) {
                _isGpsActive.value = false
                loadWeatherData(_currentLatitude.value, _currentLongitude.value, "Default Sector Coordinates")
            }
        }
    }

    fun setCoordinatesManually(lat: Double, lon: Double, name: String = "Manual Coordinate Override") {
        _currentLatitude.value = lat
        _currentLongitude.value = lon
        _isGpsActive.value = false
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            loadWeatherData(lat, lon, name)
        }
    }

    private suspend fun loadWeatherData(lat: Double, lon: Double, label: String) {
        try {
            val response = OpenMeteoClient.service.getForecast(lat, lon)
            _uiState.value = WeatherUiState.Success(response, label)
            generateAiReport(response)
        } catch (e: Exception) {
            _uiState.value = WeatherUiState.Error("Atmospheric uplink offline: ${e.localizedMessage ?: "Unknown link error"}")
        }
    }

    private fun generateAiReport(response: OpenMeteoResponse) {
        val current = response.current ?: return
        val temp = current.temperature
        val humidity = current.humidity
        val desc = getWeatherDescription(current.weatherCode)
        val wind = current.windSpeed

        _isAiLoading.value = true
        _aiReport.value = "Decoding coordinates and compiling molecular climate matrix..."

        viewModelScope.launch {
            // Retrieve key securely from injected BuildConfig
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") {
                // Highly visual futuristic local recommendation if no API Keyconfigured
                _aiReport.value = "Atmospheric conditions sit at $temp°C with apparent density reflecting $desc. Winds traveling at $wind km/h with relative humidity indices near $humidity%. Sector conditions represent stable planetary habitability."
                _isAiLoading.value = false
                return@launch
            }

            val systemContext = "You are an advanced atmospheric sub-processor AI on a planetary scout cruiser. State direct, compact tactical weather analyses using high-tech cybernetic terminology."
            val userPrompt = """
                Analyse this meteorological feed:
                - Ambient Heat Level: $temp°C (Feels like ${current.apparentTemperature}°C)
                - Sky Code Signature: "$desc"
                - Vector Draft Speeds: $wind km/h
                - Moisture Saturation index: $humidity%
                
                Compute and output a 2-sentence tactical operations summary. Use cool high-tech spacecraft computer buzzwords. Output directly without conversational introductions.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = systemContext))),
                    Content(parts = listOf(Part(text = userPrompt)))
                )
            )

            try {
                val apiResponse = GeminiClient.service.generateContent(key, request)
                val text = apiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    _aiReport.value = text.trim()
                } else {
                    _aiReport.value = "Ambient scan confirmed. Thermal readings: $temp°C. High-performance analysis metrics loaded successfully."
                }
            } catch (e: Exception) {
                _aiReport.value = "Atmospheric feed online. Local temperature index stands at $temp°C ($desc) with moisture levels saturated at $humidity%. Outer shell sensors active."
            } finally {
                _isAiLoading.value = false
            }
        }
    }
}

class WeatherViewModelFactory(
    private val context: Context,
    private val locationTracker: LocationTracker
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(context, locationTracker) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
