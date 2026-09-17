package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.DeviceTelemetry
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.StarkAlert
import com.example.ui.theme.StarkGold
import com.example.ui.theme.StarkSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.JarvisViewModel

@Composable
fun DiagnosticsTab(
    viewModel: JarvisViewModel,
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM TELEMETRY & PROTOCOLS",
                    color = ArcCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Real-time Android hardware sensors & sub-system status",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { viewModel.refreshTelemetry() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ObsidianCard,
                    contentColor = ArcCyan
                ),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ArcCyanDark.copy(alpha = 0.5f))
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "POLL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 1. Power Arc Battery Card
        TelemetryCard(
            title = "POWER SUB-SYSTEM (ARC CORE)",
            icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
            statusBadge = if (telemetry.isCharging) "CHARGING" else "DISCHARGING",
            badgeColor = if (telemetry.isCharging) StarkSuccess else StarkGold
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reserve Capacity",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { telemetry.batteryPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (telemetry.batteryPercent > 20) ArcCyan else StarkAlert,
                    trackColor = Color(0xFF070B14)
                )
            }
        }

        // 2. Comms Array Network Card
        TelemetryCard(
            title = "COMMUNICATIONS ARRAY",
            icon = Icons.Default.Wifi,
            statusBadge = if (telemetry.isConnected) "UPLINK ACTIVE" else "OFFLINE",
            badgeColor = if (telemetry.isConnected) StarkSuccess else StarkAlert
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TelemetryRow(label = "Network Interface", value = telemetry.networkType)
                TelemetryRow(
                    label = "Global Webhook Status",
                    value = if (telemetry.isConnected) "Relay Ready" else "Disabled"
                )
            }
        }

        // 3. Memory & Computational Matrix Card
        TelemetryCard(
            title = "COMPUTE & MEMORY MATRIX",
            icon = Icons.Default.Memory,
            statusBadge = "NOMINAL",
            badgeColor = ArcCyan
        ) {
            val usedRam = (telemetry.totalRamMb - telemetry.availableRamMb).coerceAtLeast(0)
            val ramFraction = if (telemetry.totalRamMb > 0) {
                (usedRam.toFloat() / telemetry.totalRamMb.toFloat()).coerceIn(0f, 1f)
            } else 0.5f

            Column {
                TelemetryRow(
                    label = "RAM Allocation",
                    value = "$usedRam MB / ${telemetry.totalRamMb} MB"
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { ramFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ArcCyanLight,
                    trackColor = Color(0xFF070B14)
                )
                Spacer(modifier = Modifier.height(6.dp))
                TelemetryRow(
                    label = "Available Buffer",
                    value = "${telemetry.availableRamMb} MB Available"
                )
            }
        }

        // 4. Unit Chassis Specs Card
        TelemetryCard(
            title = "HARDWARE TERMINAL CHASSIS",
            icon = Icons.Default.Smartphone,
            statusBadge = "AUTHENTICATED",
            badgeColor = StarkSuccess
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TelemetryRow(label = "Device Model", value = telemetry.deviceModel)
                TelemetryRow(label = "Android OS Version", value = "Android ${telemetry.androidVersion}")
                TelemetryRow(label = "Neural Model Relay", value = "Gemini Flash (SDK v0.9.0)")
            }
        }

        // 5. Tactical Protocol Controls
        Text(
            text = "SYSTEM PROTOCOL CONTROLS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Button(
            onClick = { viewModel.runDiagnosticScan() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("run_diagnostics_scan_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ArcCyan,
                contentColor = Color(0xFF070B14)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Scan",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EXECUTE FULL DIAGNOSTIC SCAN",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Button(
            onClick = { viewModel.runMorningBriefing() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("run_briefing_protocol_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ObsidianCard,
                contentColor = ArcCyanLight
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(ObsidianCardBorder)
            )
        ) {
            Text(
                text = "STARK MORNING BRIEFING PROTOCOL",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TelemetryCard(
    title: String,
    icon: ImageVector,
    statusBadge: String,
    badgeColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = ArcCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = ArcCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusBadge,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
