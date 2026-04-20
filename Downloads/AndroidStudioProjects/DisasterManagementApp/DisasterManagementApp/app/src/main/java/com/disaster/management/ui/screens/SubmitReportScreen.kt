package com.disaster.management.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disaster.management.data.model.AnalyzeResponse
import com.disaster.management.data.model.RiskLevel
import com.disaster.management.data.model.UiState
import com.disaster.management.ui.components.EmergencyAlertBanner
import com.disaster.management.ui.components.RiskBadge
import com.disaster.management.ui.components.SectionHeader
import com.disaster.management.viewmodel.DisasterViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SubmitReportScreen(viewModel: DisasterViewModel) {
    val reportText by viewModel.reportText.collectAsState()
    val locationText by viewModel.locationText.collectAsState()
    val analyzeState by viewModel.analyzeState.collectAsState()
    val context = LocalContext.current

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Header ─────────────────────────────────────────────────────────────
        Column {
            Text("🚨 Report a Disaster", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary)
            Text("Describe what you're seeing and our AI will assess the risk.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // ── Emergency banner (only when High risk result visible) ──────────────
        val successData = (analyzeState as? UiState.Success)?.data
        val riskLevel = successData?.let { RiskLevel.from(it.riskLevel) } ?: RiskLevel.UNKNOWN
        EmergencyAlertBanner(
            visible = riskLevel == RiskLevel.HIGH,
            disasterType = successData?.disasterType ?: "",
            summary = successData?.summary ?: ""
        )

        // ── Report text input ─────────────────────────────────────────────────
        SectionHeader("Incident Description")
        OutlinedTextField(
            value = reportText,
            onValueChange = viewModel::onReportTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            placeholder = { Text("e.g. Large wildfire is spreading quickly toward nearby homes...") },
            label = { Text("Description") },
            maxLines = 8,
            shape = RoundedCornerShape(12.dp)
        )

        // ── Location input ────────────────────────────────────────────────────
        SectionHeader("Location (optional)")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = locationText,
                onValueChange = viewModel::onLocationTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter address or city") },
                label = { Text("Location") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            FilledTonalIconButton(
                onClick = {
                    if (locationPermission.status.isGranted) {
                        fetchCurrentLocation(context) { viewModel.setLocationFromGps(it) }
                    } else {
                        locationPermission.launchPermissionRequest()
                    }
                }
            ) {
                Icon(Icons.Filled.GpsFixed, contentDescription = "Use GPS location")
            }
        }

        // ── Submit button ─────────────────────────────────────────────────────
        Button(
            onClick = viewModel::submitReport,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = reportText.isNotBlank() && analyzeState !is UiState.Loading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (analyzeState is UiState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Analyzing...")
            } else {
                Icon(Icons.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Analyze Report", fontWeight = FontWeight.Bold)
            }
        }

        // ── Error ─────────────────────────────────────────────────────────────
        if (analyzeState is UiState.Error) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "❌ ${(analyzeState as UiState.Error).message}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // ── Result card ───────────────────────────────────────────────────────
        AnimatedContent(targetState = successData, label = "result") { data ->
            if (data != null) {
                AnalysisResultCard(data, onDismiss = viewModel::resetAnalyzeState)
            }
        }

        Spacer(Modifier.height(80.dp)) // bottom nav space
    }
}

@Composable
private fun AnalysisResultCard(data: AnalyzeResponse, onDismiss: () -> Unit) {
    val risk = RiskLevel.from(data.riskLevel)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI Analysis Result", fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium)
                RiskBadge(risk)
            }
            HorizontalDivider()
            ResultRow("Disaster Type", data.disasterType)
            ResultRow("Confidence", data.confidence)
            ResultRow("Summary", data.summary)
            OutlinedButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Clear")
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(context: Context, onResult: (String) -> Unit) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    client.lastLocation.addOnSuccessListener { loc ->
        if (loc != null) {
            onResult("${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)}")
        }
    }
}
