package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Theme neon colors
val CyberCyan = Color(0xFF00E5FF)
val CyberOrange = Color(0xFFFF9100)
val CyberPink = Color(0xFFFF1744)
val CyberPurple = Color(0xFFD500F9)
val DarkBackground = Color(0xFF0A0E1A)
val SurfaceObsidian = Color(0xFF121B33)

@Composable
fun RadarScanningWidget(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    
    // Rotating sweep angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    // Pulsing circle rings
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Active blinking targets detected by "GPS Accurate System"
    val blinkingA by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkA"
    )

    Canvas(modifier = modifier.size(160.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Background concentric radar circle ticks
        for (i in 1..4) {
            drawCircle(
                color = CyberCyan.copy(alpha = 0.15f * (i / 4.0f)),
                radius = radius * (i / 4.0f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Crosshairs
        drawLine(
            color = CyberCyan.copy(alpha = 0.3f),
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = CyberCyan.copy(alpha = 0.3f),
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1.dp.toPx()
        )

        // Pulsing radar target rings
        drawCircle(
            color = CyberCyan.copy(alpha = 0.18f),
            radius = radius * pulseScale,
            style = Stroke(width = 1.dp.toPx())
        )

        // Sweeper sector using rotating line with glow gradient
        rotate(rotationAngle, pivot = center) {
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(CyberCyan, CyberCyan.copy(alpha = 0.0f)),
                    start = center,
                    end = Offset(center.x + radius * cos(0.0).toFloat(), center.y + radius * sin(0.0).toFloat())
                ),
                start = center,
                end = Offset(center.x + radius, center.y),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawCircle(
            color = CyberPink.copy(alpha = blinkingA),
            radius = 5.dp.toPx(),
            center = Offset(center.x + radius * 0.45f, center.y - radius * 0.35f)
        )
        drawCircle(
            color = CyberOrange.copy(alpha = blinkingA * 0.8f),
            radius = 4.dp.toPx(),
            center = Offset(center.x - radius * 0.6f, center.y + radius * 0.2f)
        )
    }
}

@Composable
fun WeatherStateCanvas(weatherCode: Int, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherAnim")
    
    // Common animations
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainOffset"
    )

    val scaleProgress by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotate"
    )

    Canvas(modifier = modifier.size(140.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        when (weatherCode) {
            // Sunny / Clear Sky (0, 1)
            0, 1 -> {
                // Outer rotating sun rays
                rotate(rotateAngle, pivot = center) {
                    for (i in 0 until 8) {
                        val angle = (i * 45) * (Math.PI / 180f)
                        val startLen = radius * 0.52f
                        val endLen = radius * 0.82f
                        val st = Offset(
                            center.x + (startLen * cos(angle)).toFloat(),
                            center.y + (startLen * sin(angle)).toFloat()
                        )
                        val ed = Offset(
                            center.x + (endLen * cos(angle)).toFloat(),
                            center.y + (endLen * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = CyberOrange.copy(alpha = 0.85f),
                            start = st,
                            end = ed,
                            strokeWidth = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                // Inner breathing core sun
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberOrange, Color(0xFFFFD54F), CyberOrange.copy(alpha = 0f)),
                        center = center,
                        radius = radius * 0.65f * scaleProgress
                    ),
                    radius = radius * 0.48f,
                    center = center
                )
            }

            // Cloudy / Overcast / Fog (2, 3, 45, 48)
            2, 3, 45, 48 -> {
                // Layered cyberpunk clouds
                val cloudPath = Path().apply {
                    val cw = size.width
                    val ch = size.height
                    moveTo(cw * 0.25f, ch * 0.65f)
                    cubicTo(cw * 0.2f, ch * 0.45f, cw * 0.45f, ch * 0.3f, cw * 0.55f, ch * 0.45f)
                    cubicTo(cw * 0.65f, ch * 0.35f, cw * 0.85f, ch * 0.45f, cw * 0.8f, ch * 0.65f)
                    lineTo(cw * 0.25f, ch * 0.65f)
                    close()
                }

                val offsetCloudPath = Path().apply {
                    val cw = size.width
                    val ch = size.height
                    moveTo(cw * 0.35f, ch * 0.75f)
                    cubicTo(cw * 0.3f, ch * 0.58f, cw * 0.55f, ch * 0.45f, cw * 0.65f, ch * 0.58f)
                    cubicTo(cw * 0.72f, ch * 0.48f, cw * 0.9f, ch * 0.58f, cw * 0.85f, ch * 0.75f)
                    lineTo(cw * 0.35f, ch * 0.75f)
                    close()
                }

                // Draw background cloud
                rotate(degrees = scaleProgress * 3f, pivot = center) {
                    drawPath(
                        path = cloudPath,
                        brush = Brush.linearGradient(
                            colors = listOf(CyberCyan.copy(alpha = 0.4f), CyberPurple.copy(alpha = 0.2f)),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }

                // Draw foreground cloud with higher visibility
                rotate(degrees = -scaleProgress * 4f, pivot = center) {
                    drawPath(
                        path = offsetCloudPath,
                        brush = Brush.linearGradient(
                            colors = listOf(CyberCyan, CyberCyan.copy(alpha = 0.2f)),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, 0f)
                        )
                    )
                }
            }

            // Rainy / Drizzle (51, 53, 55, 61, 63, 65, 80, 81, 82)
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> {
                // Cloud frame at the top
                val rainCloud = Path().apply {
                    val cw = size.width
                    val ch = size.height
                    moveTo(cw * 0.25f, ch * 0.48f)
                    cubicTo(cw * 0.2f, ch * 0.3f, cw * 0.45f, ch * 0.15f, cw * 0.55f, ch * 0.3f)
                    cubicTo(cw * 0.65f, ch * 0.2f, cw * 0.85f, ch * 0.3f, cw * 0.8f, ch * 0.48f)
                    lineTo(cw * 0.25f, ch * 0.48f)
                    close()
                }

                drawPath(
                    path = rainCloud,
                    brush = Brush.linearGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.6f), CyberPurple.copy(alpha = 0.3f)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height * 0.5f)
                    )
                )

                // Render moving rain droplets
                val dropStartY = size.height * 0.52f
                val dropMaxY = size.height * 0.88f
                val count = 4
                for (i in 0 until count) {
                    val xPos = size.width * (0.32f + (i * 0.12f))
                    // Calculate individual drop progress offsetting each index
                    val rawProgress = (offsetProgress + (i * 0.25f)) % 1f
                    val yPos = dropStartY + (dropMaxY - dropStartY) * rawProgress
                    drawLine(
                        color = CyberCyan.copy(alpha = 1.0f - rawProgress),
                        start = Offset(xPos, yPos),
                        end = Offset(xPos - 5f, yPos + 18f),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Severe Storms / Thunderstorms (95, 96, 99)
            95, 96, 99 -> {
                // Storm cloud
                val stormCloud = Path().apply {
                    val cw = size.width
                    val ch = size.height
                    moveTo(cw * 0.25f, ch * 0.45f)
                    cubicTo(cw * 0.18f, ch * 0.25f, cw * 0.45f, ch * 0.08f, cw * 0.55f, ch * 0.25f)
                    cubicTo(cw * 0.65f, ch * 0.15f, cw * 0.88f, ch * 0.22f, cw * 0.8f, ch * 0.45f)
                    lineTo(cw * 0.25f, ch * 0.45f)
                    close()
                }

                drawPath(
                    path = stormCloud,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF37474F), CyberPurple),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height * 0.5f)
                    )
                )

                // Lightning flash pulse
                val flashAlpha = if ((offsetProgress * 15f).toInt() % 4 == 0) 1.0f else 0.15f
                val boltPath = Path().apply {
                    val cw = size.width
                    val ch = size.height
                    moveTo(cw * 0.55f, ch * 0.42f)
                    lineTo(cw * 0.42f, ch * 0.64f)
                    lineTo(cw * 0.54f, ch * 0.64f)
                    lineTo(cw * 0.45f, ch * 0.9f)
                    lineTo(cw * 0.63f, ch * 0.58f)
                    lineTo(cw * 0.51f, ch * 0.58f)
                    close()
                }

                drawPath(
                    path = boltPath,
                    color = CyberOrange.copy(alpha = flashAlpha)
                )
            }

            // Snow (71, 73, 75, 77, 85, 86)
            71, 73, 75, 77, 85, 86 -> {
                // Cloud top
                val snowCloud = Path().apply {
                    val cw = size.width
                    val ch = size.height
                    moveTo(cw * 0.25f, ch * 0.45f)
                    cubicTo(cw * 0.2f, ch * 0.28f, cw * 0.45f, ch * 0.12f, cw * 0.55f, ch * 0.28f)
                    cubicTo(cw * 0.65f, ch * 0.18f, cw * 0.85f, ch * 0.28f, cw * 0.8f, ch * 0.45f)
                    lineTo(cw * 0.25f, ch * 0.45f)
                    close()
                }

                drawPath(
                    path = snowCloud,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), CyberCyan.copy(alpha = 0.2f)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height * 0.5f)
                    )
                )

                // Falling snowflake dots
                val snowStartY = size.height * 0.5f
                val snowMaxY = size.height * 0.85f
                for (i in 0 until 5) {
                    val xPos = size.width * (0.28f + (i * 0.11f))
                    val progress = (offsetProgress + (i * 0.2f)) % 1f
                    val yPos = snowStartY + (snowMaxY - snowStartY) * progress
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 1.0f - progress),
                        radius = 4.dp.toPx() * (1.2f - progress),
                        center = Offset(xPos + sin(progress * Math.PI.toFloat() * 2) * 8f, yPos)
                    )
                }
            }

            // Dynamic Custom Catch-All (Default Windy System)
            else -> {
                // Multi floating flow wind streams
                val y1 = size.height * 0.4f
                val y2 = size.height * 0.55f
                val y3 = size.height * 0.7f

                val flow1 = size.width * offsetProgress
                drawLine(
                    color = CyberCyan.copy(alpha = if (flow1 < size.width * 0.8f) 0.6f else 0f),
                    start = Offset(flow1, y1),
                    end = Offset(flow1 + 35f, y1),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val flow2 = size.width * ((offsetProgress + 0.35f) % 1f)
                drawLine(
                    color = CyberCyan.copy(alpha = if (flow2 < size.width * 0.8f) 0.7f else 0f),
                    start = Offset(flow2, y2),
                    end = Offset(flow2 + 50f, y2),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val flow3 = size.width * ((offsetProgress + 0.7f) % 1f)
                drawLine(
                    color = CyberCyan.copy(alpha = if (flow3 < size.width * 0.8f) 0.5f else 0f),
                    start = Offset(flow3, y3),
                    end = Offset(flow3 + 40f, y3),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// Convert Open-Meteo weather code to beautiful human text
fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear Sky"
        1 -> "Mainly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast Sky"
        45, 48 -> "Dense Fog"
        51, 53, 55 -> "Active Drizzle"
        56, 57 -> "Freezing Drizzle"
        61, 63, 65 -> "Continuous Rain"
        66, 67 -> "Freezing Rain"
        71, 73, 75 -> "Active Snowfall"
        77 -> "Snow Grains"
        80, 81, 82 -> "Heavy Rain Showers"
        85, 86 -> "Active Snow Showers"
        95, 96, 99 -> "Heavy Thunderstorm"
        else -> "Moderate Climate"
    }
}
