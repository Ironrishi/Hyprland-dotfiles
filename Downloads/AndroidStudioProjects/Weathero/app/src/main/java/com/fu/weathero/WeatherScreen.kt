package com.fu.weathero

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ─────────────────────────────────────────────
// Data state
// ─────────────────────────────────────────────

data class WeatherUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val conditionLabel: String = "",
    val conditionEmoji: String = "",
    val temperatureC: Double = 0.0,
    val tempMaxC: Double = 0.0,
    val tempMinC: Double = 0.0,
    val aqi: Int = 0
)

// ─────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────

class WeatherViewModel : ViewModel() {

    companion object {
        val pendingLocation = MutableSharedFlow<Pair<Double, Double>>(replay = 1)
    }

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state

    init {
        viewModelScope.launch {
            pendingLocation.collect { (lat, lon) ->
                load(lat, lon)
            }
        }
    }

    fun load(lat: Double, lon: Double) {
        val isFirstLoad = _state.value.conditionLabel.isEmpty()
        if (isFirstLoad) _state.value = WeatherUiState(isLoading = true)

        viewModelScope.launch {
            try {
                // FIX: Use wrapper functions that always pass all required query params explicitly
                val weather = fetchWeather(lat, lon)
                val aqiResponse = fetchAQI(lat, lon)
                val code = weather.current.weather_code

                _state.value = WeatherUiState(
                    isLoading = false,
                    conditionLabel = weatherCodeToLabel(code),
                    conditionEmoji = weatherCodeToEmoji(code),
                    temperatureC = weather.current.temperature_2m,
                    tempMaxC = weather.daily.temperature_2m_max.firstOrNull() ?: 0.0,
                    tempMinC = weather.daily.temperature_2m_min.firstOrNull() ?: 0.0,
                    aqi = aqiResponse.current.us_aqi
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun weatherCodeToLabel(code: Int): String = when (code) {
        0 -> "Clear Sky"
        1, 2, 3 -> "Partly Cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Showers"
        95 -> "Thunderstorm"
        else -> "Unknown"
    }

    private fun weatherCodeToEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌨️"
        95 -> "⛈️"
        else -> "🌡️"
    }
}

// ─────────────────────────────────────────────
// AQI helpers
// ─────────────────────────────────────────────

fun aqiLabel(aqi: Int): String = when {
    aqi <= 50  -> "Good"
    aqi <= 100 -> "Moderate"
    aqi <= 150 -> "Unhealthy for Sensitive"
    aqi <= 200 -> "Unhealthy"
    aqi <= 300 -> "Very Unhealthy"
    else       -> "Hazardous"
}

fun aqiColor(aqi: Int): Color = when {
    aqi <= 50  -> Color(0xFF4CAF82)
    aqi <= 100 -> Color(0xFFF9C74F)
    aqi <= 150 -> Color(0xFFFF9A3C)
    aqi <= 200 -> Color(0xFFEF5350)
    aqi <= 300 -> Color(0xFF9C27B0)
    else       -> Color(0xFF7B1FA2)
}

// ─────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B2D42), Color(0xFF0D1B2A))
                )
            )
    ) {
        GlowOrb()
        when {
            state.isLoading -> LoadingView()
            state.error != null -> ErrorView(state.error!!)
            else -> WeatherContent(state)
        }
    }
}

@Composable
fun GlowOrb() {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.12f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orb_alpha"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4FC3F7).copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.28f),
                radius = size.width * 0.65f
            )
        )
    }
}

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = Color(0xFF4FC3F7), strokeWidth = 2.dp)
            Text("Fetching weather…", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⚠️", fontSize = 40.sp)
            Text("Something went wrong", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(message, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

@Composable
fun WeatherContent(state: WeatherUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        ConditionHeader(state.conditionEmoji, state.conditionLabel)
        TemperatureDisplay(state.temperatureC)
        MinMaxRow(state.tempMaxC, state.tempMinC)
        Spacer(Modifier.height(4.dp))
        AqiCard(state.aqi)
    }
}

@Composable
fun ConditionHeader(emoji: String, label: String) {
    val offsetY by rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float_y"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.offset(y = offsetY.dp)
    ) {
        Text(emoji, fontSize = 72.sp)
        Text(label.uppercase(), color = Color(0xFF4FC3F7), fontSize = 13.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
    }
}

@Composable
fun TemperatureDisplay(tempC: Double) {
    val animated by animateFloatAsState(
        targetValue = tempC.toFloat(),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "temp_anim"
    )
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center) {
        Text(animated.roundToInt().toString(), color = Color.White,
            fontSize = 96.sp, fontWeight = FontWeight.Thin, lineHeight = 96.sp)
        Text("°C", color = Color.White.copy(alpha = 0.5f), fontSize = 32.sp,
            fontWeight = FontWeight.Light, modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun MinMaxRow(maxC: Double, minC: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
        TempPill("H", maxC, Color(0xFFFF8A65))
        Box(Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))
        TempPill("L", minC, Color(0xFF4FC3F7))
    }
}

@Composable
fun TempPill(label: String, value: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text("${value.roundToInt()}°", color = Color.White.copy(alpha = 0.85f),
            fontSize = 20.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
fun AqiCard(aqi: Int) {
    val color = aqiColor(aqi)
    val label = aqiLabel(aqi)
    val animatedProgress by animateFloatAsState(
        targetValue = (aqi.coerceIn(0, 500) / 500f),
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "aqi_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("AIR QUALITY", color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(label, color = color, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(aqi.toString(), color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress).fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(
                            listOf(Color(0xFF4CAF82), Color(0xFFF9C74F), Color(0xFFEF5350))
                        ))
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("0", "100", "200", "300", "500").forEach {
                    Text(it, color = Color.White.copy(alpha = 0.25f), fontSize = 10.sp)
                }
            }
        }
    }
}
