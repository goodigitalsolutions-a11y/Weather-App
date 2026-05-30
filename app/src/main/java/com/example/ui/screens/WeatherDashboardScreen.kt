package com.example.ui.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.OpenMeteoResponse
import com.example.ui.components.*
import com.example.viewmodel.WeatherUiState
import com.example.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboardScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isGpsActive by viewModel.isGpsActive.collectAsState()
    val currentLat by viewModel.currentLatitude.collectAsState()
    val currentLon by viewModel.currentLongitude.collectAsState()
    val aiReport by viewModel.aiReport.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Manual custom coordinate fields
    var inputLat by remember { mutableStateOf("") }
    var inputLon by remember { mutableStateOf("") }
    var isOverrideDrawerOpen by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Automatically trigger GPS tracking if permission gets granted
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.fetchWeatherForCurrentGps()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF0F1529),
                        Color(0xFF050811)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Main App Header Block (Futuristic Orbital Deck Banner)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .background(SurfaceObsidian.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "METEOROLOGICAL DECK",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "ORBITAL FEED",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Blinking Active GPS telemetry dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (locationPermissionState.status.isGranted && isGpsActive) CyberCyan else CyberOrange)
                    )
                    Text(
                        text = if (locationPermissionState.status.isGranted && isGpsActive) "GPS CO-LINK" else "MANUAL CORE",
                        color = if (locationPermissionState.status.isGranted && isGpsActive) CyberCyan else CyberOrange,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Info & Coordinates Dashboard Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coordinate label
                Text(
                    text = "TARGET L/L: ${String.format("%.4f", currentLat)}°N , ${String.format("%.4f", currentLon)}°E",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = {
                            if (locationPermissionState.status.isGranted) {
                                viewModel.fetchWeatherForCurrentGps()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(SurfaceObsidian, RoundedCornerShape(8.dp))
                            .border(0.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .testTag("refresh_gps_data")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Access GPS Location",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { isOverrideDrawerOpen = !isOverrideDrawerOpen },
                        modifier = Modifier
                            .size(32.dp)
                            .background(SurfaceObsidian, RoundedCornerShape(8.dp))
                            .border(0.5.dp, CyberOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .testTag("toggle_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Manual Override Coordinates",
                            tint = CyberOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Collapsible Manual Coordinates Override Block
            AnimatedVisibility(visible = isOverrideDrawerOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, CyberOrange.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .background(SurfaceObsidian.copy(alpha = 0.7f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MANUAL COORDINATE SELECTOR",
                        fontSize = 11.sp,
                        color = CyberOrange,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = inputLat,
                            onValueChange = { inputLat = it },
                            placeholder = { Text("Lat (e.g. 35.67)", fontSize = 12.sp, color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .testTag("lat_input_field"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = CyberOrange,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        TextField(
                            value = inputLon,
                            onValueChange = { inputLon = it },
                            placeholder = { Text("Lon (e.g. 139.65)", fontSize = 12.sp, color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .testTag("lon_input_field"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = CyberOrange,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Button(
                            onClick = {
                                val latDouble = inputLat.toDoubleOrNull()
                                val lonDouble = inputLon.toDoubleOrNull()
                                if (latDouble != null && lonDouble != null) {
                                    viewModel.setCoordinatesManually(latDouble, lonDouble, "Manual Grid Point")
                                    isOverrideDrawerOpen = false
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("apply_manual_coords")
                        ) {
                            Text("GO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Futuristic Quick Presets
                    Text(
                        text = "GLOBAL METRO PRESETS:",
                        fontSize = 9.sp,
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            Triple("TOKYO", 35.6762, 139.6503),
                            Triple("LONDON", 51.5074, -0.1278),
                            Triple("NEW YORK", 40.7128, -74.0060),
                            Triple("SYDNEY", -33.8688, 151.2093),
                            Triple("CAIRO", 30.0444, 31.2357)
                        )
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .clickable {
                                        viewModel.setCoordinatesManually(preset.second, preset.third, "${preset.first} Preset Zone")
                                        isOverrideDrawerOpen = false
                                        focusManager.clearFocus()
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    preset.first,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Main State Machine Board
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = CyberCyan)
                            Text(
                                text = "ESTABLISHING GROUND STATION DOWNLINK...",
                                color = CyberCyan.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                is WeatherUiState.Success -> {
                    val weatherData = state.data
                    val currentWeather = weatherData.current

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Segment 1: Physical GPS and Interactive Radar Reading Frame
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .background(SurfaceObsidian.copy(alpha = 0.3f))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left element: Floating Canvas weather state graphic representation
                                Box(
                                    modifier = Modifier
                                        .size(130.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    WeatherStateCanvas(
                                        weatherCode = currentWeather?.weatherCode ?: 0
                                    )
                                }

                                // Right element: Holographic real-time telemetry grid details
                                Column(
                                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = state.locationLabel.uppercase(),
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "${currentWeather?.temperature ?: 0.0}°C",
                                        color = Color.White,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp
                                    )

                                    Text(
                                        text = "FEELS LIKE: ${currentWeather?.apparentTemperature ?: 0.0}°C",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = getWeatherDescription(currentWeather?.weatherCode ?: 0).uppercase(),
                                            color = CyberCyan,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Segment 2: AI Spacecraft Atmospheric Advisory Unit (Gemini Core integration)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CyberPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = SurfaceObsidian.copy(alpha = 0.45f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SmartToy,
                                                contentDescription = "AI Atmospheric Core Scanner",
                                                tint = CyberPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "ATMOSPHERIC NEURAL ADVISORY",
                                                color = CyberPurple,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }

                                        if (isAiLoading) {
                                            CircularProgressIndicator(
                                                color = CyberPurple,
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 1.5.dp
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0xFF00E676))
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = aiReport,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Segment 3: Meteorological Metric Grid Dials
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Humidity gauge
                                FileMetricCard(
                                    icon = Icons.Default.WaterDrop,
                                    label = "HUMIDITY",
                                    value = "${currentWeather?.humidity ?: 0.0}%",
                                    tint = CyberCyan,
                                    modifier = Modifier.weight(1f)
                                )

                                // Wind speed gauge
                                FileMetricCard(
                                    icon = Icons.Default.Air,
                                    label = "WIND FEED",
                                    value = "${currentWeather?.windSpeed ?: 0.0} km/h",
                                    tint = CyberOrange,
                                    modifier = Modifier.weight(1f)
                                )

                                // Precipitation levels
                                FileMetricCard(
                                    icon = Icons.Default.Co2,
                                    label = "PRECIP INDEX",
                                    value = "${currentWeather?.precipitation ?: 0.0} mm",
                                    tint = CyberPink,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Segment 4: Radar Sweeper Animation Representation
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CyberCyan.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .background(SurfaceObsidian.copy(alpha = 0.2f))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                RadarScanningWidget(modifier = Modifier.size(72.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "LIVE SATELLITE RANGE",
                                        color = CyberCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Continuous ground tracking is co-linking coordinate indexes to high-gain orbit feeds correctly.",
                                        fontSize = 11.sp,
                                        color = Color.LightGray.copy(alpha = 0.7f),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Segment 5: 24-Hour Horizon Prognosis Slider
                        weatherData.hourly?.let { hourly ->
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "24-HOUR RADAR CYCLE",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Take the first 24 hours
                                        val count = minOf(24, hourly.time.size)
                                        items(count) { index ->
                                            val rawTime = hourly.time[index]
                                            val formattedTime = try {
                                                val parseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                                                val resultDate = parseFormat.parse(rawTime)
                                                val outFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                                resultDate?.let { outFormat.format(it) } ?: "N/A"
                                            } catch (e: Exception) {
                                                "N/A"
                                            }

                                            val temp = hourly.temperatureList.getOrNull(index) ?: 0.0
                                            val wCode = hourly.weatherCodeList.getOrNull(index) ?: 0

                                            Column(
                                                modifier = Modifier
                                                    .width(68.dp)
                                                    .border(0.5.dp, CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .background(SurfaceObsidian.copy(alpha = 0.5f))
                                                    .padding(vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = formattedTime,
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )

                                                Box(
                                                    modifier = Modifier.size(32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    WeatherStateCanvas(
                                                        weatherCode = wCode,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }

                                                Text(
                                                    text = "${temp}°",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Segment 6: Long-Range 7-Day Planetary Outlook Deck
                        weatherData.daily?.let { daily ->
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = "PLANETARY METRIC LOOKOUT (7-DAY)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val dayCount = daily.time.size
                                        for (i in 0 until dayCount) {
                                            val rawDate = daily.time[i]
                                            val dayFormatted = try {
                                                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                val date = parser.parse(rawDate)
                                                val output = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                                                date?.let { output.format(it) } ?: "Metric Point"
                                            } catch (e: Exception) {
                                                "Metric Point"
                                            }

                                            val code = daily.weatherCodeList[i]
                                            val maxT = daily.tempMax[i]
                                            val minT = daily.tempMin[i]

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                                    .background(SurfaceObsidian.copy(alpha = 0.25f))
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1.5f)) {
                                                    Text(
                                                        text = dayFormatted,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = getWeatherDescription(code),
                                                        color = CyberCyan.copy(alpha = 0.8f),
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .weight(0.8f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    WeatherStateCanvas(
                                                        weatherCode = code,
                                                        modifier = Modifier.size(34.dp)
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${minT}°C",
                                                        color = Color.White.copy(alpha = 0.6f),
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                    Text(
                                                        text = "${maxT}°C",
                                                        color = CyberOrange,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is WeatherUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error icon",
                                tint = CyberPink,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = state.message,
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Button(
                                onClick = { viewModel.fetchWeatherForCurrentGps() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("RETRY DOWNLOAD LINK", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(0.5.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .background(SurfaceObsidian.copy(alpha = 0.35f))
            .padding(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
