package com.disaster.management.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disaster.management.data.model.RiskLevel
import com.disaster.management.ui.theme.*

// ── Risk badge chip ────────────────────────────────────────────────────────────

@Composable
fun RiskBadge(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (bg, label) = when (riskLevel) {
        RiskLevel.HIGH   -> RiskHigh   to "🔴 HIGH"
        RiskLevel.MEDIUM -> RiskMedium to "🟠 MEDIUM"
        RiskLevel.LOW    -> RiskLow    to "🟢 LOW"
        RiskLevel.UNKNOWN -> RiskUnknown to "⚪ UNKNOWN"
    }
    Surface(
        color = bg.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
        modifier = modifier.border(1.dp, bg, RoundedCornerShape(50))
    ) {
        Text(
            text = label,
            color = bg,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

// ── Emergency alert banner ─────────────────────────────────────────────────────

@Composable
fun EmergencyAlertBanner(
    visible: Boolean,
    disasterType: String,
    summary: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "alpha"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RiskHigh.copy(alpha = alpha))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Column {
                Text("⚠️ HIGH RISK EMERGENCY", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(disasterType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(summary, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }
        }
    }
}

// ── Section header ─────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

// ── Location row indicator ────────────────────────────────────────────────────

@Composable
fun LocationChip(location: String, modifier: Modifier = Modifier) {
    if (location.isBlank()) return
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Filled.LocationOn, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Text(location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
